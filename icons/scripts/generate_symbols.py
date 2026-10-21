#!/usr/bin/env python3
"""Genera ImageVector de Material Symbols Rounded (outline + filled) a :icons.

Ús: python3 icons/scripts/generate_symbols.py
Font: github.com/google/material-design-icons (Apache 2.0). Torneu-lo a executar
després d'afegir un nom a ICONS.
"""
import re
import urllib.request
from pathlib import Path

BASE = "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web"
OUT = Path(__file__).resolve().parent.parent / "src/main/kotlin/com/alorma/caducity/base/ui/icons/symbols"
PKG = "com.alorma.caducity.base.ui.icons.symbols"

ICONS = [
    # navegació
    "hourglass_bottom", "kitchen", "settings", "add", "arrow_back", "chevron_left", "chevron_right",
    "expand_more", "expand_less", "unfold_more", "close", "search", "more_vert", "calendar_month",
    # estat / accions
    "check", "check_circle", "ac_unit", "event_repeat", "delete", "edit", "drive_file_move",
    "undo", "warning", "error", "info", "schedule", "today", "notifications", "backup", "shield",
    "palette", "language", "tune", "lock", "info_i", "widgets",
    "history", "add_circle", "cancel", "delete_history", "event", "touch_app", "checklist", "water_drop", "delete_sweep",
    # categories
    "water_full", "nutrition", "set_meal", "grocery", "bakery_dining", "local_drink", "icecream",
    "eco", "egg", "lunch_dining", "restaurant", "cookie", "local_pizza", "ramen_dining", "kebab_dining",
    "emoji_food_beverage", "wine_bar", "coffee", "cake", "fastfood", "pets", "child_care", "soup_kitchen",
    # onboarding
    "radio_button_unchecked", "notifications_active", "smartphone", "cloud_upload", "arrow_forward", "menu_book",
    # afegides a mà abans (no eren a la llista)
    "lightbulb", "more_horiz", "remove", "wallpaper",
    # ajustos
    "event_available", "view_week", "contrast",
    # descongela
    "sunny",
    # tipus d'unitat
    "tag", "takeout_dining", "inventory_2", "scale", "back_hand", "battery_full", "battery_4_bar", "battery_1_bar",
]


def fetch(name: str, filled: bool) -> str:
    suffix = "_fill1_24px.svg" if filled else "_24px.svg"
    url = f"{BASE}/{name}/materialsymbolsrounded/{name}{suffix}"
    try:
        with urllib.request.urlopen(url) as r:
            svg = r.read().decode()
    except Exception as e:  # noqa: BLE001
        raise SystemExit(f"No s'ha pogut baixar {url}: {e}")
    return " ".join(re.findall(r'<path d="([^"]+)"', svg))


def pascal(name: str) -> str:
    return "".join(p.capitalize() for p in name.split("_"))


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.kt"):
        old.unlink()
    (OUT / "Symbols.kt").write_text(
        f"package {PKG}\n\n"
        "/** Material Symbols Rounded (24 dp). Generat per `icons/scripts/generate_symbols.py`. */\n"
        "object Symbols {\n  object Outlined\n\n  object Filled\n}\n"
    )
    for name in ICONS:
        cls = pascal(name)
        body = [f"package {PKG}\n"]
        body.append(
            "import androidx.compose.ui.graphics.Color\n"
            "import androidx.compose.ui.graphics.SolidColor\n"
            "import androidx.compose.ui.graphics.vector.ImageVector\n"
            "import androidx.compose.ui.graphics.vector.addPathNodes\n"
            "import androidx.compose.ui.unit.dp\n"
        )
        for filled in (False, True):
            holder = "Filled" if filled else "Outlined"
            d = fetch(name, filled)
            body.append(
                f"private var _{holder}{cls}: ImageVector? = null\n\n"
                f"val Symbols.{holder}.{cls}: ImageVector\n"
                f"  get() =\n"
                f"    _{holder}{cls} ?: ImageVector\n"
                f"      .Builder(\n"
                f"        name = \"{holder}.{cls}\",\n"
                f"        defaultWidth = 24.dp,\n"
                f"        defaultHeight = 24.dp,\n"
                f"        viewportWidth = 960f,\n"
                f"        viewportHeight = 960f,\n"
                f"      ).apply {{\n"
                f"        addGroup(translationY = 960f)\n"
                f"        addPath(\n"
                f"          pathData = addPathNodes(\"{d}\"),\n"
                f"          fill = SolidColor(Color.Black),\n"
                f"        )\n"
                f"        clearGroup()\n"
                f"      }}.build()\n"
                f"      .also {{ _{holder}{cls} = it }}\n"
            )
        (OUT / f"{cls}.kt").write_text("\n".join(body))
        print("ok", name)


if __name__ == "__main__":
    main()
