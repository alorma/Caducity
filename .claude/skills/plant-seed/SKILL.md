---
name: plant-seed
description: Leave an emulator with the release app in a known state: onboarding done and the seed backup imported, so Home shows products. Use before anything that needs the app with data on a clean install (benchmarks, profile generation, screenshots, manual checks).
disable-model-invocation: true
---

# Plant seed

Puts the app on an **emulator** in a known state: release package installed, onboarding done, seed backup imported, Home with
products. Everything is done by hand with `adb`, text dumps of the screen and screenshots; no scripts.

## Rules

- **Emulator only.** The package is the RELEASE one (`com.alorma.caducity`, no `.dev`) and its data is wiped. Never run this
  on Bernat's phone: it has the Play release installed. With two devices, use `adb -s emulator-XXXX` in every call (and
  `ANDROID_SERIAL=emulator-XXXX` for Gradle).
- The UI must be in **English**.
- Tap by **text or description** from a fresh dump, never by remembered coordinates.
- Do **not** press Back on Home (it leaves the app and later taps hit another app).

## Looking at the screen

```bash
adb -s $S shell uiautomator dump /sdcard/u.xml >/dev/null
adb -s $S exec-out cat /sdcard/u.xml | tr '>' '\n' | grep -E ' text="[^"]+"| content-desc="[^"]+"' \
  | sed -E 's/.* text="([^"]*)".* content-desc="([^"]*)".* clickable="([^"]*)".* bounds="([^"]*)".*/\1 | \2 | \3 | \4/'
```

Each line is `text | description | clickable | [x1,y1][x2,y2]`. Tap the centre of the bounds with
`adb -s $S shell input tap X Y`. Take a screenshot (`adb -s $S exec-out screencap -p > file.png`, then read it) when the
dump is not enough, and at the end as evidence.

## Steps

1. **Device.** `adb devices`; pick the emulator as `$S`. Stop and ask if there is none.
2. **Install and wipe.** Install the release-type build signed with the debug key
   (`ANDROID_SERIAL=$S ./gradlew :app:installNonMinifiedRelease`, or the APK the task at hand needs), then
   `adb -s $S shell pm clear com.alorma.caducity`.
3. **Seed file.** `seed-backup.json` (next to this file) has 6 categories and 61 items (Flour in grams, Whole milk in millilitres, others in packs, tuppers and cans). Move its dates to today and push it:

   ```bash
   NOW=$(( $(date -u +%s) / 86400 * 86400000 ))
   jq --argjson now "$NOW" '(($now - .exportDate)) as $d | .exportDate = $now | .categories[] |= (.standaloneItems[] |= (.expirationDate += $d | .createdAt += $d) | .products[] |= (.createdAt += $d | .items[] |= (.expirationDate += $d | .createdAt += $d)))' \
     .claude/skills/plant-seed/seed-backup.json > "$TMPDIR/caducity-seed.json"
   adb -s $S push "$TMPDIR/caducity-seed.json" /sdcard/Download/caducity-seed.json
   ```

4. **Launch.** `adb -s $S shell monkey -p com.alorma.caducity -c android.intent.category.LAUNCHER 1`.
5. **Onboarding (5 steps).** Tap, in order, what the dump shows: «Start» → «Continue with N» (default categories) →
   «Not now» (notifications) → «Continue» (Transforma step) → «Continue» (privacy; analytics stay off) → on the notice step tick
   the checkbox (tap the row «I have read the notice…») and tap «Accept and start». After it a «Transform» announcement
   sheet may appear: tap «Not now». Home shows when the bar with «Pantry» and «Settings» is there. If a step has a button
   not listed here, read the dump and tap the forward one; decline notifications and analytics.
6. **Import the seed.** «Settings» (description) → swipe up until «Backup» shows → «Backup» → «Restore backup» → in the file
   picker tap «Show roots» (description) → «Downloads» → «caducity-seed.json» → «Restore». Check there is no error.
7. **Check Home.** Back to Home (tap «Home»). The dump must show «N expired products» and the Soon/Fresh counts, and not
   «The pantry is empty». Take a screenshot.

## Report

Say the device used and show the screenshot of Home with data. If a step failed, say which one and why.
