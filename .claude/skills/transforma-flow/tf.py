#!/usr/bin/env python3
"""Drives the Transforma flow on the connected Android device (adb, uiautomator) from a clean install.

    python3 -I tf.py <menu|one|multi|all> [--lang ca|es|en] [--out DIR] [--serial SERIAL]

The UI texts are read from app/src/main/res/values*/strings.xml, so the script follows the app language;
the product names (no accents: `adb input text` cannot type them) come from NAMES below.
"""

import argparse
import html
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

SERIAL = None  # resolved by pick_device(): ANDROID_SERIAL, --serial, or the only (or only physical) device connected
PKG = "com.alorma.caducity.dev"
PREFS = "shared_prefs/com.alorma.caducity.dev_preferences.xml"
RES = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../app/src/main/res"))

NAMES = {
    "ca": dict(
        chicken="Pollastre sencer", stock="Verdures per al caldo", onions="Cebes", milk="Llet", yogurt="Iogurts",
        eggs="Ous", breasts="Pits de pollastre", wings="Aletes", thighs="Cuixes", carcass="Carcanada",
        broth="Caldo", croquettes="Croquetes",
    ),
    "es": dict(
        chicken="Pollo entero", stock="Verduras para caldo", onions="Cebollas", milk="Leche", yogurt="Yogures",
        eggs="Huevos", breasts="Pechugas de pollo", wings="Alitas", thighs="Muslos", carcass="Carcasa",
        broth="Caldo", croquettes="Croquetas",
    ),
    "en": dict(
        chicken="Whole chicken", stock="Stock vegetables", onions="Onions", milk="Milk", yogurt="Yogurts",
        eggs="Eggs", breasts="Chicken breasts", wings="Wings", thighs="Drumsticks", carcass="Carcass",
        broth="Broth", croquettes="Croquettes",
    ),
}
FOLDERS = {"ca": "values-ca", "es": "values-es", "en": "values"}

# ---------------------------------------------------------------------------------------------- strings


def _load(folder):
    tree = ET.parse(os.path.join(RES, folder, "strings.xml"))
    out = {}
    for el in tree.getroot():
        if el.tag == "string":
            out[el.get("name")] = el.text or ""
        elif el.tag == "plurals":
            out[el.get("name")] = {i.get("quantity"): i.text or "" for i in el}
    return out


class Strings:
    def __init__(self, lang):
        self.base, self.loc = _load("values"), _load(FOLDERS[lang])

    def raw(self, key):
        v = self.loc.get(key, self.base[key])
        return v

    def get(self, key, *args, quantity="other"):
        v = self.raw(key)
        if isinstance(v, dict):
            v = v.get(quantity) or v["other"]
        v = v.replace("\\'", "'").replace('\\"', '"').replace("\\n", "\n")
        v = html.unescape(v)
        for i, a in enumerate(args, 1):
            v = v.replace(f"%{i}$d", str(a)).replace(f"%{i}$s", str(a))
        if args:
            for a in args:
                v = re.sub(r"%[ds]", str(a), v, count=1)
        return v

    def regex(self, key):
        """A regex matching any quantity of a (plural) string, with numbers replaced by \\d+."""
        v = self.raw(key)
        variants = list(v.values()) if isinstance(v, dict) else [v]
        pats = []
        for s in variants:
            s = html.unescape(s.replace("\\'", "'"))
            parts = re.split(r"%(?:\d\$)?[ds]", s)
            pats.append("^" + r"\d+".join(re.escape(p) for p in parts) + "$")
        return "|".join(pats)


# ---------------------------------------------------------------------------------------------- adb / ui


def pick_device(wanted=None):
    """The device that is connected: the one asked for, else the only one, else the only physical one."""
    wanted = wanted or os.environ.get("ANDROID_SERIAL")
    lines = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout.splitlines()[1:]
    devices = [l.split()[0] for l in lines if l.strip().endswith("device")]
    if wanted:
        if wanted not in devices:
            raise RuntimeError(f"device {wanted} is not connected ({devices})")
        return wanted
    if len(devices) == 1:
        return devices[0]
    physical = [d for d in devices if not d.startswith("emulator-")]
    if len(physical) == 1:
        return physical[0]
    raise RuntimeError(f"several devices connected {devices}: pass --serial or set ANDROID_SERIAL")


def adb(*args, check=True, binary=False):
    r = subprocess.run(["adb", "-s", SERIAL, *args], capture_output=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"adb {' '.join(args)}: {r.stderr.decode()}")
    return r.stdout if binary else r.stdout.decode(errors="replace")


