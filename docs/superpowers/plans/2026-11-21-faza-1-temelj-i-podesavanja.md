# Faza 1 — temelj i podešavanja: plan izrade

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Cilj:** APK koji se pokreće na tabletu 1280×800, pravi bazu sa početnim podacima, a vlasniku poslije PIN-a otvara sva podešavanja (V0–V7) koja rade nad pravom bazom.

**Arhitektura:** jedan Gradle modul `app`, paket `dev.nextpixel.cafebar919`. `domain/` je čist Kotlin (pravila, formati, PIN), `data/` su Room entiteti i DAO-i, `repo/` su repozitoriji (jedina mjesta koja pišu u bazu), `ui/` je Compose (tema, komponente, ekrani sa ViewModel-ima). `AppContainer` ručno povezuje sve.

**Tehnologije:** AGP 9.5.2 (ugrađeni Kotlin), Kotlin 2.4.21, Compose BOM 2026.11.01 (Material 3 + material-icons-extended 1.7.8), Room 2.8.5 + KSP 2.3.14, Navigation Compose 2.10.2, Lifecycle 2.11.1, Activity Compose 1.13.0, kotlinx-serialization-json 1.13.0, sh.calvin.reorderable 3.1.0, testovi: JUnit 4.13.2, Robolectric 4.18.1, kotlinx-coroutines-test 1.11.0, Compose ui-test-junit4 (iz BOM-a). Gradle wrapper: najnovija verzija koju traži AGP 9.5.2 (tabela kompatibilnosti AGP-a). JDK 21.

**Spec:** `docs/superpowers/specs/2026-11-21-android-aplikacija-design.md` (u daljem tekstu **spec**). Tekstovi i izgled svakog ekrana: `docs/superpowers/specs/2026-11-21-paper-inventar.md` (**inventar**). Tačne vrijednosti (veličine, boje, razmaci) uzimaju se iz Papera alatima `get_jsx` (sa `includeNodeIds: false`) i `get_computed_styles`, fajl `01M7Z35WJSKRE36Z9P9VQBPWWA`. Slike služe samo za provjeru.

## Opšta ograničenja

