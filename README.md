# Geedka Jannada

App Soomaali-ka ugu horreeya ee dikhriga, offline ah (Android, Kotlin, Jetpack Compose, Material 3).
Akoon ma jiro, xayeysiis ma jiro, analytics ma jiro, backend ma jiro, `INTERNET` permission ma jiro, location ma jiro.

## Muuqaallada
- **Tabs**: Quraan, Salaad, Hoyga, Dua, Magacyada ("Kale" waxaa laga furaa Hoyga).
- **Magacyada Alle**: 99 magac (weli lama hubin, eeg [docs/NAMES_REVIEW.md](docs/NAMES_REVIEW.md)).
- **Dua**: 35 duco Quraan ah (macnaha weli lama hubin, eeg [docs/DUA_REVIEW.md](docs/DUA_REVIEW.md)).
- Asturnaanta: [docs/PRIVACY.md](docs/PRIVACY.md).
- **Tasbeeh**: 3 dhikri x 33 (hal taabasho = hal tiro, debounce 120ms), tusbax iyo giraangir, 3 dhalo midabbo leh, 5 dhawaaq oo la dami karo, "Xiga", "Dib u bilow" oo xaqiijin leh, kaarka dhammaystirka (99/99).
- **Dejimaha**: codka (SoundPool, default ON) iyo gariirka (haptic, default OFF).
- **Quraan**: liiska 114 suuradood, raadin, "Sii wad akhriska", akhriska RTL oo leh Amiri, cabbirka qoraalka, calaamad, suurad hore/xigta.
- **Salaad**: waqtiyada si toos ah ayaa loo xisaabiyaa (offline, magaalo Soomaaliyeed la doorto) ama gacanta ayaa lagu geliyaa; salaadda soo socota + waqti ka hadhay; ogeysiin dhawaaq leh (waqtiga / 5 / 10 daqiiqo ka hor).
- **Adkaar**: adkaarta subaxa iyo masaa oo leh calaamad maalinle ah.
- **Caqabadaha silsiladda**: 3 / 7 / 14 / 30 / 100 maalmood isku xigta.
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
La hubiyay (orodsiiyay): `assembleDebug` guul, `testDebugUnitTest` dhammaan unit tests-ka guul, manifest-ka APK-ga ma laha `INTERNET`.
**Aan la hubin**: app-ka lama tijaabin qalab/emulator dhab ah (muuqaalka, SoundPool, gariirka, ogeysiinta iyo alarm-ka way dhisan yihiin laakiin lama arkin shaqeynaya); `androidTest` ma jiro; release build lama saxiixin.

## Decisions
- **Yoolka waa 33 go'an** dhikri kasta (yoolal gaar ah iyo xulashada dikhriga waa la saaray).
- **Alarm**: `setExactAndAllowWhileIdle` haddii `canScheduleExactAlarms()`; haddii kale WorkManager inexact (waxaa lagu sheegay Salaad shaashadda). `SCHEDULE_EXACT_ALARM` waa la dalbanayaa laakiin lama codsado si toos ah.
- **POST_NOTIFICATIONS** waxaa la codsadaa kaliya marka isticmaaluhu shido switch-ka ogeysiinta salaad; haddii la diido switch-ku wuu ahaanayaa OFF oo sharax ayaa la muujiyaa.
- **Taariikh** waxay ku kaydsan tahay DataStore (`day_<taariikh>_<dikhri>`), Room lama isticmaalo.
- **Waqtiyada salaadda**: habka Muslim World League (Fajr 18°, Isha 17°), Asr shadow=1, Africa/Mogadishu (UTC+3). Waa xisaab, ma aha waqtiyada rasmiga ah ee masaajidka; isticmaaluhu wuu beddeli karaa gacanta. Location lama isticmaalo; magaalada waa la doortaa.
- **Dhawaaqa ogeysiinta**: dhawaaqa caadiga ah ee telefoonka (adhaan duuban lama darin, sababtoo ah ma haysanno duubis leh license cad).
- **Adkaarta**: qoraalka Quraanka (Aayatul Kursi, Ikhlaas, Falaq, Naas, Baqara 285–286 iyo 2:201) waxaa laga qaadaa dataset-ka. Adkaarta kale (Salaadda kadib, hurdada, cuntada, Bismillaahil-ladhii laa yadurru, Raadiitu, iwm.) gacanta ayaa lagu qoray ee `data/Adhkar.kt` — **ka hor intaadan daabicin ha dib u eego aqoonyahan diineed**; tarjumaadda Soomaaliga ee kaar kasta waa kuwa app-ka.
- **Caqabadaha silsiladda**: maalin waxay tirsanaysaa haddii dikhri kasta la sameeyay (tiro > 0).
- **Tarjumaad Quraan** lama darin (ikhtiyaari).
- **Calaamadda (bookmark)**: hal calaamad oo dhan app-ka; aayad kasta lambarkeeda ayaa badhan ah.
- **Icons**: waxaa la isticmaalay `material-icons-core` kaliya.
- **Applicationld**: `com.geedkajannada`.

## License
Eeg [LICENSES.md](LICENSES.md): qoraalka Quraanka (Tanzil / quran-json, CC BY), font-ka Amiri (SIL OFL 1.1).
