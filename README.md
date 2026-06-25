# Appiary 🐝

A **local-first beekeeping app** for practical field use. Appiary turns hive history into
actionable next steps: open it and instantly see *what to do today*, *what's due soon*,
*which hives are stable*, and **why** the app suggests each action.

No account. No cloud. No network required. A deterministic, explainable rules engine —
not an LLM — owns every recommendation.

---

## Requirements

- **Android Studio** (latest stable) or the Gradle CLI
- **JDK 17+** (the project targets JVM 17; JDK 25 is fine)
- **Android SDK** with API 36 platform + build-tools, and a device/emulator on **API 23+**
- First build downloads dependencies, so an internet connection is needed once

The repo already contains the Gradle wrapper (Gradle 9.4.1) and a version catalog.

## Build & run

```bash
# from the project root
./gradlew :app:assembleDebug          # build the debug APK
./gradlew :app:installDebug           # install on a connected device/emulator
./gradlew :app:testDebugUnitTest      # unit tests: rules, analytics, colony ops + Robolectric DB migrations (106)
```

Or open the project in Android Studio and press **Run**. The app starts **empty** — add an
apiary (Apiaries → +), open it, and add hives (→ +); then log inspections/events from the
central **+** FAB or a hive's quick-action chips. Recommendations appear on Today as soon as
there's data to reason about. (Settings → Debug → *Clear all data* wipes everything.)

> The APK output lands in `app/build/outputs/apk/debug/`.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Room (KSP) · WorkManager ·
ViewModel + Coroutines/Flow · DataStore · CameraX · kotlinx.serialization · Coil ·
Glance (home-screen widget) · osmdroid (offline map) · JUnit.