- Svi tekstovi u UI su na srpskom, ijekavica, latinica, doslovno iz inventara, osim gdje spec poglavlje 7 kaže drugačije. Identifikatori u kodu su srpski bez dijakritika (`artikal`, `mjera`, `noc`).
- PIN ima **6 cifara**. Početni PIN je **123456** (`pin_probni = true`).
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`, `applicationId "dev.nextpixel.cafebar919"`, `versionCode 1`, `versionName "1.0"`.
- Manifest: `android:screenOrientation="sensorLandscape"`, `android:allowBackup="false"`, bez `INTERNET` dozvole.
- Tamna tema iz Paper tokena (spec 3). Fontovi Inter (UI) i Cinzel (samo znak „919 CAFFE BAR“) su u `res/font`.
- Dugmad i redovi koji se dodiruju imaju najmanje 56 dp visine (`--spacing-touch`).
- Nema izmišljenih podataka: u bazu ulaze samo početni podaci iz spec 6.2. Testovi prave svoje podatke u bazi u memoriji.
- Room: `exportSchema = true`, šema u `app/schemas/`. **Nikad** `fallbackToDestructiveMigration`.
- Vrijeme se čita samo iz ubrizganog `java.time.Clock`.
- Svi testovi rade na JVM-u (`./gradlew testDebugUnitTest`), Compose testovi preko Robolectric-a. Emulator služi samo za ručnu provjeru.
- Okruženje za sve komande: `export JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home` i `export ANDROID_HOME=$HOME/Library/Android/sdk`.

## Na šta paziti pri pregledu

1. **Ponovno uključivanje isključenog artikla** čija je kategorija ili mjera u međuvremenu isključena, ili čiji naziv sada ima drugi aktivan artikal. Očekuje se odbijanje sa jasnom porukom, nikad aktivan artikal sa isključenom mjerom ili duplim nazivom. Test: Task 4, `ukljuciArtikal_odbijaKadJeMjeraIskljucena` i `ukljuciArtikal_odbijaDupliNaziv`.
2. **Naziv sa razmakom ili drugim velikim slovima** („ jack daniel’s “ pored „Jack Daniel’s“). Očekuje se ARTIKAL-01. Naziv se čuva bez razmaka na krajevima. Test: Task 4, `sacuvajArtikal_duplikatBezObziraNaVelicinuIRazmake`.
3. **Restart aplikacije za vrijeme blokade PIN-a.** Blokada i brojač pogrešnih pokušaja ostaju, jer su u bazi. Test: Task 5, `blokadaPreziviNovuInstancuServisa`.
4. **Aplikacija ode u pozadinu dok su podešavanja otključana** (gašenje ekrana, dugme Home). Po povratku su podešavanja zaključana. Test: Task 7, `sesija_zakljucavaKadAplikacijaOdeUPozadinu`.
5. **Litre upisane sa tačkom ili sa više decimala** („0.7“, „0,705“). Prihvata se zarez ili tačka i zaokružuje na 2 decimale. Van opsega 0,01–5,00 prikazuje se greška ispod polja. Test: Task 2, `parsirajLitre_prihvataTackuIZarez`.

---

## Mapa fajlova

```
CafeBar919-Android/
  settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml, gradlew…
  .gitignore
  app/build.gradle.kts
  app/schemas/                         (Room šema, u gitu)
  app/src/main/AndroidManifest.xml
  app/src/main/res/font/               inter_*.ttf, cinzel_*.ttf
  app/src/main/res/mipmap-*/, drawable/ ikona aplikacije
  app/src/main/java/dev/nextpixel/cafebar919/
    CafeBarApp.kt                      Application, drži AppContainer
    MainActivity.kt                    edge-to-edge, imerzivno, postavlja sadržaj
    AppContainer.kt                    baza, repozitoriji, Clock, PinServis
    domain/Format.kt                   brojevi, litre, množina, zbir mjera, odbrojavanje
    domain/Validacija.kt               pravila za nazive, litre, razloge
    domain/Pin.kt                      heš, zabranjeni PIN-ovi, stanje blokade
    domain/Ishod.kt                    Ishod i Odbijanje
    data/Entiteti.kt                   svi @Entity iz spec 6.2
    data/Konverteri.kt                 LocalDate i Instant
    data/dao/*.kt                      KategorijaDao, MjeraDao, ArtikalDao, RadnikDao, RazlogDao, PostavkeDao, NocDao
    data/AppDatabase.kt
    data/PocetniPodaci.kt              početni podaci (onCreate)
    data/OtvaranjeBaze.kt              stanje Ucitavanje/Spremna/Greska (BAZA-01)
    repo/PodesavanjaRepo.kt            kategorije, mjere, artikli, radnici, razlozi, postavke
    repo/PinServis.kt                  provjera, blokada, promjena PIN-a
    ui/theme/Boje.kt, Tipografija.kt, Oblici.kt, Razmaci.kt, Tema.kt
    ui/components/…                    Dugmad.kt, Cipovi.kt, Prekidac.kt, Stepper.kt, PinUnos.kt, Baner.kt,
                                       Toast.kt, Prozor.kt, Polje.kt, Znak919.kt, SegmentiraniIzbor.kt
    ui/greska/BazaGreskaEkran.kt       T6
    ui/pocetni/PocetniEkran.kt         privremeni početni ekran faze 1
    ui/podesavanja/PodesavanjaSesija.kt
    ui/podesavanja/PinProzor.kt (V0), Ljuska.kt, PodesavanjaNav.kt
    ui/podesavanja/artikli/  (V1, V1.1, V1.2 + ViewModel)
    ui/podesavanja/kategorije/ (V5), mjere/ (V4), ljudi/ (V2), pravila/ (V6), pin/ (V3), oaplikaciji/ (V7)
  app/src/test/java/dev/nextpixel/cafebar919/…   testovi u istim paketima
```

## Zajednički tipovi (koriste ih svi taskovi)

```kotlin
// domain/Ishod.kt
sealed interface Ishod {
    data object Ok : Ishod
    data class Odbijeno(val razlog: Odbijanje) : Ishod
}
enum class Odbijanje {
    NAZIV_PRAZAN, NAZIV_PREDUG, NAZIV_POSTOJI,   // NAZIV_POSTOJI za artikal = ARTIKAL-01
    BEZ_KATEGORIJE, BROJ_MJERA,                  // artikal: tačno 1 kategorija, 1–3 mjere
    U_UPOTREBI,                                  // mjera/kategorija koju koristi aktivan artikal
    KATEGORIJA_ISKLJUCENA, MJERA_ISKLJUCENA,     // pri uključivanju artikla
    NATPIS_PRAZAN, NATPIS_PREDUG, U_ZBIRU_PRAZNO, U_ZBIRU_PREDUGO, LITRE_VAN_OPSEGA,
    VRIJEDNOST_VAN_OPSEGA                        // stepper V6
}
```

---

### Task 1: Okruženje, kostur projekta i tema

**Fajlovi:**
- Napraviti: sve Gradle fajlove, `.gitignore`, `AndroidManifest.xml`, `CafeBarApp.kt`, `MainActivity.kt`, `ui/theme/*`, `ui/components/Znak919.kt`, `ui/pocetni/PocetniEkran.kt`, `res/font/*`, ikonu aplikacije
- Test: `app/src/test/java/dev/nextpixel/cafebar919/ui/theme/TemaTest.kt`

**Interfejsi:**
- Daje: `CafeBarTema(content: @Composable () -> Unit)`; `object Boje` sa svim `--color-*` tokenima kao `Color` (imena camelCase: `bg`, `surface`, `line`, `ink`, `muted`, `primary`, `primaryTint`, `warning`, `warningTint`, `success`, `successTint`, `danger`, `dangerTint`, `placeholder`, `disabledInk`, `disabledBg`, `primaryPressed`, `pressed`, `warningLine`, `dangerLine`, `infoLine`, `neutralLine`, `toastAction`, `tile`, `onPrimary`); `object Razmaci` (`s1=4.dp … s8=32.dp, dodir=56.dp`); `object Oblici` (`sm=8, md=12, lg=16, pill`); `object Tekst` sa `TextStyle`-ovima `caption 12, label 13, body 16, section 20, title 28, display 40` (Inter; caps stilovi sa `letterSpacing 0.08em`, naslovi `-0.02em`); `Znak919(modifier)` (zlatni krug „919“ + „CAFFE BAR“, Cinzel); `PocetniEkran(onPodesavanja: () -> Unit)`.

- [ ] **Korak 1: Instalirati alate**

```bash
brew install openjdk@21 gradle
brew install --cask android-commandlinetools
export JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=$HOME/Library/Android/sdk
sdkmanager --sdk_root=$ANDROID_HOME --list_installed
avdmanager create avd -n Tablet_1280x800 -k "system-images;android-36.1;google_apis_playstore;arm64-v8a" -d pixel_tablet
```
Očekivano: `java -version` → 21, AVD `Tablet_1280x800` postoji (`emulator -list-avds`). Pixel Tablet je 2560×1600 px na 320 dpi, odnosno 1280×800 dp.

- [ ] **Korak 2: Git i kostur projekta**

`git init` u `CafeBar919-Android/`. Prvi commit su postojeći `docs/`. Zatim Gradle projekat sa verzijama iz zaglavlja u `libs.versions.toml`. Pluginovi su `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, `com.google.devtools.ksp`, `org.jetbrains.kotlin.plugin.serialization` i `androidx.room`. `kotlin-android` se ne koristi, jer AGP 9 ima ugrađen Kotlin. Wrapper se pravi sa `gradle wrapper --gradle-version <verzija za AGP 9.5.2>`. U `.gitignore` idu `build/`, `.gradle/`, `local.properties`, `*.jks`, `*.keystore` i `.idea/`. `testOptions.unitTests.isIncludeAndroidResources = true`. Room: `room { schemaDirectory("$projectDir/schemas") }`.

- [ ] **Korak 3: Fontovi i ikona**

Inter (variable ili 400/500/600/700) i Cinzel (400/600) se skidaju iz repozitorija `github.com/google/fonts` (`ofl/inter`, `ofl/cinzel`) u `res/font/` i dodaje se `OFL.txt`. Ikona je adaptivna: tamna podloga `#191919`, zlatni krug `#D3B574`, „919“ Cinzel tamnim slovima, isto kao znak iz Papera (sloj „919“ na S1).

- [ ] **Korak 4: Test teme**

```kotlin
@RunWith(RobolectricTestRunner::class)
class TemaTest {
    @Test fun tokeniOdgovarajuPaperu() {
        assertEquals(Color(0xFF191919), Boje.bg)
        assertEquals(Color(0xFFD3B574), Boje.primary)
        assertEquals(Color(0xFFF5F1E6), Boje.ink)
        assertEquals(56.dp, Razmaci.dodir)
        assertEquals(16.sp, Tekst.body.fontSize)
    }
    @get:Rule val compose = createComposeRule()
    @Test fun pocetniEkranPrikazujePodesavanja() {
        compose.setContent { CafeBarTema { PocetniEkran(onPodesavanja = {}) } }
        compose.onNodeWithText("Podešavanja").assertIsDisplayed()
    }
}
```

- [ ] **Korak 5: Pokrenuti test, mora pasti** — `./gradlew testDebugUnitTest --tests '*TemaTest*'` → FAIL (nema `Boje`).

- [ ] **Korak 6: Implementacija.** Tema po tokenima iz Papera (`get_tokens`). `MainActivity` je edge-to-edge, sakriva sistemske trake (imerzivno, `WindowInsetsControllerCompat`, `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`) i prikazuje `PocetniEkran`. `PocetniEkran` je privremen: gornja traka 72 dp kao na S0 (znak, današnji datum u formatu „Subota 21. 11.“, desno outline dugme sa katancem „Podešavanja“), a u sredini tekst „Unos tura dolazi u sljedećoj fazi izrade.“ Bez izmišljenih podataka.

- [ ] **Korak 7: Testovi prolaze, APK se pravi** — `./gradlew testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL.

- [ ] **Korak 8: Ručna provjera.** Pokrenuti emulator `Tablet_1280x800`, zatim `adb install -r app/build/outputs/apk/debug/app-debug.apk` i snimak ekrana `adb exec-out screencap -p > /tmp/…png`. Ekran mora biti horizontalan i taman, a znak isti kao u Paperu.

- [ ] **Korak 9: Commit** — `git commit -m "Kostur projekta, tema iz Paper tokena, fontovi"`

---

### Task 2: Formati i validacija (domain)

**Fajlovi:** napraviti `domain/Format.kt`, `domain/Validacija.kt`, `domain/Ishod.kt`. Testovi u `test/…/domain/FormatTest.kt` i `ValidacijaTest.kt`.

**Interfejsi (daje):**
```kotlin
object Format {
    fun mnozina(n: Int, jedan: String, dva: String, pet: String): String   // samo oblik riječi
    fun litre(l: Double): String                // "0,03", "1,00" — 2 decimale, zarez
    fun litreSaJedinicom(l: Double): String     // "3,14 l"
    data class MjeraBroj(val uZbiru: String, val redoslijed: Int, val kolicina: Int)
    fun zbirMjera(stavke: List<MjeraBroj>): String   // "kom 22 · doza 26 · bok. 4"; prazno -> "još ništa"
    fun odbrojavanje(sekundi: Int): String      // "2 min", "1 min 40 s", "45 s"
    fun datumNoci(d: LocalDate): String         // "Subota 21. 11."
    fun brojac(n: Int, jedan: String, dva: String, pet: String): String   // "15 pića", "1 kategorija"
}
object Validacija {
    const val NAZIV_ARTIKLA_MAX = 24; const val BROJAC_OD = 20
    const val NAZIV_KATEGORIJE_MAX = 16; const val NAZIV_MJERE_MAX = 16
    const val NATPIS_MAX = 8; const val U_ZBIRU_MAX = 8; const val IME_MAX = 20; const val RAZLOG_MAX = 32
    fun ocisti(naziv: String): String                       // trim + jedan razmak između riječi
    fun kljucNaziva(naziv: String): String                  // ocisti + lowercase(Locale("sr")) — za poređenje
    fun parsirajLitre(tekst: String): Double?               // "0,7"/"0.7" -> 0.70; prazno -> null; nevažeće -> NaN
    fun provjeriLitre(l: Double?): Ishod                    // null je OK (komad); 0.01..5.00
    fun provjeriNaziv(naziv: String, max: Int): Ishod       // NAZIV_PRAZAN / NAZIV_PREDUG
}
```
Pravilo množine: `n%10==1 && n%100!=11` daje `jedan`; `n%10 in 2..4 && n%100 !in 12..14` daje `dva`; inače `pet`.

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@Test fun mnozina_srpskaPravila() {
    val f = { n: Int -> "$n " + Format.mnozina(n, "piće", "pića", "pića") }
    assertEquals("1 piće", f(1)); assertEquals("2 pića", f(2)); assertEquals("11 pića", f(11)); assertEquals("21 piće", f(21))
    assertEquals("1 kategorija", Format.brojac(1, "kategorija", "kategorije", "kategorija"))
    assertEquals("3 kategorije", Format.brojac(3, "kategorija", "kategorije", "kategorija"))
    assertEquals("12 kategorija", Format.brojac(12, "kategorija", "kategorije", "kategorija"))
    assertEquals("4 stalna radnika", Format.brojac(4, "stalni radnik", "stalna radnika", "stalnih radnika"))
}
@Test fun litre_zarezIDvijeDecimale() {
    assertEquals("0,03", Format.litre(0.03)); assertEquals("0,70", Format.litre(0.7)); assertEquals("3,14 l", Format.litreSaJedinicom(3.14))
}
@Test fun zbirMjera_kratkoImePaBroj_poRedoslijedu_bezNula() {
    val s = listOf(Format.MjeraBroj("bok.", 2, 4), Format.MjeraBroj("kom", 0, 22), Format.MjeraBroj("doza", 1, 26), Format.MjeraBroj("boca 1 l", 4, 0))
    assertEquals("kom 22 · doza 26 · bok. 4", Format.zbirMjera(s))
    assertEquals("još ništa", Format.zbirMjera(emptyList()))
}
@Test fun odbrojavanje() {
    assertEquals("2 min", Format.odbrojavanje(120)); assertEquals("1 min 40 s", Format.odbrojavanje(100)); assertEquals("45 s", Format.odbrojavanje(45))
}
@Test fun datumNoci() { assertEquals("Subota 21. 11.", Format.datumNoci(LocalDate.of(2026, 11, 21))) }
@Test fun parsirajLitre_prihvataTackuIZarez() {
    assertEquals(0.70, Validacija.parsirajLitre("0,7")!!, 1e-9); assertEquals(0.70, Validacija.parsirajLitre("0.7")!!, 1e-9)
    assertEquals(0.71, Validacija.parsirajLitre("0,705")!!, 1e-9); assertNull(Validacija.parsirajLitre("  "))
    assertTrue(Validacija.parsirajLitre("abc")!!.isNaN())
    assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(0.0))
    assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(5.01))
    assertEquals(Ishod.Ok, Validacija.provjeriLitre(null))
}
@Test fun naziv() {
    assertEquals("Jack Daniel’s", Validacija.ocisti("  Jack   Daniel’s "))
    assertEquals(Validacija.kljucNaziva("JACK daniel’s"), Validacija.kljucNaziva(" Jack Daniel’s"))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PRAZAN), Validacija.provjeriNaziv("   ", 24))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PREDUG), Validacija.provjeriNaziv("x".repeat(25), 24))
}
```
Dani u sedmici za `datumNoci`: Ponedjeljak, Utorak, Srijeda, Četvrtak, Petak, Subota, Nedjelja.

- [ ] **Korak 2:** `./gradlew testDebugUnitTest --tests '*domain*'` → FAIL.
- [ ] **Korak 3:** Implementacija po interfejsu. `zbirMjera` sabira iste `uZbiru` (uz isti `redoslijed`), preskače nule i sortira po `redoslijed`. Zaokruživanje litara je `BigDecimal.setScale(2, HALF_UP)`.
- [ ] **Korak 4:** Isti test → PASS.
- [ ] **Korak 5: Commit** — `"Formati, množina i validacija"`

---

### Task 3: Room baza, početni podaci, BAZA-01

**Fajlovi:** napraviti `data/Entiteti.kt`, `data/Konverteri.kt`, `data/dao/*.kt`, `data/AppDatabase.kt`, `data/PocetniPodaci.kt`, `data/OtvaranjeBaze.kt`, `AppContainer.kt`, `ui/greska/BazaGreskaEkran.kt`. Izmijeniti `MainActivity.kt`. Testovi u `test/…/data/BazaTest.kt` i `OtvaranjeBazeTest.kt`.

**Interfejsi (daje):**
- Entiteti, tačno kolone iz spec 6.2 (uključujući `mjera.u_zbiru` i snimke u `stavka_ture`), sa ovim tipovima:
  - `Instant` kao `Long` (ms), `LocalDate` kao `Long` (epochDay);
  - enumi `Uloga { KONOBAR, ZA_SANKOM }` i `VrstaPrimaoca { KONOBAR, ZA_SANKOM, ISPOMOC, SANK }` kao `String`;
  - `tura.kljuc` kao `String` sa `@Index(unique = true)`;
  - strani ključevi sa `ON DELETE CASCADE` za `noc → noc_primalac/tura` i `tura → stavka_ture`;
  - indeksi na svim stranim ključevima.
- Imena entiteta: `KategorijaE`, `MjeraE`, `ArtikalE`, `ArtikalMjeraE`, `RadnikE`, `RazlogE`, `PostavkeE` (id uvijek 1), `NocE`, `NocPrimalacE`, `TuraE`, `StavkaTureE`, `NacrtTureE`.
- `PostavkeE(id=1, rokBezPinaS=60, istorijaDana=30, zakljucavanjeS=120, ispomocDozvoljena=true, pinHes: String, pinSo: String, pinProbni=true, pinPogresnihZaredom=0, pinBlokiranDo: Long?=null)`.
- `AppDatabase.napravi(context: Context, imeFajla: String? = "cafebar919.db"): AppDatabase`. Za `null` pravi bazu u memoriji. Uvijek dodaje `PocetniPodaci.callback`.
- `PocetniPodaci.callback: RoomDatabase.Callback`. `onCreate` upisuje početne podatke iz spec 6.2 sirovim SQL-om, u jednoj transakciji. PIN heš se računa preko `Pin.hesiraj("123456")` (Task 5 daje `Pin`; dok Task 5 ne postoji, Task 3 pravi `domain/Pin.kt` samo sa funkcijom `hesiraj`, a Task 5 je proširuje).
- `class OtvaranjeBaze(otvori: suspend () -> AppDatabase) { val stanje: StateFlow<StanjeBaze>; suspend fun pokusaj() }` sa `sealed interface StanjeBaze { Ucitavanje; Spremna(db); Greska(e) }`. `pokusaj()` zove `otvori()` i `db.openHelper.writableDatabase` i hvata svaki `Exception`.
- `class AppContainer(context, clock: Clock = Clock.systemDefaultZone())` drži `otvaranjeBaze`. Repozitorije pravi tek kad je baza `Spremna` (`fun repo(): PodesavanjaRepo`, `fun pin(): PinServis`; Taskovi 4 i 5).
- `BazaGreskaEkran(onPonovo: () -> Unit)`: tekstovi iz T6, uz izmjenu iz spec 7 #20 („Pomoć: NextPixel · +387 66 603 900“ umjesto linka „Javi vlasniku.“).
- DAO-i daju `Flow` liste sortirane po `redoslijed` i `suspend` upise. DAO metode koje trebaju Tasku 4 su navedene tamo.

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@RunWith(RobolectricTestRunner::class)
class BazaTest {
    lateinit var db: AppDatabase
    @Before fun setUp() { db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null) }
    @After fun tearDown() = db.close()

    @Test fun pocetneMjere() = runTest {
        val m = db.mjeraDao().sve().first()
        assertEquals(listOf("Komad","Doza","Bokal","Boca 0,7","Boca 1 l"), m.map { it.naziv })
        assertEquals(listOf("Komad","0,03","Bokal","Boca","Boca 1 l"), m.map { it.natpis })
        assertEquals(listOf("kom","doza","bok.","boca 0,7","boca 1 l"), m.map { it.uZbiru })
        assertEquals(listOf(null, 0.03, 0.25, 0.70, 1.00), m.map { it.litara })
        assertTrue(m.all { it.aktivna })
    }
    @Test fun pocetniRadnici() = runTest {
        val r = db.radnikDao().svi().first()
        assertEquals(listOf("Vuleta" to Uloga.KONOBAR, "Srđan" to Uloga.KONOBAR, "Miloš" to Uloga.ZA_SANKOM, "Smitran" to Uloga.ZA_SANKOM), r.map { it.ime to it.uloga })
    }
    @Test fun pocetniRazloziBezVraceno() = runTest {
        assertEquals(listOf("Pogrešan konobar","Pogrešno piće","Pogrešna količina","Uneseno dvaput"), db.razlogDao().svi().first().map { it.tekst })
    }
    @Test fun nemaKategorijaNiArtikala() = runTest {
        assertTrue(db.kategorijaDao().sve().first().isEmpty()); assertTrue(db.artikalDao().svi().first().isEmpty())
    }
    @Test fun pocetnePostavke() = runTest {
        val p = db.postavkeDao().jedne()
        assertEquals(60, p.rokBezPinaS); assertEquals(30, p.istorijaDana); assertEquals(120, p.zakljucavanjeS)
        assertTrue(p.ispomocDozvoljena); assertTrue(p.pinProbni); assertTrue(Pin.provjeri("123456", p.pinHes, p.pinSo))
    }
}
class OtvaranjeBazeTest {
    @Test fun greskaPriOtvaranju_dajeGreska_paPonovoSpremna() = runTest {
        var puta = 0
        val o = OtvaranjeBaze { if (puta++ == 0) throw IllegalStateException("x") else AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null) }
        o.pokusaj(); assertTrue(o.stanje.value is StanjeBaze.Greska)
        o.pokusaj(); assertTrue(o.stanje.value is StanjeBaze.Spremna)
    }
}
```
`OtvaranjeBazeTest` takođe radi sa `@RunWith(RobolectricTestRunner::class)`.

- [ ] **Korak 2:** `./gradlew testDebugUnitTest --tests '*data*'` → FAIL.
- [ ] **Korak 3:** Implementacija po interfejsu. `MainActivity` pokazuje `BazaGreskaEkran` za `Greska`, prazan tamni ekran za `Ucitavanje` i `PocetniEkran` za `Spremna`.
- [ ] **Korak 4:** Isti testovi → PASS. Fajl `app/schemas/…/1.json` postoji.
- [ ] **Korak 5: Commit** — `"Room baza v1, početni podaci, BAZA-01"`

---

### Task 4: Repozitorij podešavanja

**Fajlovi:** napraviti `repo/PodesavanjaRepo.kt`, a po potrebi dopuniti DAO-e. Test: `test/…/repo/PodesavanjaRepoTest.kt`.

**Interfejsi:**
- Koristi: DAO-e i entitete iz Taska 3, `Validacija` i `Ishod` iz Taska 2.
- Daje (`class PodesavanjaRepo(db: AppDatabase, clock: Clock)`):

```kotlin
// čitanje (Flow, sortirano po redoslijedu)
val kategorije: Flow<List<KategorijaPrikaz>>   // KategorijaPrikaz(k: KategorijaE, brojAktivnihPica: Int)
val mjere: Flow<List<MjeraPrikaz>>             // MjeraPrikaz(m: MjeraE, aktivnaPica: List<String>)  — imena aktivnih pića koja je koriste
val artikli: Flow<List<ArtikalPrikaz>>         // ArtikalPrikaz(a: ArtikalE, kategorija: KategorijaE, mjere: List<MjeraE>)
val radnici: Flow<List<RadnikE>>
val razlozi: Flow<List<RazlogE>>               // samo aktivni
val postavke: Flow<PostavkeE>
val brojaciMenija: Flow<BrojaciMenija>         // BrojaciMenija(pica, kategorija, mjera, stalnihRadnika) — samo aktivni (spec 7 #15)

// kategorije
suspend fun sacuvajKategoriju(id: Long?, naziv: String): Ishod      // NAZIV_*, NAZIV_POSTOJI među aktivnim
suspend fun postaviAktivnaKategorija(id: Long, aktivna: Boolean): Ishod  // U_UPOTREBI ako ima aktivno piće
suspend fun redoslijedKategorija(ids: List<Long>)
// mjere
suspend fun sacuvajMjeru(id: Long?, naziv: String, natpis: String, uZbiru: String, litara: Double?): Ishod
suspend fun postaviAktivnaMjera(id: Long, aktivna: Boolean): Ishod
// artikli
suspend fun sacuvajArtikal(id: Long?, naziv: String, kategorijaId: Long?, mjereIds: List<Long>): Ishod
suspend fun postaviAktivanArtikal(id: Long, aktivan: Boolean): Ishod   // pri uključivanju: NAZIV_POSTOJI, KATEGORIJA_ISKLJUCENA, MJERA_ISKLJUCENA
suspend fun redoslijedArtikala(ids: List<Long>)
suspend fun unosiUAktivnojNoci(artikalId: Long): List<Format.MjeraBroj>   // za napomenu u V1.2; prazno kad nema aktivne noći
// ljudi
suspend fun sacuvajRadnika(id: Long?, ime: String, uloga: Uloga): Ishod   // NAZIV_POSTOJI među aktivnim
suspend fun postaviAktivanRadnik(id: Long, aktivan: Boolean)
// pravila rada
suspend fun postaviRokBezPina(s: Int): Ishod        // 0..300, korak 10
suspend fun postaviIstoriju(dana: Int): Ishod       // 7..90
suspend fun postaviZakljucavanje(s: Int): Ishod     // 60..600, korak 60
suspend fun postaviIspomoc(dozvoljena: Boolean)
suspend fun nociKojeBiNestale(dana: Int): Int       // zatvorene noći sa datumom < danas − dana (spec 7 #33)
suspend fun dodajRazlog(tekst: String): Ishod       // NAZIV_PRAZAN/PREDUG/POSTOJI, max 32
suspend fun ukloniRazlog(id: Long)                  // postavlja aktivan=false
```
Novi zapis ide na kraj (`redoslijed = max + 1`). Sve se uvijek čuva preko `Validacija.ocisti`, a poredi preko `kljucNaziva`. Svaka funkcija koja piše radi u `db.withTransaction`.

- [ ] **Korak 1: Testovi koji padaju** (Robolectric, baza u memoriji, podaci se prave u testu)

```kotlin
@Test fun sacuvajArtikal_duplikatBezObziraNaVelicinuIRazmake() = runTest {
    val k = kat("Žestina"); val doza = mjeraId("Doza")
    assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, "Jack Daniel’s", k, listOf(doza)))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), repo.sacuvajArtikal(null, "  jack daniel’s ", k, listOf(doza)))
}
@Test fun sacuvajArtikal_jednaDoTriMjere() = runTest {
    val k = kat("Žestina"); val sve = repo.mjere.first().map { it.m.id }
    assertEquals(Ishod.Odbijeno(Odbijanje.BROJ_MJERA), repo.sacuvajArtikal(null, "Viljamovka", k, emptyList()))
    assertEquals(Ishod.Odbijeno(Odbijanje.BROJ_MJERA), repo.sacuvajArtikal(null, "Viljamovka", k, sve.take(4)))
    assertEquals(Ishod.Odbijeno(Odbijanje.BEZ_KATEGORIJE), repo.sacuvajArtikal(null, "Viljamovka", null, sve.take(1)))
}
@Test fun iskljucenArtikalNeSmetaIstomNazivu() = runTest { /* isključi "Smirnoff", pa novi "Smirnoff" → Ok */ }
@Test fun ukljuciArtikal_odbijaDupliNaziv() = runTest { /* A isključen, novi aktivni istog naziva, uključi A → NAZIV_POSTOJI */ }
@Test fun ukljuciArtikal_odbijaKadJeMjeraIskljucena() = runTest { /* isključi artikal, isključi njegovu mjeru (sada slobodna), uključi artikal → MJERA_ISKLJUCENA */ }
@Test fun mjeraUUpotrebi_neMozeSeIskljuciti() = runTest { /* artikal sa Bokal → postaviAktivnaMjera(bokal,false) == Odbijeno(U_UPOTREBI); isključi artikal → Ok */ }
@Test fun kategorijaSaAktivnimPicem_neMozeSeIskljuciti() = runTest { /* analogno */ }
@Test fun mjera_validacija() = runTest {
    assertEquals(Ishod.Odbijeno(Odbijanje.U_ZBIRU_PRAZNO), repo.sacuvajMjeru(null, "Čaša vina", "Čaša", "", 0.2))
    assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), repo.sacuvajMjeru(null, "Čaša vina", "Čaša", "čaša", 0.0))
    assertEquals(Ishod.Odbijeno(Odbijanje.NATPIS_PREDUG), repo.sacuvajMjeru(null, "Čaša vina", "Čaša vina 2", "čaša", 0.2))
}
@Test fun redoslijedArtikala_cuvaSe() = runTest { /* tri artikla, redoslijedArtikala([c,a,b]) → artikli.first() u tom redu */ }
@Test fun brojaciMenija_samoAktivni() = runTest { /* 2 aktivna + 1 isključen artikal → pica == 2; mjera == 5; stalnihRadnika == 4 */ }
@Test fun pravilaRada_granice() = runTest {
    assertEquals(Ishod.Odbijeno(Odbijanje.VRIJEDNOST_VAN_OPSEGA), repo.postaviRokBezPina(310))
    assertEquals(Ishod.Odbijeno(Odbijanje.VRIJEDNOST_VAN_OPSEGA), repo.postaviIstoriju(6))
    assertEquals(Ishod.Ok, repo.postaviZakljucavanje(60)); assertEquals(60, repo.postavke.first().zakljucavanjeS)
}
@Test fun nociKojeBiNestale_brojiSamoZatvoreneStarije() = runTest { /* sat 2026-11-21; noći 2026-10-01 (zatvorena), 2026-11-20 (zatvorena), 2026-10-01 aktivna → nociKojeBiNestale(30) == 1 */ }
@Test fun razlog_dodajIUkloni() = runTest { /* dodajRazlog("Vraćeno") Ok, duplikat NAZIV_POSTOJI, ukloniRazlog → nema ga u razlozi */ }
@Test fun unosiUAktivnojNoci_saberePoMjeri() = runTest { /* aktivna noć, 2 neponištene ture sa 2+1 bokala, jedna poništena sa 5 → [MjeraBroj("bok.",…,3)] */ }
```
Komentari u tijelu testa su tačan scenario. Implementator piše tijelo sa pomoćnim funkcijama `kat(naziv)` i `mjeraId(naziv)` u testu.

- [ ] **Korak 2:** `./gradlew testDebugUnitTest --tests '*PodesavanjaRepoTest*'` → FAIL.
- [ ] **Korak 3:** Implementacija po interfejsu.
- [ ] **Korak 4:** Isti testovi → PASS.
- [ ] **Korak 5: Commit** — `"Repozitorij podešavanja sa pravilima"`

---

### Task 5: PIN servis

**Fajlovi:** napraviti `domain/Pin.kt` (dopuna) i `repo/PinServis.kt`. Testovi: `test/…/domain/PinTest.kt` i `test/…/repo/PinServisTest.kt`.

**Interfejsi (daje):**
```kotlin
object Pin {
    const val DUZINA = 6; const val MAX_POGRESNIH = 10; val BLOKADA: Duration = Duration.ofMinutes(5)
    fun hesiraj(pin: String, so: String = novaSo()): Pair<String, String>   // (hešBase64, soBase64), PBKDF2WithHmacSHA256, 20_000 iteracija, 256 bita, so 16 bajtova
    fun provjeri(pin: String, hes: String, so: String): Boolean             // MessageDigest.isEqual
    fun zabranjen(pin: String): Boolean                                     // svih 6 istih, "123456", "654321"
}
sealed interface ProvjeraPina { data object Tacan; data class Pogresan(val preostalo: Int); data class Blokiran(val do_: Instant) }
class PinServis(db: AppDatabase, clock: Clock) {
    suspend fun provjeri(pin: String): ProvjeraPina
    suspend fun blokiranDo(): Instant?                    // null ako nije blokiran sada
    suspend fun promijeni(trenutni: String, novi: String): PromjenaPina
    val probni: Flow<Boolean>
}
sealed interface PromjenaPina { data object Uspjeh; data object PogresanTrenutni; data class Blokiran(val do_: Instant); data object Zabranjen; data object NeispravanFormat }
```
Pravila:
- Tačan PIN briše brojač.
- Pogrešan PIN povećava brojač. Na 10 se postavlja `pinBlokiranDo = sada + 5 min` i vraća `Blokiran`.
- Dok traje blokada, `provjeri` vraća `Blokiran` i ne broji pokušaj.
- Kad blokada istekne, brojač se vraća na 0.
- `promijeni` provjerava trenutni PIN istim brojačem, zatim provjerava format i zabranjene PIN-ove, upisuje novi heš i postavlja `pinProbni = false`.
- Provjera „novi PIN se ne poklapa“ je u UI-ju (V3, korak 3), ne u servisu.

- [ ] **Korak 1: Testovi koji padaju** (`MutableClock` je testni `Clock` sa `fun pomjeri(d: Duration)`, u `test/…/TestSat.kt`)

```kotlin
@Test fun hesIProvjera() { val (h, s) = Pin.hesiraj("123456"); assertTrue(Pin.provjeri("123456", h, s)); assertFalse(Pin.provjeri("123457", h, s)) }
@Test fun zabranjeni() { assertTrue(Pin.zabranjen("000000")); assertTrue(Pin.zabranjen("123456")); assertTrue(Pin.zabranjen("654321")); assertFalse(Pin.zabranjen("482915")) }
@Test fun desetPogresnih_blokira5min_paPustaPonovo() = runTest {
    repeat(9) { assertEquals(ProvjeraPina.Pogresan(9 - it), s.provjeri("000001")) }
    assertEquals(ProvjeraPina.Blokiran(sat.instant() + 5.min), s.provjeri("000001"))
    assertTrue(s.provjeri("123456") is ProvjeraPina.Blokiran)        // ni tačan ne prolazi dok traje
    sat.pomjeri(Duration.ofMinutes(5)); assertEquals(ProvjeraPina.Tacan, s.provjeri("123456"))
}
@Test fun tacanBriseBrojac() = runTest { repeat(9) { s.provjeri("000001") }; s.provjeri("123456"); assertEquals(ProvjeraPina.Pogresan(9), s.provjeri("000001")) }
@Test fun blokadaPreziviNovuInstancuServisa() = runTest { repeat(10) { s.provjeri("000001") }; assertTrue(PinServis(db, sat).provjeri("123456") is ProvjeraPina.Blokiran) }
@Test fun promjena() = runTest {
    assertEquals(PromjenaPina.PogresanTrenutni, s.promijeni("111112", "482915"))
    assertEquals(PromjenaPina.Zabranjen, s.promijeni("123456", "777777"))
    assertEquals(PromjenaPina.NeispravanFormat, s.promijeni("123456", "4829"))
    assertTrue(s.probni.first())
    assertEquals(PromjenaPina.Uspjeh, s.promijeni("123456", "482915"))
    assertFalse(s.probni.first()); assertEquals(ProvjeraPina.Tacan, s.provjeri("482915"))
}
```
U kodu `sat.instant() + 5.min` znači `plus(Duration.ofMinutes(5))`.

- [ ] **Korak 2:** pokrenuti → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Commit** — `"PIN: heš, blokada 10/5 min, promjena"`

---

### Task 6: UI komponente

**Fajlovi:** napraviti `ui/components/*.kt` sa mape fajlova. Test: `test/…/ui/components/KomponenteTest.kt`.

Izgled i vrijednosti se uzimaju iz Papera (O2, O3, O6 na stranici „Osnova“) preko `get_jsx`/`get_computed_styles`. Opis stanja je u inventaru (O2, O3, O6).

**Interfejsi (daje):**
```kotlin
@Composable fun GlavnoDugme(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, ikona: ImageVector? = null, omoguceno: Boolean = true, upisuje: Boolean = false)  // zlatno; upisuje -> "Upisuje se…" + spinner, zaključano
@Composable fun SporednoDugme(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, ikona: ImageVector? = null, omoguceno: Boolean = true)  // outline
@Composable fun OpasnoDugme(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier)  // crveni tekst i okvir
@Composable fun Cip(tekst: String, izabran: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, omogucen: Boolean = true, kvacica: Boolean = true)  // izbor kao V1.1
@Composable fun Pilula(tekst: String, izabrana: Boolean, onClick: () -> Unit)  // filter kao V1 („Sve · 15“)
@Composable fun Prekidac(ukljucen: Boolean, onPromjena: (Boolean) -> Unit, omogucen: Boolean = true)
@Composable fun Stepper(vrijednost: String, jedinica: String, onMinus: () -> Unit, onPlus: () -> Unit, minusOmogucen: Boolean, plusOmogucen: Boolean)  // 56×56 dugmad
@Composable fun SegmentiraniIzbor(opcije: List<String>, izabrana: Int, onIzbor: (Int) -> Unit)  // V2 uloga
@Composable fun PinUnos(unesene: Int, greska: String?, onCifra: (Char) -> Unit, onBrisi: () -> Unit, omogucen: Boolean = true)  // 6 tačaka + tastatura 3×4 (1-9 / prazno 0 ⌫); greška: prazne tačke sa crvenim okvirom + tekst sa trouglom
@Composable fun Baner(vrsta: VrstaBanera, naslov: String, tekst: String, dugme: String? = null, onDugme: () -> Unit = {})  // UPOZORENJE (warning tint), INFO, GRESKA
@Composable fun Prozor(naslov: String, onZatvori: () -> Unit, sirina: Dp, ikona: ImageVector? = null, sadrzaj: @Composable ColumnScope.() -> Unit, dugmad: @Composable RowScope.() -> Unit)  // scrim + surface radius 16
@Composable fun Polje(labela: String, vrijednost: String, onPromjena: (String) -> Unit, greska: String? = null, placeholder: String = "", max: Int? = null, brojacOd: Int? = null, tastatura: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier)  // labela iznad, 56 dp; greška se prikazuje kad je greska != null
class ToastStanje { fun prikazi(tekst: String, akcija: String? = null, onAkcija: () -> Unit = {}) }  // 4 s, krem podloga, zelena kvačica (O3)
@Composable fun ToastHost(stanje: ToastStanje, modifier: Modifier = Modifier)
```
Ikone su iz `Icons.Outlined`/`Icons.Rounded` (material-icons-extended), sa onim koje su najbliže ikonama u Paperu (katanac, čaša, mreža, ljudi, klizači, info, strelica nazad, kvačica, ×, ⌫, ručica ⋮⋮). Validacija „greška tek kad se izađe iz polja“ (O6) živi u ekranu: on predaje `greska` tek poslije gubitka fokusa ili pokušaja čuvanja.

- [ ] **Korak 1: Testovi koji padaju** (Robolectric + `createComposeRule`)

```kotlin
@Test fun pinUnos_sestCifaraIBrisanje() {
    val unos = mutableStateListOf<Char>()
    compose.setContent { CafeBarTema { PinUnos(unos.size, null, { unos.add(it) }, { unos.removeLastOrNull() }) } }
    "123456".forEach { compose.onNodeWithText(it.toString()).performClick() }
    assertEquals("123456".toList(), unos.toList())
    compose.onNodeWithContentDescription("Obriši cifru").performClick(); assertEquals(5, unos.size)
}
@Test fun pinUnos_onemogucenNePrimaCifre() { /* omogucen=false, klik na "1" → lista prazna */ }
@Test fun glavnoDugme_upisujeIgnoriseDrugiKlik() { /* upisuje=true: prikazan "Upisuje se…", klik ne zove onClick */ }
@Test fun polje_prikazujeBrojacOd20() { /* max=24, brojacOd=20, vrijednost 21 znak → prikazan "21/24"; 19 znakova → nema brojača */ }
@Test fun stepper_onemogucenPlusNaMaksimumu() { /* plusOmogucen=false → "+" nije omogućen (assertIsNotEnabled) */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Commit** — `"UI komponente iz O2/O3/O6"`

---

### Task 7: Ulaz u podešavanja — V0, sesija, ljuska, navigacija

**Fajlovi:** napraviti `ui/podesavanja/PodesavanjaSesija.kt`, `PinProzor.kt`, `Ljuska.kt`, `PodesavanjaNav.kt`. Izmijeniti `MainActivity.kt`, `AppContainer.kt` i `PocetniEkran.kt`. Testovi: `test/…/ui/podesavanja/PodesavanjaSesijaTest.kt` i `PinProzorTest.kt`.

**Interfejsi:**
- Koristi: `PinServis` (Task 5), `PodesavanjaRepo.brojaciMenija` i `postavke` (Task 4), komponente (Task 6).
- Daje:
```kotlin
class PodesavanjaSesija(scope: CoroutineScope, zakljucavanjeS: () -> Int) {
    val otkljucano: StateFlow<Boolean>; val preostaloS: StateFlow<Int>
    fun otkljucaj(); fun dodir(); fun zakljucaj()
}
enum class Sekcija { ARTIKLI, KATEGORIJE, MJERE, LJUDI, PRAVILA, PIN, O_APLIKACIJI }
@Composable fun Ljuska(aktivna: Sekcija, onSekcija: (Sekcija) -> Unit, onNazad: () -> Unit, sesija: PodesavanjaSesija, brojaci: BrojaciMenija, probniPin: Boolean, sadrzaj: @Composable () -> Unit)
@Composable fun PinProzor(pin: PinServis, onOtkljucano: () -> Unit, onOdustani: () -> Unit)
```
- Sesija odbrojava svake sekunde. Na 0 se zaključava. `dodir()` vraća brojač na `zakljucavanjeS()`.
- `MainActivity` u `onStop` zove `sesija.zakljucaj()`.
- Ljuska hvata svaki dodir preko `Modifier.pointerInput` sa `awaitPointerEventScope` u `PointerEventPass.Initial`, bez trošenja događaja, i zove `sesija.dodir()`.
- Kad `otkljucano` postane `false`, navigacija se vraća na početni ekran.
- Ljuska (spec 6.1, inventar „Zajednička ljuska“):
  - gornja traka: „‹ Nazad na unos“, „Podešavanja“, „samo vlasnik“;
  - baner UPOZORENJE: „Otključano za vlasnika.“ + „Zaključava se samo za {Format.odbrojavanje(preostaloS)} bez dodira.“ + „Zaključaj sada“;
  - kad je `probniPin`, ispod banera ide drugi baner UPOZORENJE: „Još se koristi probni PIN 123456. Promijeni ga u meniju PIN vlasnika.“;
  - lijevi meni sa 7 stavki, podnaslovi:
    - „{Format.brojac(pica,"piće","pića","pića")} · redoslijed“
    - `Format.brojac(kategorija,"kategorija","kategorije","kategorija")`
    - „{Format.brojac(mjera,"mjera","mjere","mjera")} u litrima“
    - `Format.brojac(stalnihRadnika,"stalni radnik","stalna radnika","stalnih radnika")`
    - „poništavanje, istorija“
    - „promjena“
    - „verzija, kontakt“
  - na dnu menija: „Sve promjene važe odmah, bez nove verzije aplikacije.“
- „Nazad na unos“ i „Zaključaj sada“ zovu `zakljucaj()` (spec 7 #25).
- `PinProzor` (V0):
  - tekst „Samo vlasnik mijenja artikle, mjere i radnike. Otključava se čim upišeš šestu cifru.“;
  - provjera ide na šestoj cifri;
  - `Pogresan` → tačke se prazne, a ispod piše „PIN nije tačan. Unesi ponovo. · PIN-01“;
  - `Blokiran` → tastatura je onemogućena i piše „Previše pogrešnih pokušaja. Novi pokušaj je moguć u {HH:mm}. · PIN-02“ (format kao T5; tekst u inventaru T5);
  - „Odustani“ zatvara prozor.
- Navigacija: Navigation Compose sa rutama `pocetni` i `podesavanja/{sekcija}`. Početna sekcija je ARTIKLI. Sekcije koje još nisu napravljene u ovom tasku su prazan radni dio (Taskovi 8–11 ih pune).

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@Test fun sesija_odbrojavaIZakljucava() = runTest {
    val s = PodesavanjaSesija(backgroundScope, { 120 }); s.otkljucaj()
    advanceTimeBy(20_000); runCurrent(); assertEquals(100, s.preostaloS.value)
    s.dodir(); assertEquals(120, s.preostaloS.value)
    advanceTimeBy(120_000); runCurrent(); assertFalse(s.otkljucano.value)
}
@Test fun sesija_zakljucavaKadAplikacijaOdeUPozadinu() { /* ActivityScenario: otključaj, moveToState(CREATED), pa RESUMED → otkljucano == false */ }
@Test fun pinProzor_tacanPinOtkljucava() { /* unesi 123456 → onOtkljucano pozvan */ }
@Test fun pinProzor_pogresanPrikazujePin01() { /* unesi 000001 → prikazan tekst koji sadrži "PIN-01", tačke prazne */ }
@Test fun pinProzor_blokiranPrikazujePin02IOnemogucenaTastatura() { /* 10× pogrešan → tekst sadrži "PIN-02"; klik na "1" ne mijenja broj unesenih */ }
@Test fun ljuska_baneriIMeni() { /* probniPin=true → vidi se "Još se koristi probni PIN 123456…"; brojaci(15,3,6,4) → "15 pića · redoslijed", "3 kategorije", "4 stalna radnika" */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Ručna provjera na emulatoru.**
  1. „Podešavanja“ → 123456 → otvara se ljuska sa dva banera.
  2. Odbrojavanje radi.
  3. Dugme Home, pa povratak → zaključano.
  4. Snimak se poredi sa V0 i ljuskom u Paperu.
- [ ] **Korak 6: Commit** — `"V0 PIN, sesija sa zaključavanjem, ljuska podešavanja"`

---

### Task 8: V1 Artikli, V1.1 i V1.2

**Fajlovi:** napraviti `ui/podesavanja/artikli/ArtikliEkran.kt`, `ArtikalProzor.kt` i `ArtikliViewModel.kt`. Test: `test/…/ui/podesavanja/artikli/ArtikliTest.kt`.

**Interfejsi:**
- Koristi: `PodesavanjaRepo` (`artikli`, `kategorije`, `mjere`, `sacuvajArtikal`, `postaviAktivanArtikal`, `redoslijedArtikala`, `unosiUAktivnojNoci`), komponente.
- Daje `ArtikliViewModel(repo)` sa stanjem `filter: Long?` (null = Sve), `lista`, `prozor: ArtikalForma?` i radnjama `otvoriNovi()`, `otvoriIzmjenu(id)`, `sacuvaj()`, `odustani()`, `prebaci(id, aktivan)`, `pomjeri(od, do)`.

Ponašanje (tekstovi iz inventara V1, V1.1, V1.2 i O6):
- Filter pilule: „Sve · {broj}“, pa aktivne kategorije. Lista se crta po koloni iz inventara.
- Čip mjere u listi ima format „{naziv} {Format.litre}“ bez „l“, a za komad samo naziv (spec 7 #13).
- Isključen red je siv.
- Drag ručicom preko `sh.calvin.reorderable`. Na spuštanje se zove `redoslijedArtikala` sa redoslijedom **svih** artikala. Kad je filter aktivan, pomjera se samo unutar filtera, a ostali zadržavaju relativna mjesta.
- Isključivanje prekidačem otvara prozor „Isključiti {naziv}?“ / „Neće se nuditi na šanku. Stare noći ga i dalje prikazuju. Možeš ga ponovo uključiti ovdje.“ / „Odustani“ i „Isključi“ (spec, O3 „Arhiviranje“). Uključivanje ide bez prozora.
- Odbijeno uključivanje prikazuje toast:
  - NAZIV_POSTOJI → „Taj naziv već postoji · ARTIKAL-01“
  - KATEGORIJA_ISKLJUCENA → „Prvo uključi kategoriju {naziv}.“
  - MJERA_ISKLJUCENA → „Prvo uključi mjeru {naziv}.“
- Prozor (820 dp):
  - polje „Naziv“ sa placeholderom „npr. Jägermeister 0,7“, max 24, brojač od 20;
  - čipovi kategorija, samo aktivne, jednostruki izbor;
  - „Dugmad na pločici · najviše 3“: čipovi aktivnih mjera u formatu „{naziv} {litre} l“ (komad bez litara); kad su izabrane 3, ostali su onemogućeni;
  - desno živi pregled pločice: naziv + dugmad sa `natpis` mjera; za jednu mjeru pločica kao na S1 sa natpisom malim slovom dole;
  - „Odustani“ i „✓ Sačuvaj artikal“ / „✓ Sačuvaj izmjenu“;
  - dugme za čuvanje je onemogućeno dok naziv nije prazan, kategorija izabrana i 1–3 mjere izabrane.
- Greške ispod polja se javljaju poslije izlaska iz polja ili pokušaja čuvanja:
  - NAZIV_POSTOJI → „Taj naziv već postoji · ARTIKAL-01“
  - NAZIV_PREDUG → ne može se desiti, jer polje ne prima preko 24 znaka
- Izlazak iz prozora sa izmjenama otvara potvrdu „Odbaciti novi artikal?“ / „Uneseni naziv i mjera se ne čuvaju.“ / „Nastavi unos“ i „Odbaci“ (O6). Za izmjenu je naslov „Odbaciti izmjene?“. Ovo je zamjena sadržaja prozora, ne prozor preko prozora (O5).
- Uspjeh: prozor se zatvara i ide toast „Sačuvano: {naziv}“.
- V1.2:
  - naslov „Izmjena · {naziv}“;
  - ako `unosiUAktivnojNoci` nije prazno, dole lijevo piše „Promjena važi od sljedeće ture. Ove noći je već uneseno {Format.zbirMjera(...)}, to ostaje kako je uneseno.“

- [ ] **Korak 1: Testovi koji padaju** (Robolectric Compose nad pravim repozitorijem i bazom u memoriji)

```kotlin
@Test fun noviArtikal_sacuvanIVidiSeUListi() { /* napravi kategoriju "Žestina" kroz repo; otvori "+ Novi artikal", upiši "Viljamovka", izaberi Žestina, Doza i Bokal, Sačuvaj → red "Viljamovka" postoji, čipovi "Doza 0,03" i "Bokal 0,25" */ }
@Test fun sacuvajOnemogucenoBezMjere() { /* naziv + kategorija, bez mjere → "Sačuvaj artikal" nije omogućeno */ }
@Test fun cetvrtaMjeraSeNeMozeIzabrati() { /* izaberi 3 → čip "Boca 1,00 l" nije omogućen */ }
@Test fun duplikatPrikazujeArtikal01() { /* postoji "Smirnoff"; novi "smirnoff", Sačuvaj → tekst "Taj naziv već postoji · ARTIKAL-01", prozor ostaje otvoren sa unesenim nazivom */ }
@Test fun iskljucivanjeTraziPotvrdu() { /* prekidač → prozor "Isključiti Smirnoff?" → "Isključi" → artikal neaktivan u repo */ }
@Test fun odustaniSaIzmjenamaPita() { /* upiši naziv, Odustani → "Odbaciti novi artikal?"; "Nastavi unos" vraća formu sa nazivom */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Ručna provjera.** Snimci V1, V1.1 i V1.2 na emulatoru se porede sa Paperom (raspored, veličine, boje). Provjeriti i drag.
- [ ] **Korak 6: Commit** — `"V1 Artikli sa prozorima i redoslijedom"`

---

### Task 9: V5 Kategorije i V4 Mjere

**Fajlovi:** napraviti `ui/podesavanja/kategorije/{KategorijeEkran,KategorijaProzor,KategorijeViewModel}.kt` i `ui/podesavanja/mjere/{MjereEkran,MjeraProzor,MjereViewModel}.kt`. Test: `test/…/ui/podesavanja/KategorijeMjereTest.kt`.

**Interfejsi:** koristi `PodesavanjaRepo` (kategorije i mjere) i komponente. Prozori imaju isti oblik kao `ArtikalProzor` (spec 7 #26).

Ponašanje:
- **V5** (inventar V5):
  - ručica i drag (`redoslijedKategorija`), naziv, „{n} pića“, prekidač, „›“;
  - za 0 pića piše „nema pića“ (spec 7 #14);
  - prekidač kategorije sa aktivnim pićem je onemogućen;
  - prozor „Nova kategorija“ / „Izmjena · {naziv}“: polje „Naziv“ (max 16), „Odustani“ i „✓ Sačuvaj kategoriju“;
  - duplikat → ispod polja „Ta kategorija već postoji.“
- **V4** (inventar V4 + nova kolona „U ZBIRU“ iz spec 7 #26):
  - kolone: NAZIV U PREGLEDU | LITARA | DUGME | U ZBIRU | KORISTE JE, pa prekidač i „›“;
  - LITARA: `Format.litre` ili „—“;
  - KORISTE JE: 0 → „nema pića“, 1–2 → imena spojena sa „, “, više → „{n} pića“;
  - nema drag-a;
  - prekidač mjere u upotrebi je onemogućen;
  - prozor „Nova mjera“ / „Izmjena · {naziv}“ sa poljima:
    - „Naziv u pregledu“ (max 16)
    - „Natpis na dugmetu“ (max 8)
    - „U zbiru“ (max 8, pomoćni tekst „Kratko ime u zbiru, npr. kom, doza, bok.“)
    - „Litara“ (decimalna tastatura, pomoćni tekst „Prazno za komad. Kuca se sa zarezom: 0,70.“)
  - greške ispod polja: „Litre moraju biti od 0,01 do 5,00.“, „Obavezno polje.“, „Ta mjera već postoji.“;
  - pomoćni tekst u prozoru izmjene: „Promjena litara važi od sljedeće ture. Stare ture čuvaju litre iz trenutka unosa.“

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@Test fun kategorija_novaIDuplikat() { /* "Pivo" sačuvana i vidi se "nema pića"; ponovo "pivo" → "Ta kategorija već postoji." */ }
@Test fun kategorija_prekidacOnemogucenKadImaAktivnoPice() { /* kategorija + aktivan artikal → prekidač tog reda nije omogućen */ }
@Test fun mjere_pocetnaTabela() { /* redovi Komad … Boca 1 l; Komad ima "—" u litrama i "kom" u zbiru; KORISTE JE "nema pića" */ }
@Test fun mjera_novaSaLitrama() { /* "Čaša vina", "Čaša", "čaša", "0,2" → red sa "0,20" */ }
@Test fun mjera_litreVanOpsega() { /* "6" → "Litre moraju biti od 0,01 do 5,00." */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Ručna provjera** V4 i V5 prema Paperu.
- [ ] **Korak 6: Commit** — `"V5 Kategorije i V4 Mjere"`

---

### Task 10: V2 Ljudi i V6 Pravila rada

**Fajlovi:** napraviti `ui/podesavanja/ljudi/{LjudiEkran,RadnikProzor,LjudiViewModel}.kt` i `ui/podesavanja/pravila/{PravilaEkran,PravilaViewModel}.kt`. Test: `test/…/ui/podesavanja/LjudiPravilaTest.kt`.

Ponašanje:
- **V2** (inventar V2):
  - red: ime + opis iz uloge („nosi piće gostima“ / „ne izlazi iz šanka, prodaje piće“), `SegmentiraniIzbor` „Konobar | Za šankom“ (promjena se odmah čuva), „radi“ + prekidač;
  - isključivanje radnika traži potvrdu „Isključiti {ime}?“ / „Neće se nuditi pri otvaranju noći. Ranije noći ga i dalje prikazuju.“ / „Odustani“ i „Isključi“;
  - dodir na ime otvara „Izmjena · {ime}“; „+ Novi radnik“ otvara „Novi radnik“;
  - polja: „Ime“ (max 20) i uloga; duplikat → „Radnik sa tim imenom već postoji.“;
  - info kartica i tekst ispod tačno iz inventara.
- **V6** (inventar V6, granice iz spec 7 #27):
  - Steperi:
    - rok bez PIN-a: ±10 s, 0–300
    - istorija: ±1 dan, 7–90
    - zaključavanje: ±1 min, 1–10
  - Prikaz vrijednosti: „60 s“, „30 dana“ (množina: dan/dana/dana), „2 min“.
  - Dugme na granici je onemogućeno.
  - Smanjenje istorije zove `nociKojeBiNestale(nova)`. Ako je > 0, otvara prozor „Obrisati stare noći?“ / „{n} {noć/noći/noći} starijih od {d} dana biće obrisano pri sljedećem pokretanju.“ / „Nastavi unos“ i „Smanji istoriju“ (spec 7 #33). Bez potvrde se vrijednost ne mijenja.
  - Prekidač „Ispomoć“.
  - Razlozi su čipovi sa „×“. „×“ zove `ukloniRazlog` bez potvrde, jer stare ture čuvaju tekst razloga.
  - „+ Razlog“ otvara mali prozor „Novi razlog“ sa poljem „Razlog“ (max 32), „Odustani“ i „✓ Dodaj razlog“.

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@Test fun ljudi_pocetnaListaIUloge() { /* Vuleta "nosi piće gostima", Miloš "ne izlazi iz šanka, prodaje piće" */ }
@Test fun ljudi_promjenaUlogeSeCuva() { /* Smitran → Konobar → repo.radnici: uloga KONOBAR */ }
@Test fun ljudi_noviIDuplikat() { /* "Dejan" Ok; "vuleta" → "Radnik sa tim imenom već postoji." */ }
@Test fun pravila_stepperGranice() { /* rok 300: "+" onemogućen; zaključavanje 1 min: "−" onemogućen */ }
@Test fun pravila_razlogDodajIUkloni() { /* "+ Razlog" → "Vraćeno" → čip postoji; "×" → nestaje */ }
@Test fun pravila_smanjenjeIstorijeTraziPotvrduKadBriseNoci() { /* sat fiksan; u bazi zatvorena noć sa datumom prije 20 dana; repo.postaviIstoriju(20); na ekranu "−" → prozor "Obrisati stare noći?" sa "1 noć starijih od 19 dana…"; "Nastavi unos" → prikazano "20 dana"; ponovo "−" → "Smanji istoriju" → "19 dana" */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Ručna provjera** V2 i V6 prema Paperu.
- [ ] **Korak 6: Commit** — `"V2 Ljudi i V6 Pravila rada"`

---

### Task 11: V3 Promjena PIN-a i V7 O aplikaciji

**Fajlovi:** napraviti `ui/podesavanja/pin/{PromjenaPinaEkran,PromjenaPinaViewModel}.kt` i `ui/podesavanja/oaplikaciji/OAplikacijiEkran.kt`. Test: `test/…/ui/podesavanja/PinOAplikacijiTest.kt`.

Ponašanje:
- **V3**:
  - stepper sa 3 koraka iz inventara;
  - natpis „Korak {n} od 3 · {Trenutni PIN | Novi PIN | Ponovi novi PIN}“;
  - `PinUnos` sa 6 tačaka;
  - tekst „PIN ima 6 cifara i zna ga samo vlasnik. Ne koristi datum rođenja ni 123456.“ i „Zaboravljen PIN niko ne može poništiti. Zapiši ga na sigurno mjesto.“
  - Korak 1 provjerava trenutni PIN odmah preko `PinServis.provjeri`, pa se pogrešan broji u blokadu:
    - pogrešan → PIN-01, ostaje se na koraku 1;
    - blokada → PIN-02.
  - Korak 2: zabranjen PIN → „Taj PIN je lako pogoditi. Izaberi drugi.“, ostaje se na koraku 2.
  - Korak 3:
    - nepoklapanje → „PIN-ovi se ne poklapaju. Upiši novi PIN ponovo.“ i povratak na korak 2;
    - poklapanje → `promijeni(trenutni, novi)` → toast „PIN je promijenjen.“, ekran se vraća na korak 1, a baner probnog PIN-a nestaje.
- **V7**:
  - tabela iz inventara; „Verzija“ = `BuildConfig.VERSION_NAME`; „Istorija“ = „{istorijaDana} dana · podešava se u Pravilima rada“;
  - kontakt kartica doslovno;
  - broj telefona nije klikabilan, jer tablet nije telefon.

- [ ] **Korak 1: Testovi koji padaju**

```kotlin
@Test fun promjenaPina_cijeliTok() { /* 123456 → 482915 → 482915 → toast "PIN je promijenjen."; PinServis.provjeri("482915") == Tacan; probni == false */ }
@Test fun promjenaPina_pogresanTrenutni() { /* 000001 → "PIN-01", natpis i dalje "Korak 1 od 3" */ }
@Test fun promjenaPina_nepoklapanje() { /* 123456 → 482915 → 482916 → "PIN-ovi se ne poklapaju…", "Korak 2 od 3" */ }
@Test fun promjenaPina_zabranjen() { /* 123456 → 111111 → "Taj PIN je lako pogoditi. Izaberi drugi." */ }
@Test fun oAplikaciji_istorijaIzPostavki() { /* postaviIstoriju(45) → "45 dana · podešava se u Pravilima rada" */ }
```

- [ ] **Korak 2:** → FAIL. **Korak 3:** implementacija. **Korak 4:** → PASS.
- [ ] **Korak 5: Commit** — `"V3 Promjena PIN-a i V7 O aplikaciji"`

---

### Task 12: Release potpis, APK i završna provjera faze 1

**Fajlovi:** izmijeniti `app/build.gradle.kts` (`signingConfigs.release` iz Gradle svojstava, `isMinifyEnabled = true`, `proguard-rules.pro` za Room i serializaciju). Napraviti `docs/IZDAVANJE.md`.

- [ ] **Korak 1: Ključ.**
  - Ključ se pravi sa `keytool -genkeypair -v -keystore ~/Desktop/2-Klijenti/Marinko919/Kljuc-potpisa/cafebar919-release.jks -alias cafebar919 -keyalg RSA -keysize 4096 -validity 10000`. Lozinka je nasumična, 24 znaka (`openssl rand -base64 18`).
  - Lozinka ide u macOS Keychain (`security add-generic-password -s cafebar919-release -a nextpixel -w <lozinka>`) i u `~/.gradle/gradle.properties` kao `CAFEBAR_STORE_FILE`, `CAFEBAR_STORE_PASSWORD`, `CAFEBAR_KEY_ALIAS`, `CAFEBAR_KEY_PASSWORD`.
  - Ništa od toga ne ide u git.
  - Na kraju se korisniku kaže gdje su ključ i lozinka i da ih mora sačuvati, jer se bez njih nova verzija ne može instalirati preko stare.
- [ ] **Korak 2:** `docs/IZDAVANJE.md` objašnjava:
  - kako se pravi release APK (`./gradlew assembleRelease`);
  - pravilo „uvijek isti ključ, `versionCode` raste, instalacija preko stare“;
  - kako se instalira sa `adb install -r`.
- [ ] **Korak 3: Svi testovi** — `./gradlew testDebugUnitTest` → svi PASS (navesti broj testova).
- [ ] **Korak 4:** `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`. `apksigner verify --print-certs` pokazuje cafebar919 sertifikat.
- [ ] **Korak 5: Ručni prolaz na emulatoru `Tablet_1280x800` sa release APK-om:**
  1. prvo pokretanje;
  2. PIN 123456;
  3. kategorije Pivo i Žestina;
  4. artikli Jelen pivo (Komad) i Rakija šljiva (Doza + Bokal);
  5. promjena redoslijeda;
  6. isključivanje i uključivanje;
  7. mjera „Čaša vina“;
  8. radnik i promjena uloge;
  9. pravila;
  10. promjena PIN-a;
  11. zatvaranje i ponovno otvaranje aplikacije → sve je sačuvano, a podešavanja su zaključana.

  Sve ovo se unosi ručno i obriše se deinstalacijom poslije provjere. Snimci V0–V7 idu u scratchpad i porede se sa Paperom.
- [ ] **Korak 6: Commit** — `"Release potpis i uputstvo za izdavanje"`