class Node:
    def __init__(self, el):
        self.text = el.get("text", "")
        self.desc = el.get("content-desc", "")
        self.cls = el.get("class", "")
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", el.get("bounds", "[0,0][0,0]"))
        self.x1, self.y1, self.x2, self.y2 = map(int, m.groups())

    @property
    def cx(self):
        return (self.x1 + self.x2) // 2

    @property
    def cy(self):
        return (self.y1 + self.y2) // 2

    def label(self):
        return self.text or self.desc

    def __repr__(self):
        return f"<{self.label()!r} @{self.cx},{self.cy}>"


def dump():
    for _ in range(5):
        raw = adb("exec-out", "uiautomator", "dump", "/dev/stdout", binary=True).decode(errors="replace")
        end = raw.find("</hierarchy>")
        if end > 0:
            root = ET.fromstring(raw[: end + len("</hierarchy>")])
            return [Node(e) for e in root.iter("node") if e.get("text") or e.get("content-desc")]
        time.sleep(0.5)
    raise RuntimeError("uiautomator dump failed")


def match(n, label=None, prefix=None, regex=None):
    for s in (n.text, n.desc):
        if not s:
            continue
        if label is not None and s == label:
            return True
        if prefix is not None and s.startswith(prefix):
            return True
        if regex is not None and re.search(regex, s):
            return True
    return False


def find(label=None, prefix=None, regex=None, nodes=None):
    return [n for n in (nodes if nodes is not None else dump()) if match(n, label, prefix, regex)]


def tap_xy(x, y):
    adb("shell", "input", "tap", str(x), str(y))


def wait(label=None, prefix=None, regex=None, timeout=8, gone=False):
    end = time.time() + timeout
    while time.time() < end:
        hits = find(label, prefix, regex)
        if bool(hits) != gone:
            return hits
        time.sleep(0.4)
    raise TimeoutError(f"wait {'gone ' if gone else ''}{label or prefix or regex!r}")


def tap(label=None, prefix=None, regex=None, nth=0, timeout=8, last=False):
    hits = wait(label, prefix, regex, timeout)
    n = hits[-1] if last else hits[nth]
    tap_xy(n.cx, n.cy)
    time.sleep(0.5)
    return n


def keyboard_shown():
    return "mInputShown=true" in adb("shell", "dumpsys", "input_method", check=False)


def hide_kb():
    if keyboard_shown():
        adb("shell", "input", "keyevent", "4")
        time.sleep(0.7)


def typed(s):
    adb("shell", "input", "text", s.replace(" ", "%s"))
    time.sleep(0.3)


def swipe(up=True):
    a, b = (1900, 700) if up else (700, 1900)
    adb("shell", "input", "swipe", "540", str(a), "540", str(b), "300")
    time.sleep(0.7)


def scroll_to(label=None, prefix=None, regex=None, tries=8):
    for _ in range(tries):
        hits = find(label, prefix, regex)
        if hits and 300 < hits[0].cy < 2150:
            return hits[0]
        swipe(up=True)
    raise TimeoutError(f"scroll_to {label or prefix or regex!r}")


def shot(name):
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name + ".png"), "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", binary=True))


# ---------------------------------------------------------------------------------------------- setup


def reset_app(lang):
    adb("shell", "pm", "clear", PKG)
    adb("shell", "cmd", "locale", "set-app-locales", PKG, "--locales", lang)
    adb("shell", "monkey", "-p", PKG, "-c", "android.intent.category.LAUNCHER", "1", check=False)
    tap(S.get("ob_start"))
    tap(S.get("ob_category_ready_meals"))
    tap(regex=S.regex("ob_categories_continue"))
    tap(S.get("ob_not_now"))
    tap(S.get("ob_continue"))  # Transforma step
    tap(S.get("ob_continue"))  # privacy
    chk = wait(S.get("ob_disclaimer_check"))[0]
    tap_xy(100, chk.cy)
    time.sleep(0.5)
    tap(S.get("ob_accept_and_start"))
    wait(desc_label("action_add_product"))
    adb("shell", "am", "force-stop", PKG)
    patch = (
        "run-as %s sed -i -e '/theme_mode/d' -e "
        "'s|</map>|    <string name=\"theme_mode\">LIGHT</string>\\n</map>|' %s"
    ) % (PKG, PREFS)
    adb("shell", patch)
    adb("shell", "monkey", "-p", PKG, "-c", "android.intent.category.LAUNCHER", "1", check=False)
    wait(desc_label("action_add_product"))


def desc_label(key):
    return S.get(key)


def add_product(name, category_key, date_key, qty):
    tap(desc_label("action_add_product"))
    time.sleep(1.0)
    typed(name)
    hide_kb()
    tap(S.get(category_key))
    tap(S.get(date_key))
    for _ in range(qty - 1):
        tap(S.get("add_quantity_increase"))
    tap(S.get("add_save"))
    wait(desc_label("action_add_product"))
    time.sleep(1.0)


