# Testiranje

Aplikacija ima tri nivoa testova:

| Nivo | Gdje | Koliko | Pokretanje |
|---|---|---|---|
| JVM (domain, repo, Room u memoriji, Compose pod Robolectric-om) | `app/src/test` | 266 | `./gradlew :app:testDebugUnitTest` |
| E2E na emulatoru ili tabletu (prava aplikacija, prava baza, pravi dodiri) | `app/src/androidTest/.../e2e` | 37 | `./gradlew :app:connectedDebugAndroidTest` |
| Ručna provjera preko adb-a (pravo gašenje procesa) | release APK na emulatoru | — | opis ispod |

## E2E testovi

- Pokreću se na AVD-u `Tablet_1280x800` (1280×800, landscape).
- Debug verzija ima `applicationId` sa nastavkom `.debug`. Zato se instalira **pored** release verzije i ne dira njenu bazu.
- Orchestrator sa `clearPackageData` briše podatke prije svakog testa. Svaki test zato počinje kao prva instalacija: 5 mjera, 4 radnika i probni PIN 123456.
- Testovi klikću po tekstu koji korisnik vidi. Ako tekst na ekranu nije onakav kakav test očekuje, poruka greške ispisuje cijeli ekran.
- Kad Gradle javi „Unknown API Level“ ili „No compatible devices“, pomaže `adb kill-server && adb start-server`.

Jedna klasa: `-Pandroid.testInstrumentationRunnerArguments.class=dev.nextpixel.cafebar919.e2e.SankE2ETest`

| Klasa | Scenariji |
|---|---|
| `PinE2ETest` (7) | prvi start i S0; pogrešan PIN (PIN-01) pa tačan, baner probnog PIN-a; „Odustani“; 10 pogrešnih (PIN-02), tastatura onemogućena, tačan PIN ne otvara; „Zaključaj sada“; promjena PIN-a (zabranjen, nepoklapanje, stari ne radi, novi radi); V7 |
| `SankE2ETest` (19) | otvaranje noći (samo izabrani i Šank); ispomoć: validacija („Upiši ime.“, ime radnika, „Šank“), dodavanje, prikaz na S1; „Odustani“ u ispomoći; T2; tura od izbora do zbira na pločici i u bazi (litre kao snimak); tri ture, jedna na Šank; dvostruki dodir na „Potvrdi“ daje jednu turu; stepper; poništavanje u traci i iz toasta; pretraga bez kvačica, T1, „Očisti pretragu“; čip kategorije; nacrt preživljava ponovno pravljenje ekrana; KEEP_SCREEN_ON; promjena uloge u toku noći ne mijenja otvorenu noć; mjera skinuta sa pića u nacrtu; unos ture za vrijeme PIN blokade; isključen radnik se ne nudi na S0 |
| `PodesavanjaE2ETest` (10) | meni kroz podešavanja se vidi na šanku; kategorija: duplikat i prazno; ARTIKAL-01 (novi i preimenovanje); isključene kategorije i mjere se ne nude; mjera i kategorija u upotrebi se ne isključuju; litre van opsega; novi radnik na S0; rok poništavanja iz V6 važi na šanku; zaključavanje iz V6 mijenja rok sesije; posljednji razlog ostaje; „Nazad na unos“ zaključava |
| `BazaE2ETest` (1) | oštećen fajl baze daje BAZA-01, a fajl ostaje netaknut (isto provjerava i JVM test `OstecenaBazaTest`) |

## Ručna provjera: pravo gašenje procesa

Test u `androidTest` radi u istom procesu kao aplikacija, pa ne može da je ubije. Zato se ova provjera radi spolja, na release aplikaciji:

1. na S1 izaberi konobara i dodaj piće;
2. `adb shell am force-stop dev.nextpixel.cafebar919`;
3. ponovo pokreni aplikaciju.

Izabrani konobar i stavke nacrta moraju ostati. Provjereno 22. 11. 2026: oboje ostaje.

## Otvoreno

1. **BAZA-01, ispravljeno 22. 11. 2026.** Room je ranije oštećenu bazu brisao (`onCorruption`) i pravio novu, praznu. Sada `data/BezBrisanjaBaze.kt` samo zapiše oštećenje u log. Aplikacija pokaže BAZA-01, a fajl ostaje za pregled. Testovi: `OstecenaBazaTest` (JVM) i `BazaE2ETest` (uređaj).
2. Bez E2E testa (nemoguće ili nepouzdano na emulatoru):
   - nestanak struje;
   - pravi StatFs sa malo prostora (PROSTOR-01 je pokriven JVM testovima);
   - prevlačenje ručicom (redoslijed);
   - prava softverska tastatura;
   - instalacija nove verzije preko stare sa pravim podacima (radi se ručno pri svakom izdanju, vidi `IZDAVANJE.md`).
3. Rupe u JVM testovima koje E2E ne zatvara:
   - razlog ≤ 32 znaka;
   - „u zbiru“ ≤ 8 znakova;
   - predugo ime radnika i ispomoći;
   - granice V6 (rok 0, istorija 90 dana, zaključavanje 10 min).
4. `ArtikliTest.iskljucivanjeTraziPotvrdu` povremeno padne sa „connection pool has been closed“: baza u memoriji se zatvori dok posao još radi. Pri ponovnom pokretanju prolazi. Problem je u samom testu i postojao je i prije.
5. Faza 3 (S1.3, S2, S1.2, S3, brisanje starih noći) još nije napravljena, pa nema ni testova.
