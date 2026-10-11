# Google Play release notes

No `paths` on purpose: the check below applies to every task, not only when editing the notes.

## When to add a bullet

At the end of every task or feature, say whether it deserves a bullet in the release notes and why.

- **Yes** only if the user can see or measure it. **No** for refactors, docs, CI, dependency bumps, internal fixes, and anything behind a flag or not shipped to users yet.
- Propose it and ask. Do not edit the notes unless the user agrees.
- The notes are rewritten for every release, so a bullet agreed now can wait until the next release is prepared.

## Where they live

The «What's new» text lives in `app/src/main/play/release-notes/<locale>/default.txt` and is uploaded with the AAB by `release.yml` (Gradle Play Publisher). It is user-facing, so the tone matters.

## Tone

Plain, friendly and short. State what changed for the user; no hype and no jokes unless they come naturally. Never make fun of the user.

## Format

- One file per locale, always all three: `en-US`, `ca` and `es-ES`. Same bullets, translated by meaning, not word by word.
- First line: `Version X.Y.Z:` (`Versió`, `Versión`), then one bullet per user-visible change, each starting with `•` followed by a space. Plain text only (no Markdown, no emojis).
- Short bullets (one or two sentences). Play allows 500 characters per locale; keep it well under, around 300.
- End with a bullet for small fixes and internal changes only if there were any, and keep it brief («A few small fixes you won't notice, which is exactly the point»).
- Rewrite the notes for every release: they carry the version number, so never reuse the previous ones.

## Facts only

Every claim must be true and checked. List only what the user can see or measure: look at `git log <last tag>..HEAD` and the diff, and skip dependency bumps, docs, CI and refactors (they go in the «small fixes» bullet at most). Do not promise «faster», «smoother» or «more stable» without having measured it; a real number (for example «1.6 MB of font gone») is better than an adjective. Never mention features that are behind a flag or not shipped.