def seed():
    n = N
    add_product(n["chicken"], "ob_category_meat_fish", "add_quick_3_days", 1)
    add_product(n["stock"], "ob_category_produce", "add_quick_1_week", 1)
    add_product(n["onions"], "ob_category_produce", "add_quick_2_weeks", 3)
    add_product(n["milk"], "ob_category_dairy", "add_quick_1_week", 2)
    add_product(n["yogurt"], "ob_category_dairy", "add_quick_2_weeks", 4)
    add_product(n["eggs"], "ob_category_dairy", "add_quick_2_weeks", 6)


# ---------------------------------------------------------------------------------------------- transform screen


def go_home():
    for _ in range(4):
        if find(desc_label("action_add_product")):
            return
        adb("shell", "input", "keyevent", "4")
        time.sleep(0.8)


def open_fresh_list():
    go_home()
    tap(S.get("home_summary_fresh"))
    time.sleep(1.0)


def tap_row(name):
    n = scroll_to(prefix=name)
    tap_xy(n.cx, n.cy)
    time.sleep(0.6)


def picker_add(names):
    """From the Transform screen: «Afegeix ingredient» → picker → select [names] → add."""
    # Without ingredients the dashed «Afegeix ingredient» row is there; with some, the «+ Afegeix» of their header.
    tap(S.get("transform_add_ingredient") if find(label=S.get("transform_add_ingredient")) else S.get("transform_add"))
    wait(S.get("transform_picker_search"))
    for name in names:
        tap_xy(540, 366)  # search field
        time.sleep(0.4)
        typed(name)
        hide_kb()
        rows = [n for n in find(label=name) if n.cy > 700]
        tap_xy(rows[0].cx, rows[0].cy)
        time.sleep(0.5)
        tap_xy(540, 366)
        time.sleep(0.3)
        adb("shell", "input", "keyevent", "123")  # move to end
        for _ in name:
            adb("shell", "input", "keyevent", "67")  # delete
        hide_kb()
    tap(regex=S.regex("transform_picker_add"))
    wait(regex=S.regex("transform_save_count"))
    time.sleep(0.8)


def open_transform(mode, names):
    if mode == "menu":
        go_home()
        tap(desc_label("action_more_actions"))
        tap(S.get("transform_title"))
        wait(S.get("transform_add_ingredient"))
        picker_add(names)
    elif mode == "one":
        open_fresh_list()
        tap_row(names[0])
        tap(S.get("item_actions_transform"))
        wait(regex=S.regex("transform_save_count"))
        if len(names) > 1:
            picker_add(names[1:])
    elif mode == "multi":
        open_fresh_list()
        tap(S.get("status_select"))
        for name in names:
            tap_row(name)
        tap(S.get("transform_bar_transform"), last=True)
        wait(regex=S.regex("transform_save_count"))
    time.sleep(0.8)


def set_counts(targets):
    """targets: {ingredient name: units to spend}. Only products with several units have a chip («1 de 25 ⇅»):
    tapping it turns it into the stepper."""
    for name, want in targets.items():
        nodes = dump()
        chips = [n for n in nodes if re.search(S.regex("transform_ingredient_units_edit"), n.desc)]
        owners = [n for n in nodes if n.text == name]
        for chip in chips:
            owner = max((n for n in owners if n.cy < chip.cy and abs(n.cx - chip.cx) < 220), key=lambda n: n.cy, default=None)
            if owner is None:
                continue
            cur = int(re.findall(r"\d+", chip.desc)[0])
            if cur == want:
                break
            tap_xy(chip.cx, chip.cy)
            time.sleep(0.5)
            nodes = dump()
            label = S.get("quantity_more") if want > cur else S.get("quantity_less")
            btn = min((n for n in nodes if n.desc == label and abs(n.cy - chip.cy) < 60), key=lambda n: abs(n.cx - chip.cx))
            for _ in range(abs(want - cur)):
                tap_xy(btn.cx, btn.cy)
                time.sleep(0.25)
            break
    time.sleep(0.4)