> **AGP 9 note:** this project relies on AGP's built-in Kotlin. Do **not** add the
> `org.jetbrains.kotlin.android` plugin (it's already on the classpath), and keep
> `android.disallowKotlinSourceSets=false` in `gradle.properties` for KSP/Room.

## Architecture

```
io.github.max_schall.appiary
  data/      db, dao, entity, repository, mapper, backup, settings, seed
  domain/    model (enums), rules (engine + 10 rules), usecase
  ui/        theme, components, navigation, screen/{today,apiaries,hives,tasks,log,photo,settings,analytics,map}, widget, model, state
  worker/    WorkManager reminder + scheduler
  nfc/       optional NFC support
  di/        AppContainer (manual DI — no Hilt)
```

- **Room is the source of truth.** Repositories expose `Flow`s; ViewModels combine them
  into UI state with `stateIn`.
- **Rules engine** (`domain/rules`) is pure and deterministic: `RuleEngine` runs 10 rules
  over each hive's context, sorts by urgency bucket then score, and every recommendation
  carries a plain-language explanation citing the inputs it used. Thresholds are
  configurable in Settings (persisted via DataStore).
- **`RefreshRecommendations`** reruns the engine after any relevant write and reconciles
  results with stored rows, preserving snooze/dismiss state.

## Features

- **Today** — Do now / Due soon / Watchlist / Healthy counters, prioritized recommendation
  list, apiary + bucket filters, per-card act/snooze/dismiss, and a "why" explanation sheet.
- **Apiaries / Hives** — lists with status, next action, search/filter; apiary detail with
  an inspection-round entry point; rich **hive detail** with current-state snapshot,
  next actions, a unified timeline, photos, and quick-log chips.
- **Insights** — a read-only trends screen (from the Today app bar): honey yield per year,
  varroa load over time with caution/action threshold lines, and inspection activity per
  month — all hand-drawn on Compose Canvas (no chart dependency).
- **Colony lifecycle** — record **splits** (spawns a daughter hive with parent→daughter
  lineage), **swarm captures** (new colony at an apiary), and **merges** (folds a weak
  colony into another, archiving it). Operations show on both hives' timelines.
- **Hive weight** — quick scale readings logged per hive, shown on the timeline.
- **Map** — an offline OpenStreetMap (osmdroid) of geolocated apiaries; set an apiary's
  location from its menu (device last-known fix or typed coordinates).
- **Home-screen widget** — a Glance "Today" widget: urgent-task count + the top
  recommendation, tapping through to the app.
- **Fast logging** — inspection (defaults to the "all good" path → save in seconds), mite
  check, treatment, feeding, harvest, and quick notes — reachable from the central + FAB
  or a hive's detail.
- **Tasks** — recommendations + manual tasks (add / complete / reopen).
- **Reminders** — daily summary notification via WorkManager, with quiet hours.
- **Backup** — full JSON export/import + inspections CSV via the system file picker.
- **Photos** — optional CameraX capture attached to a hive.
- **NFC** — optional: tap a tag to open its hive; link a tag from the hive's menu.
- **Settings** — language (System / English / Deutsch), rule thresholds, reminders, **seasonal
  profile editor**, dynamic color, NFC, backup, and clear-all-data.
- **Seasonal profile** — editable months (active season / harvest / winter prep) and
  hemisphere, or **derive from a location**: enter coordinates or use the device's last-known
  GPS fix, and the season is computed **offline from latitude**, refined with Open-Meteo
  monthly temperatures when there's a connection (no API key; silent offline fallback).

## Seasonal & climate intelligence

Beyond per-hive rules, Appiary derives a per-apiary **season model** from the apiary's
coordinates and gives proactive, location-aware guidance — all deterministic and explainable,
distilled from beekeeping literature (Central-European consensus: Wernet, Timme, Pfeifenberger).

- **Climate classification** (`domain/season/ClimateClassifier`) — Köppen–Geiger group + USDA
  hardiness zone from monthly temperature/precipitation normals (Open-Meteo, cached for offline
  reuse); offline latitude heuristic as a fallback.
- **Annual task calendar** (`PhenologyEngine` + `SeasonalTaskRule`) — five phases (winter →
  spring build-up → swarm & flow → harvest → autumn prep) with proactive "what to do now" advice.
- **Nectar flow / dearth** (`BloomCalendar` + `FlowDetector` + `NectarFlowRule`/`DearthRule`) —
  region bloom calendars detect the main flow (super up + swarm control before it) and the
  dearth (robbing/feeding/treatment window after), incl. the Central-European August *Trachtlücke*.
- **Weather timing** (`WeatherRepository` + weather rules) — a 7-day Open-Meteo forecast adds
  good-inspection-window, treatment-heat (formic-acid risk), and cold-snap warnings.

**Privacy / offline:** only coordinates are ever sent (to Open-Meteo — free, keyless, no
account, no Appiary backend). Climate is cached for offline reuse; the weather layer is
online-only and simply produces nothing without a connection — the rest of the app is unaffected.
Thresholds and the seasonal profile are editable in Settings; the live phase, flow state, and
climate descriptor are shown on the Seasonal Profile screen.

## Localization

Fully localized in **English and German**, switchable in-app (Settings → Language) via
AndroidX per-app locales — including the generated recommendation titles, reasons, and
explanations. Static UI uses Android string resources (`values/`, `values-de/`); the rules
engine stays pure Kotlin and picks an `English`/`German` `RuleStrings` implementation by the
active locale, regenerating recommendations on language change.

## Design

A calm "modern field notebook" aesthetic: moss-green primary, warm-paper surfaces, honey
amber for due-soon, muted berry for errors — tuned for outdoor readability with a coherent
light **and** dark theme. Status is always conveyed by icon + label, never color alone.

## Testing

The recommendation engine is covered by JUnit tests (`app/src/test`): one suite per rule,
plus urgency bucketing/ordering, the snooze/dismiss reconciler, hive-state derivation, and
an end-to-end check that representative hives produce the expected recommendations.

```bash
./gradlew :app:testDebugUnitTest
```

## License

Copyright (C) 2026 Max Schall

Appiary is free software, licensed under the **GNU General Public License v3.0**
(or later) — see [LICENSE](LICENSE). You may use, study, share, and modify it;
derivative works must remain free software under the same license.
