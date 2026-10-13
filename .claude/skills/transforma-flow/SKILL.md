---
name: transforma-flow
description: Run the Transforma end-to-end flow on the connected Android device from a clean install (reset, seed products, three transformations) through the «⌄» menu, one ingredient, or a multi-selection. Use to smoke-test Transforma after changing it.
argument-hint: "[menu|one|multi|all] [ca|es|en]"
disable-model-invocation: true
---

# Transforma flow

Drives the real app on the **connected device** with `adb` + `uiautomator`. The script is `tf.py`, next to this file.
The device is the one that is active: `--serial` or `ANDROID_SERIAL` if given, else the only connected device, else
the only physical one; with several candidates it stops and asks. It never hardcodes a device.

## Arguments

`$ARGUMENTS` = `<entry> [lang]`. Defaults: `all` and `ca`.

| Entry | How Transforma is opened |
|---|---|
| `menu` | «+ ⌄» → Transforma (free entry), ingredients added from the picker |
| `one` | Item actions sheet of one product → «Transforma aquest producte», extra ingredients from the picker |
| `multi` | Fresh list → Select → several rows → Transforma in the selection bar |
| `all` | The three above, in that order, each from a clean install |

`lang` is the **app language** (`ca`, `es`, `en`). UI texts come from `app/src/main/res/values*/strings.xml`; product
names come from `NAMES` in `tf.py` (no accents: `adb input text` cannot type them). Add a language by adding a
`NAMES` entry and a `FOLDERS` entry.

## What it does, per entry

1. `pm clear` the `.dev` app, set the app locale, go through onboarding, then force Light theme and the
   `transform_enabled` debug override in the preferences file.
2. Seed 6 products: whole chicken, stock vegetables, onions ×3, milk ×2, yogurts ×4, eggs ×6.
3. Transformations:
   1. chicken → breasts ×2 (frozen), wings ×4, thighs ×2 (1 frozen), carcass ×1
   2. carcass + stock vegetables → broth ×4 (3 frozen)
   3. one thigh + one onion + 2 eggs → croquettes ×16 (12 frozen)
4. Reads the Fresh and Frozen lists at the end.

## Run it

```bash
python3 -I .claude/skills/transforma-flow/tf.py <entry> --lang <lang> --out /tmp/transforma-flow  # optional: --serial <serial>
```

Takes ~4–6 minutes per entry. Screenshots (`<entry>-<step>-before-save|after-save.png`, `<entry>-final.png`) go to
`--out`; on failure, `failure.png`. Read them, they are the evidence.

## Report

Per entry: the snackbar of each step (`N ingredients turned into M products`), and the final Fresh/Frozen lists.
Expected totals after the three steps (when the right units are spent): Frozen = broth ×3, croquettes ×12, breasts
×2 (+ 0–1 thighs); Fresh = milk ×2, yogurts ×4, eggs ×4, wings ×4, broth ×1, croquettes ×4, onions ×2 (+ 0–1 thighs).
Report any difference and any step that failed or looked wrong, with the screenshot. Do not fix anything unless asked.

## Notes

- Needs the debug build installed on that device (`./gradlew :app:installDebug`).
- It wipes the app data of `com.alorma.caducity.dev` on the device. Nothing else is touched.
- Never press Back on the home screen: it leaves the app and later taps hit another app.
