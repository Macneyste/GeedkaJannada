# Changelog

## 1.1.0
- Counting sound is now ON by default (tick, chime at goal); can be switched off in Dejimo.
- New **Adkaar** tab: morning (Subax) and evening (Masaa) adhkar with daily completion marks. Quran passages come from the offline dataset.
- Prayer times are computed automatically offline for a chosen Somali city (no location, no network); manual entry still available.
- Prayer reminders use a high-importance channel (sound + vibration).
- Streak challenges: milestones 3/7/14/30/100 days, best streak and progress to the next one.

## 1.0.0
- Scaffold: Gradle (version catalog), wrapper, `.gitignore`.
- Theme: palette, Material 3 typography, dimens, Amiri.
- Data: dhikr model, `SettingsStore` (DataStore), repositories.
- Counter: tree canvas, tap debounce 120ms, goal dialog, reset confirmation, picker + custom goal validation.
- Settings: sound (SoundPool), haptics.
- Quran: offline dataset, list + search, reader (RTL, font size, bookmark, prev/next).
- Prayer: manual times, next-prayer countdown, reminders (alarm / WorkManager), boot receiver, permission flow.
- History: daily totals, weekly chart, streak, per-dhikr.
- Tests: counter logic, goal validation, stats, prayer math, scheduler, parser, store, view models.
