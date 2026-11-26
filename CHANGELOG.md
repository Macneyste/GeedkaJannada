# Changelog

## 1.3.0 (in progress)
- Tasbeeh rebuilt (feature/tasbeeh): colour jars, mute icon, five original sounds, Xiga button, persisted completion total. Custom goals and the dhikr picker were removed (fixed 33 each).
- Tasbeeh redesigned: 33/33/33 chain with bead string, wheel and three jars; the growing tree was removed.
- Design v3: new palette (Sand, Indigo, Night, Sun, Bronze), Home dashboard, Qibla, Zakat, automatic prayer times via optional coarse location.
- Home screen, Quran Juz tab and reader share / auto-scroll / dark theme already landed (see git log).

## 1.2.0
- Adkaar: five categories (Subax, Masaa, Salaadda kadib, Hurdo, Maalinta) with Somali meaning on every card.
- After-prayer adhkar (istighfar x3, Allaahumma Antas-Salaam, 33/33/33, tahlil, Ayatul Kursi, Ikhlaas/Falaq/Naas).
- Per-card repetition counter (e.g. 33/33) that marks the card done automatically; progress is saved per day.
- Quran text uses the Amiri Quran font.

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
