---
name: baseline-profile
description: Regenerate the Baseline Profile on an emulator. Leaves the app in the state the generator needs (onboarding done, seed backup imported, Home with products), runs the generation and checks the result. Use before a release when the rule says the profile is stale, or when the main screens changed a lot.
disable-model-invocation: true
---

# Baseline Profile

The generator (`baselineprofile/.../BaselineProfileGenerator.kt`) walks Home → Pantry → Settings. It **fails on purpose** if
the app is on onboarding or Home has no list, so a weak profile is never committed. This skill puts the app in the right
state first. Everything is done by hand with `adb`, text dumps of the screen and screenshots; no scripts.

## Rules

- **Emulator only.** The package is the RELEASE one (`com.alorma.caducity`, no `.dev`) and its data is wiped. Never run
  this on Bernat's phone: it has the Play release installed. With two devices, pass `ANDROID_SERIAL=emulator-XXXX` to every
  `adb -s` and Gradle call.
- The UI must be in **English** (the generator finds the tabs by their description: «Pantry», «Settings»).
- Tap by **text or description** from a fresh dump, never by remembered coordinates.
- The AGP uninstalls the app when the generation ends. That is expected: the next run starts from step 2 again.

## Looking at the screen

```bash
adb -s $S shell uiautomator dump /sdcard/u.xml >/dev/null
adb -s $S exec-out cat /sdcard/u.xml | tr '>' '\n' | grep -E ' text="[^"]+"| content-desc="[^"]+"' \
  | sed -E 's/.* text="([^"]*)".* content-desc="([^"]*)".* clickable="([^"]*)".* bounds="([^"]*)".*/\1 | \2 | \3 | \4/'
```

Each line is `text | description | clickable | [x1,y1][x2,y2]`. Tap the centre of the bounds with
`adb -s $S shell input tap X Y`. Take a screenshot (`adb -s $S exec-out screencap -p > file.png`, then read it) when
the dump is not enough, and at the end as evidence.

## Steps

1. **Device.** `adb devices`; pick the emulator as `$S`. Stop and ask if there is none.
2. **Install and wipe.** `ANDROID_SERIAL=$S ./gradlew :app:installNonMinifiedRelease`, then `adb -s $S shell pm clear com.alorma.caducity`.
3. **Seed file.** `seed-backup.json` (next to this file) has 6 categories and 59 items. Move its dates to today and push it:

   ```bash
   NOW=$(( $(date -u +%s) / 86400 * 86400000 ))
   jq --argjson now "$NOW" '(($now - .exportDate)) as $d | .exportDate = $now | .categories[] |= (.standaloneItems[] |= (.expirationDate += $d | .createdAt += $d) | .products[] |= (.createdAt += $d | .items[] |= (.expirationDate += $d | .createdAt += $d)))' \
     .claude/skills/baseline-profile/seed-backup.json > "$TMPDIR/caducity-seed.json"
   adb -s $S push "$TMPDIR/caducity-seed.json" /sdcard/Download/caducity-seed.json
   ```

4. **Launch.** `adb -s $S shell monkey -p com.alorma.caducity -c android.intent.category.LAUNCHER 1`.
5. **Onboarding (5 steps).** Tap, in order, what the dump shows: «Start» → «Continue with N» (default categories) →
   «Not now» (notifications) → the Transforma step's forward button → on the notice step tick the checkbox (tap the row
   «I have read the notice…») and tap «Accept and start». After it a «Transform» announcement sheet may appear: tap «Not now».
   Home shows when the bar with «Pantry» and «Settings» is there. If a step has a button not listed here, read the
   dump and tap the forward one; decline notifications and analytics.
6. **Import the seed.** «Settings» (description) → swipe up until «Backup» shows → «Backup» → «Restore backup» → in the file
   picker tap «Show roots» (description) → «Downloads» → «caducity-seed.json» → «Restore». Check there is no error.
7. **Check Home.** Back to Home (tap «Home»). The dump must show «N expired products» and the Soon/Fresh counts, and
   not «The pantry is empty». Take a screenshot. Do **not** press Back on Home (it leaves the app).
8. **Generate.** `ANDROID_SERIAL=$S ./gradlew :app:generateReleaseBaselineProfile`. If it fails with «L'app no és a Home»
   or «Home no té llista», the state was lost: go back to step 2.
9. **Check the result.** `git diff --stat app/src/release/generated/baselineProfiles` must show a change; the file
   `baseline-prof.txt` must have thousands of lines and entries for `screen/home/Home`, `screen/pantry` and
   `screen/settings`.
10. Run `./gradlew ktlintCheck`. Commit the two generated files in their own commit.

## Report

Say what changed in the profile (lines before/after), what the generator checked, and anything unusual in the screenshots.
The seed has 59 items; if the real app grows new main screens, add them to the generator and tell Bernat.
