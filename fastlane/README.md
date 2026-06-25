# F-Droid / Fastlane listing metadata

F-Droid builds this listing from `fastlane/metadata/android/<locale>/`:

```
<locale>/
  title.txt              # app name (≤ 50 chars)
  short_description.txt  # one-liner (≤ 80 chars)
  full_description.txt   # long description (limited Markdown)
  images/
    icon.png             # 512×512 listing icon
    phoneScreenshots/    # 1.png, 2.png, … (PENDING — see below)
  changelogs/
    1.txt                # keyed by versionCode (current: 1)
```

Locales provided: `en-US`, `de-DE` (matching the app's in-app languages).

## TODO before submission
- **Screenshots.** Drop PNG/JPG captures into
  `images/phoneScreenshots/` for each locale (1.png, 2.png, …). These must be
  taken from the running app, so they're not generated here. Capture the Today,
  hive detail, Insights, and map screens in both English and German.
- Bump a new `changelogs/<versionCode>.txt` for each release.

The actual F-Droid build recipe (the `.yml` metadata) lives in the separate
`fdroiddata` repository, submitted via merge request — not in this repo.
