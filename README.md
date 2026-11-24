# Geedka Jannada

App Soomaali-ka ugu horreeya ee dikhriga, offline ah (Android, Kotlin, Jetpack Compose, Material 3).
Akoon ma jiro, xayeysiis ma jiro, analytics ma jiro, backend ma jiro, `INTERNET` permission ma jiro, location ma jiro.

## Muuqaallada
- **Tirin**: geedka jannada oo koraya marka la taabto (hal taabasho = hal tiro, debounce 120ms), yool, bar horumar, dialog-ka "Hambalyo!", "Dib u bilow" oo xaqiijin leh.
- **Xulashada dikriga**: Subxaanallaah, Alxamdulillaah, Allaahu Akbar; yoolal 33 / 100 iyo yool gaar ah (1–9999).
- **Dejimaha**: codka (SoundPool) iyo gariirka (haptic), labadaba default OFF.
- **Quraan**: liiska 114 suuradood, raadin, "Sii wad akhriska", akhriska RTL oo leh Amiri, cabbirka qoraalka, calaamad, suurad hore/xigta.
- **Salaad**: waqtiyada gacanta ayaa lagu geliyaa, salaadda soo socota + waqti ka hadhay, ogeysiin (waqtiga / 5 / 10 daqiiqo ka hor).
- **Taariikh**: maanta, todobaadka, silsilad, chart 7 maalmood, dhikri kasta.

## Dhis (build)
Waxaa loo baahan yahay JDK 17 iyo Android SDK (platform 35).

```
./gradlew assembleDebug     # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew test              # unit tests
```
Samee `local.properties` oo ku jira `sdk.dir=<jidka SDK>` (ma la commit-gareeyo).

## Qaab-dhismeed
Single Activity + navigation-compose; MVVM (ViewModel + UiState + SharedFlow events); `SettingsStore` (DataStore), `QuranRepository` (assets/quran.json), `PrayerRepository`, `StatsRepository`. Dependencies: AndroidX/Compose/DataStore/Navigation/WorkManager kaliya.

## Hubinta (verification)
La hubiyay (orodsiiyay): `assembleDebug` guul, `testDebugUnitTest` 35 tests oo dhan guul, manifest-ka APK-ga ma laha `INTERNET`.
**Aan la hubin**: app-ka lama tijaabin qalab/emulator dhab ah (muuqaalka, SoundPool, gariirka, ogeysiinta iyo alarm-ka way dhisan yihiin laakiin lama arkin shaqeynaya); `androidTest` ma jiro; release build lama saxiixin.

## Decisions
- **Dikhri kasta yoolkiisa caadiga ah = 33.**
- **Alarm**: `setExactAndAllowWhileIdle` haddii `canScheduleExactAlarms()`; haddii kale WorkManager inexact (waxaa lagu sheegay Salaad shaashadda). `SCHEDULE_EXACT_ALARM` waa la dalbanayaa laakiin lama codsado si toos ah.
- **POST_NOTIFICATIONS** waxaa la codsadaa kaliya marka isticmaaluhu shido switch-ka ogeysiinta salaad; haddii la diido switch-ku wuu ahaanayaa OFF oo sharax ayaa la muujiyaa.
- **Taariikh** waxay ku kaydsan tahay DataStore (`day_<taariikh>_<dikhri>`), Room lama isticmaalo.
- **Tarjumaad Quraan** lama darin (ikhtiyaari).
- **Calaamadda (bookmark)**: hal calaamad oo dhan app-ka; aayad kasta lambarkeeda ayaa badhan ah.
- **Icons**: waxaa la isticmaalay `material-icons-core` kaliya.
- **Applicationld**: `com.geedkajannada`.

## License
Eeg [LICENSES.md](LICENSES.md): qoraalka Quraanka (Tanzil / quran-json, CC BY), font-ka Amiri (SIL OFL 1.1).