def fill_result(spec, first):
    if not first:
        tap(S.get("transform_add"), last=True)  # the «+ Afegeix» of the results header, always the last one
        time.sleep(1.0)
    typed(spec["name"])
    hide_kb()
    scroll_to(S.get("transform_result_done"))
    nodes = dump()
    more = sorted([n for n in nodes if n.desc == S.get("quantity_more")], key=lambda n: n.cy)
    serv, frz = more[-2], more[-1]
    for _ in range(spec["servings"] - 1):
        tap_xy(serv.cx, serv.cy)
    time.sleep(0.4)
    for _ in range(spec["frozen"]):
        tap_xy(frz.cx, frz.cy)
    time.sleep(0.4)
    if spec.get("category"):
        nodes = dump()
        top = [n for n in nodes if n.text == S.get("transform_servings")][-1]
        bottom = [n for n in nodes if n.text == S.get("transform_freeze")][-1]
        cats = {S.get(k) for k in CATEGORY_KEYS}
        chip = [n for n in nodes if n.text in cats and top.cy < n.cy < bottom.cy][0]
        tap_xy(chip.cx, chip.cy)
        time.sleep(0.6)
        target = [n for n in find(label=S.get(spec["category"])) if n.cy > chip.cy] or find(label=S.get(spec["category"]))
        tap_xy(target[-1].cx, target[-1].cy)
        time.sleep(0.6)


def save_transform(tag):
    shot(f"{OUT_TAG}-{tag}-before-save")
    tap(regex=S.regex("transform_save_count"), nth=0)
    pats = "|".join(S.regex(k) for k in ("transform_done", "transform_done_one_ingredient", "transform_done_one_product", "transform_done_one_one"))
    hit = wait(regex=pats, timeout=10)[0]
    shot(f"{OUT_TAG}-{tag}-after-save")
    RESULTS.append(f"{OUT_TAG} {tag}: {hit.text}")
    time.sleep(3.5)


CATEGORY_KEYS = [
    "ob_category_dairy", "ob_category_produce", "ob_category_meat_fish", "ob_category_pantry",
    "ob_category_bakery", "ob_category_ready_meals",
]


def transformations(mode):
    n = N
    # 1. whole chicken → breasts (frozen), wings, thighs (1 frozen), carcass
    open_transform(mode, [n["chicken"]])
    fill_result(dict(name=n["breasts"], servings=2, frozen=2), True)
    fill_result(dict(name=n["wings"], servings=4, frozen=0), False)
    fill_result(dict(name=n["thighs"], servings=2, frozen=1), False)
    fill_result(dict(name=n["carcass"], servings=1, frozen=0), False)
    save_transform("1-chicken")
    # 2. carcass + stock vegetables → 4 broths (3 frozen)
    open_transform(mode, [n["carcass"], n["stock"]])
    fill_result(dict(name=n["broth"], servings=4, frozen=3, category="ob_category_ready_meals"), True)
    save_transform("2-broth")
    # 3. one thigh + one onion (+ 2 eggs) → 16 croquettes (12 frozen)
    open_transform(mode, [n["thighs"], n["onions"], n["eggs"]])
    set_counts({n["thighs"]: 1, n["onions"]: 1, n["eggs"]: 2})
    fill_result(dict(name=n["croquettes"], servings=16, frozen=12, category="ob_category_ready_meals"), True)
    save_transform("3-croquettes")


def verify():
    out = []
    for tile, title in (("home_summary_fresh", "FRESH"), ("home_summary_frozen", "FROZEN")):
        go_home()
        tap(S.get(tile))
        time.sleep(1.2)
        rows = []
        for _ in range(4):
            rows += [n.text for n in dump() if any(n.text.startswith(v) for v in N.values())]
            swipe(up=True)
        out.append(f"{title}: " + " | ".join(dict.fromkeys(rows)))
    return out


def main():
    global S, N, OUT, OUT_TAG, RESULTS, SERIAL
    ap = argparse.ArgumentParser()
    ap.add_argument("mode", choices=["menu", "one", "multi", "all"])
    ap.add_argument("--lang", default="ca", choices=["ca", "es", "en"])
    ap.add_argument("--out", default="/tmp/transforma-flow")
    ap.add_argument("--serial", help="adb serial; default: the connected device")
    a = ap.parse_args()
    SERIAL = pick_device(a.serial)
    print(f"device: {SERIAL}", flush=True)
    S, N, OUT, RESULTS = Strings(a.lang), NAMES[a.lang], a.out, []
    modes = ["menu", "one", "multi"] if a.mode == "all" else [a.mode]
    for mode in modes:
        OUT_TAG = mode
        print(f"== {mode} ({a.lang})", flush=True)
        reset_app(a.lang)
        seed()
        transformations(mode)
        for line in verify():
            RESULTS.append(f"{mode} {line}")
        shot(f"{mode}-final")
    print("\n".join(RESULTS))


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        import traceback

        traceback.print_exc()
        print("FAILED:", repr(e), file=sys.stderr)
        try:
            shot("failure")
            print(f"screenshot: {OUT}/failure.png", file=sys.stderr)
        except Exception:
            pass
        sys.exit(1)
