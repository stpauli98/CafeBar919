# Faza 2 — noć i unos ture: plan izrade

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Cilj:** radnik otvara noć (S0, S0.1), bira konobara i pića, potvrđuje turu i poništava je u roku bez PIN-a (S1, T1, T2). Nepotvrđena tura preživljava restart, a dvostruki dodir upisuje jednu turu. Na kraju je release APK `versionCode 2`, instaliran preko faze 1 bez gubitka podataka.

**Arhitektura:** isti obrazac kao faza 1. `domain/` dobija čista pravila ture (nacrt, rok, pretraga, litre po kategoriji, formati), `data/` nove DAO upite (bez promjene šeme), `repo/` dva nova repozitorija (`NocRepo`, `TuraRepo`), `ui/sank/` ekrane S0 i S1 sa ViewModel-ima. Privremeni `PocetniEkran` nestaje: početna ruta crta S0 ili S1, zavisno od toga da li postoji otvorena noć.

**Tehnologije:** iste kao u fazi 1 (`gradle/libs.versions.toml`), bez novih zavisnosti. Nacrt ture je JSON preko kotlinx-serialization (već u projektu).

**Spec:** `docs/superpowers/specs/2026-11-21-android-aplikacija-design.md` (**spec**), tekstovi i izgled: `docs/superpowers/specs/2026-11-21-paper-inventar.md` (**inventar**), odjeljci S0, S0.1, S1, S1.1, T1, T2, O2, O3, O4, O7. Tačne vrijednosti (veličine, boje, razmaci) uzimaju se iz Papera alatima `get_jsx` (`includeNodeIds: false`) i `get_computed_styles`, fajl `01M7Z35WJSKRE36Z9P9VQBPWWA`, stranica „Šank — tablet“ (S0–S1.1) i „Osnova“ (O2–O4, T1, T2). Slike služe samo za provjeru. Napomene iz pregleda faze 1 su u memoriji projekta (`cafebar919-faza2-napomene`) i ugrađene su u taskove 2, 4 i 7.

## Opšta ograničenja

- Sva pravila iz „Opšta ograničenja“ plana faze 1 važe i dalje (jezik, 56 dp, Clock, bez izmišljenih podataka, Robolectric, okruženje sa `JAVA_HOME`/`ANDROID_HOME`).
- **Šema baze ostaje verzija 1.** Novi su samo upiti i klase bez `@Entity`. Ako task ipak traži promjenu šeme, staje se i pita (pravilo iz `docs/IZDAVANJE.md`). Provjera: `git diff --exit-code app/schemas` prolazi na kraju svakog taska.
- Upis ture **hvata izuzetke** (UPIS-01). Obrazac iz ViewModel-a podešavanja, gdje nema try/catch oko baze, ne prenosi se na unos ture.
- Rad je na grani `faza-2` napravljenoj iz `main`.
- Dugmad „Završi noć“ (S1), „Pregled noći“ (traka) i „Istorija“ (S0) se u fazi 2 **ne crtaju**. Ekrani iza njih (S1.2, S2, S3) dolaze u fazi 3. Isto važi za dodir na turu van roka (S1.3).

## Odluke u ovom planu (spec ih ne pokriva; upisuju se u spec poglavlje 7 u Tasku 10)

- **#34 Početno stanje S0:** nijedan radnik nije označen. Radnik označava dodirom. Spisak čine aktivni radnici redoslijedom iz V2.
- **#35 Pretraga (T1):** ne razlikuje velika i mala slova ni kvačice („sljiva“ nalazi „Rakija šljiva“, „đ“ = „d“). Svaka upisana riječ mora biti početak neke riječi u nazivu („jack d“ nalazi „Jack Daniel’s“, „cola“ nalazi „Coca-Cola“). Pretraga ide kroz **sva** pića: upis u polje bira čip „Sve“.
- **#36 Čipovi kategorija na S1:** „Sve“ plus aktivne kategorije koje imaju bar jedno aktivno piće, redoslijedom iz V5. Ako izabrana kategorija nestane, bira se „Sve“.
- **#37 Količina:** jedna stavka ide od 1 do 99. Na 99 se plus i dodir na pločicu ignorišu.
- **#38 Sat vraćen unazad:** ako je `sada < potvrdjena_u`, rok bez PIN-a se smatra isteklim. Takva tura se poništava PIN-om (faza 3).
- **#39 Piće ili mjera isključeni dok su u nacrtu:** stavka nestaje iz ture na ekranu i ne upisuje se. Ako ne ostane nijedna stavka, „Potvrdi“ je onemogućeno kao za praznu turu.
- **#40 Ime ispomoći:** očišćeno (`Validacija.ocisti`), 1–20 znakova (`Validacija.IME_MAX`), ne smije biti jednako (bez obzira na velika slova) imenu stalnog radnika na spisku, drugoj ispomoći te noći ni riječi „Šank“.

**Novi tekstovi koji čekaju odobrenje korisnika** (nisu u Paperu): „Upiši ime.“ (prazno ime ispomoći), „To ime već postoji večeras.“ (duplo ime ispomoći).

## Na šta paziti pri pregledu

1. **Dvostruki dodir na „Potvrdi“ i „Pokušaj ponovo“ poslije UPIS-01** moraju dati tačno jednu turu, jer nacrt nosi isti `kljuc`. Test: Task 4, `potvrdi_istiKljucDvaputJednaTura` i `potvrdi_poslijeGreskeIstiKljucJednaTura`; Task 7, `potvrdi_dvaDodiraJednaTura`.
2. **Restart usred ture** (proces ubijen, tablet ugašen) vraća izabranog primaoca i stavke. Piće isključeno u međuvremenu ne ulazi u turu. Test: Task 7, `nacrt_preziviNoviViewModel` i `nacrt_iskljucenoPiceNestaje`; Task 4, `potvrdi_preskaceIskljucenArtikal`.
3. **Ponoć i sat vraćen unazad.** Datum noći ostaje datum otvaranja, a „Poništi“ ne traje duže od roka ni kad se sat vrati. Test: Task 1, `preostalo_satUnazadNemaRoka`; Task 3, `datumNociOstajePoslijePonoci`; Task 4, `ponistiBezPina_satUnazadOdbija`.
4. **Puno ljudi i pića** (10 radnika, 3 ispomoći, 60 pića, tura od 15 stavki). Sve se dohvata skrolovanjem, a „Potvrdi“ ostaje vidljivo dole desno. Test: Task 8, `mnogoLjudiIPica_sveDostupnoIPotvrdiVidljivo`.
5. **Ime ispomoći** koje je prazno, samo razmaci, „šank“ ili ime stalnog radnika („vuleta“) odbija se porukom ispod polja, a „Dodaj“ ne dodaje karticu. Test: Task 1, `provjeriIspomoc_*`; Task 6, `ispomoc_dupliNazivOstajeUProzoru`.

---

## Mapa fajlova

```
app/src/main/java/dev/nextpixel/cafebar919/
  domain/Format.kt            + vrijeme, mjeraNaPlocici, mjeraUStavci, natpisUTuri, sazetakTure, litreKategorija, datumMalim
  domain/Ishod.kt             + PRAZNA_TURA, BEZ_PRIMAOCA, NEMA_NOCI, ROK_ISTEKAO, VEC_PONISTENA
  domain/Validacija.kt        + provjeriIspomoc
  domain/Nacrt.kt             NOVO: StavkaNacrta, operacije nad nacrtom, JSON, poruka ispod „Potvrdi“
  domain/Rok.kt               NOVO: preostale sekunde roka bez PIN-a
  domain/Pretraga.kt          NOVO: normalizacija i poklapanje naziva
  domain/Obracun.kt           NOVO: StavkaObracuna, zbir mjera, litre po kategoriji (faza 3 dopunjuje)
  data/Relacije.kt            NOVO: TuraSaStavkama, StavkaPrimaoca (bez @Entity)
  data/dao/NocDao.kt          + upiti za aktivnu noć, primaoce, nacrt, ture
  data/AppDatabase.kt         + PRAGMA synchronous=FULL pri otvaranju
  data/Prostor.kt             NOVO: Prostor, ProstorTableta, ProstorStanje (PROSTOR-01)
  repo/NocRepo.kt             NOVO: aktivna noć, primaoci, otvaranje noći
  repo/TuraRepo.kt            NOVO: katalog, nacrt, potvrda, poništavanje bez PIN-a, posljednje, zbirovi
  AppContainer.kt             + noc(), ture(), prostor
  MainActivity.kt             AppNav bez datuma
  ui/components/PlocicaOsobe.kt, PlocicaPica.kt, StepperTure.kt, PoljePretrage.kt, Znacka.kt   NOVO
  ui/sank/GornjaTraka.kt      NOVO: traka 72 (znak, datum, značka, dugmad) — zamjenjuje traku iz PocetniEkran
  ui/sank/SankEkran.kt        NOVO: S0 ili S1 prema aktivnoj noći, PROSTOR-01, PIN prozor za podešavanja
  ui/sank/NovaNocEkran.kt, NovaNocViewModel.kt, IspomocProzor.kt    NOVO (S0, S0.1)
  ui/sank/UnosEkran.kt, UnosViewModel.kt                            NOVO (S1, S1.1, T1, T2)
  ui/sank/KoKolona.kt, PicaKolona.kt, TuraKolona.kt, PosljednjeTraka.kt   NOVO (dijelovi S1)
  ui/podesavanja/PodesavanjaNav.kt   početna ruta „sank“ umjesto „pocetni“
  ui/pocetni/PocetniEkran.kt  BRISANJE
app/src/test/java/dev/nextpixel/cafebar919/   testovi u istim paketima
```

## Zajednički tipovi (koriste ih taskovi 4–9)

```kotlin
// domain/Nacrt.kt
@Serializable data class StavkaNacrta(val artikalId: Long, val mjeraId: Long, val kolicina: Int)

// repo/TuraRepo.kt
sealed interface Potvrda {
    data class Upisana(val turaId: Long, val ime: String, val brojStavki: Int, val noviKljuc: String) : Potvrda
    data class Odbijena(val razlog: Odbijanje) : Potvrda
    data object NijeUpisana : Potvrda   // UPIS-01: izuzetak baze, nacrt ostaje
}

// data/Relacije.kt
data class TuraSaStavkama(
    @Embedded val tura: TuraE,
    @Relation(parentColumn = "id", entityColumn = "tura_id") val stavke: List<StavkaTureE>,
    @Relation(parentColumn = "primalac_id", entityColumn = "id") val primalac: NocPrimalacE,
)
data class StavkaPrimaoca(val primalacId: Long, val uZbiru: String, val mjeraRedoslijed: Int, val kolicina: Int)
```

---

### Task 1: Pravila ture u `domain/`

**Fajlovi:**
- Izmijeniti: `domain/Format.kt`, `domain/Ishod.kt`, `domain/Validacija.kt`
- Napraviti: `domain/Nacrt.kt`, `domain/Rok.kt`, `domain/Pretraga.kt`, `domain/Obracun.kt`
- Test: `domain/FormatTest.kt` (dopuna), `domain/NacrtTest.kt`, `domain/RokTest.kt`, `domain/PretragaTest.kt`, `domain/ObracunTest.kt`, `domain/ValidacijaTest.kt` (dopuna)

**Interfejsi:**
- Daje:
  - `Format.vrijeme(ms: Long, zona: ZoneId): String` („23:47“, 24 h)
  - `Format.mjeraNaPlocici(naziv: String, litara: Double?): String` (spec 7 #13: „komad“, „doza 0,03 l“, „boca 0,70 l“)
  - `Format.mjeraUStavci(naziv: String, litara: Double?, kolicina: Int): String` („komad“, „doza · 0,06 l“, „bokal · 0,25 l“)
  - `Format.natpisUTuri(natpis: String, kolicina: Int): String` („Bokal · 1“)
  - `data class Format.StavkaSazetka(val naziv: String, val uZbiru: String, val litara: Double?, val kolicina: Int)` i `Format.sazetakTure(stavke: List<StavkaSazetka>): String` (spec 7 #12)
  - `Format.litreKategorija(l: List<Obracun.KategorijaLitre>): String` („Žestina 0,31 l · Vino 0,20 l“, prazno kad je lista prazna)
  - `Format.datumMalim(d: LocalDate): String` („subota 21. 11.“)
  - `Odbijanje` dobija `PRAZNA_TURA, BEZ_PRIMAOCA, NEMA_NOCI, ROK_ISTEKAO, VEC_PONISTENA`
  - `Validacija.provjeriIspomoc(ime: String, zauzeta: List<String>): Ishod` (prazno → `NAZIV_PRAZAN`, > 20 → `NAZIV_PREDUG`, „Šank“ ili ime iz `zauzeta` po `kljucNaziva` → `NAZIV_POSTOJI`)
  - `object Nacrt { const val MAX_KOLICINA = 99; fun dodaj(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long): List<StavkaNacrta>; fun promijeni(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long, delta: Int): List<StavkaNacrta>; fun kolicina(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long): Int; fun uJson(s: List<StavkaNacrta>): String; fun izJsona(json: String): List<StavkaNacrta>; fun porukaPotvrde(imaPica: Boolean, imaPrimaoca: Boolean, imaStavki: Boolean): String? }`
  - `object Rok { fun preostaloSekundi(potvrdjenaUMs: Long, sadaMs: Long, rokS: Int): Int }`
  - `object Pretraga { fun normalizuj(s: String): String; fun odgovara(naziv: String, upit: String): Boolean }`
  - `object Obracun { data class StavkaObracuna(val kategorijaId: Long, val kategorijaNaziv: String, val kategorijaRedoslijed: Int, val uZbiru: String, val mjeraRedoslijed: Int, val litara: Double?, val kolicina: Int); data class KategorijaLitre(val naziv: String, val litara: Double); fun zbirMjera(s: List<StavkaObracuna>): String; fun litrePoKategoriji(s: List<StavkaObracuna>): List<KategorijaLitre> }`

- [ ] **Korak 1: Napraviti granu**

```bash
git checkout main && git pull && git checkout -b faza-2
```

- [ ] **Korak 2: Napisati testove koji padaju**

```kotlin
// FormatTest (dopuna)
@Test fun mjeraNaPlocici() {
    assertEquals("komad", Format.mjeraNaPlocici("Komad", null))
    assertEquals("doza 0,03 l", Format.mjeraNaPlocici("Doza", 0.03))
    assertEquals("boca 0,70 l", Format.mjeraNaPlocici("Boca 0,7", 0.70))
    assertEquals("boca 1,00 l", Format.mjeraNaPlocici("Boca 1 l", 1.0))
}
@Test fun mjeraUStavci() {
    assertEquals("komad", Format.mjeraUStavci("Komad", null, 4))
    assertEquals("doza · 0,06 l", Format.mjeraUStavci("Doza", 0.03, 2))
    assertEquals("doza · 0,09 l", Format.mjeraUStavci("Doza", 0.03, 3))
    assertEquals("bokal · 0,25 l", Format.mjeraUStavci("Bokal", 0.25, 1))
}
@Test fun sazetakTure_imePaBroj() = assertEquals(
    "Jelen pivo 4 · Jack Daniel’s doza 2 · Rakija šljiva bok. 1",
    Format.sazetakTure(listOf(
        Format.StavkaSazetka("Jelen pivo", "kom", null, 4),
        Format.StavkaSazetka("Jack Daniel’s", "doza", 0.03, 2),
        Format.StavkaSazetka("Rakija šljiva", "bok.", 0.25, 1),
    )),
)
@Test fun vrijemeIDatumMalim() {
    val z = ZoneId.of("Europe/Sarajevo")
    assertEquals("23:47", Format.vrijeme(LocalDateTime.of(2026, 11, 20, 23, 47).atZone(z).toInstant().toEpochMilli(), z))
    assertEquals("05:03", Format.vrijeme(LocalDateTime.of(2026, 11, 21, 5, 3).atZone(z).toInstant().toEpochMilli(), z))
    assertEquals("subota 21. 11.", Format.datumMalim(LocalDate.of(2026, 11, 21)))
}
@Test fun natpisILitreKategorija() {
    assertEquals("Bokal · 1", Format.natpisUTuri("Bokal", 1))
    assertEquals("Žestina 0,31 l · Vino 0,20 l", Format.litreKategorija(listOf(Obracun.KategorijaLitre("Žestina", 0.31), Obracun.KategorijaLitre("Vino", 0.2))))
    assertEquals("", Format.litreKategorija(emptyList()))
}

// NacrtTest
@Test fun dodaj_novaStavkaNaKrajIstaSePovecava() {
    val a = Nacrt.dodaj(Nacrt.dodaj(emptyList(), 1, 10), 2, 20)
    assertEquals(listOf(StavkaNacrta(1, 10, 1), StavkaNacrta(2, 20, 1)), a)
    assertEquals(listOf(StavkaNacrta(1, 10, 2), StavkaNacrta(2, 20, 1)), Nacrt.dodaj(a, 1, 10))
}
@Test fun promijeni_minusNaJedanUklanja() = assertEquals(emptyList<StavkaNacrta>(), Nacrt.promijeni(listOf(StavkaNacrta(1, 10, 1)), 1, 10, -1))
@Test fun kolicina_najvise99() {
    val s = listOf(StavkaNacrta(1, 10, 99))
    assertEquals(s, Nacrt.dodaj(s, 1, 10)); assertEquals(s, Nacrt.promijeni(s, 1, 10, +1))
}
@Test fun json_tamoIVratiIOstecenoJePrazno() {
    val s = listOf(StavkaNacrta(1, 10, 3), StavkaNacrta(2, 20, 1))
    assertEquals(s, Nacrt.izJsona(Nacrt.uJson(s)))
    assertEquals(emptyList<StavkaNacrta>(), Nacrt.izJsona("{nije json"))
    // nula/negativno se izbacuje, preko 99 se svodi na 99, dupli par se spaja
    assertEquals(listOf(StavkaNacrta(1, 10, 99)), Nacrt.izJsona("""[{"artikalId":1,"mjeraId":10,"kolicina":150},{"artikalId":2,"mjeraId":20,"kolicina":0}]"""))
}
@Test fun porukaPotvrde_spec17() {
    assertEquals("Prvo treba dodati pića.", Nacrt.porukaPotvrde(imaPica = false, imaPrimaoca = true, imaStavki = false))
    assertEquals("Izaberi konobara i dodaj piće.", Nacrt.porukaPotvrde(true, false, false))
    assertEquals("Dodaj piće u turu.", Nacrt.porukaPotvrde(true, true, false))
    assertEquals("Izaberi konobara.", Nacrt.porukaPotvrde(true, false, true))
    assertNull(Nacrt.porukaPotvrde(true, true, true))
}

// RokTest (rok 60 s, potvrđeno u t = 1_000_000)
@Test fun preostalo_zaokruzujeNavise() {
    assertEquals(60, Rok.preostaloSekundi(1_000_000, 1_000_000, 60))
    assertEquals(41, Rok.preostaloSekundi(1_000_000, 1_019_500, 60))
    assertEquals(1, Rok.preostaloSekundi(1_000_000, 1_059_999, 60))
    assertEquals(0, Rok.preostaloSekundi(1_000_000, 1_060_000, 60))
}
@Test fun preostalo_rokNulaNikad() = assertEquals(0, Rok.preostaloSekundi(1_000_000, 1_000_000, 0))
@Test fun preostalo_satUnazadNemaRoka() = assertEquals(0, Rok.preostaloSekundi(1_000_000, 999_000, 60))

// PretragaTest
@Test fun odgovara_poPocetkuRijeciBezKvacica() {
    assertTrue(Pretraga.odgovara("Jack Daniel’s", "jack"))
    assertTrue(Pretraga.odgovara("Jack Daniel’s", "jack d"))
    assertTrue(Pretraga.odgovara("Coca-Cola", "cola"))
    assertTrue(Pretraga.odgovara("Rakija šljiva", "SLJ"))
    assertTrue(Pretraga.odgovara("Đus narandža", "dus"))
    assertFalse(Pretraga.odgovara("Jelen pivo", "len"))
    assertFalse(Pretraga.odgovara("Jelen pivo", "mojito"))
    assertTrue(Pretraga.odgovara("Jelen pivo", "   "))
}

// ObracunTest — brojevi iz inventara E (Vuleta: kom 22, doza 26, bok. 4; žestina 1,78 l)
@Test fun vuleta_zbirILitre() {
    val zest = { m: String, r: Int, l: Double, k: Int -> Obracun.StavkaObracuna(2, "Žestina", 1, m, r, l, k) }
    val s = listOf(
        Obracun.StavkaObracuna(1, "Pivo", 0, "kom", 0, null, 22),
        zest("doza", 1, 0.03, 26), zest("bok.", 2, 0.25, 4),
    )
    assertEquals("kom 22 · doza 26 · bok. 4", Obracun.zbirMjera(s))
    assertEquals(listOf(Obracun.KategorijaLitre("Žestina", 1.78)), Obracun.litrePoKategoriji(s))
}
@Test fun litre_kategorijeRedoslijedomBezNula() {
    val s = listOf(
        Obracun.StavkaObracuna(3, "Vino", 2, "čaša", 0, 0.2, 1),
        Obracun.StavkaObracuna(2, "Žestina", 1, "doza", 1, 0.03, 10),
        Obracun.StavkaObracuna(1, "Pivo", 0, "kom", 0, null, 5),
    )
    assertEquals(listOf(Obracun.KategorijaLitre("Žestina", 0.30), Obracun.KategorijaLitre("Vino", 0.20)), Obracun.litrePoKategoriji(s))
}

// ValidacijaTest (dopuna)
@Test fun provjeriIspomoc_odbijaPraznoSankIZauzeto() {
    val zauzeta = listOf("Vuleta", "Srđan")
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PRAZAN), Validacija.provjeriIspomoc("   ", zauzeta))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), Validacija.provjeriIspomoc(" vuleta ", zauzeta))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), Validacija.provjeriIspomoc("šank", zauzeta))
    assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PREDUG), Validacija.provjeriIspomoc("A".repeat(21), zauzeta))
    assertEquals(Ishod.Ok, Validacija.provjeriIspomoc("Dejan M.", zauzeta))
}
```

- [ ] **Korak 3: Pokrenuti, očekuje se pad (nedefinisane funkcije)**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.domain.*'`
Expected: FAIL, kompilacija ne prolazi.

- [ ] **Korak 4: Implementirati**

- `osnova` mjere = naziv bez količine na kraju (postojeći `KOLICINA_NA_KRAJU`), malim slovima `Locale.forLanguageTag("sr")`. `mjeraNaPlocici` = za `litara == null` samo naziv malim slovima, inače `"$osnova ${litreSaJedinicom(litara)}"`. `mjeraUStavci` = `"$osnova · ${litreSaJedinicom(litara * kolicina)}"`.
- Litre se sabiraju kao `BigDecimal` (`BigDecimal.valueOf(litara) * kolicina`), da 26 × 0,03 + 4 × 0,25 bude tačno 1,78. Kategorija sa zbirom 0 i stavke bez litara se izostavljaju. Redoslijed: `kategorijaRedoslijed`, pa `kategorijaId`.
- `Obracun.zbirMjera` mapira na `Format.MjeraBroj` i zove postojeći `Format.zbirMjera`.
- `Pretraga.normalizuj`: malim slovima (sr), `đ`→`d`, NFD i uklanjanje `\p{Mn}`. Riječi naziva se dijele na `[\s\-–’'.,/]+`. Upit se dijeli na razmake, a prazan upit odgovara svemu.
- `Rok.preostaloSekundi`: `proteklo = sada − potvrdjena`. Ako je `proteklo < 0` ili `proteklo >= rok·1000`, vraća 0. Inače `ceil((rok·1000 − proteklo) / 1000)`.
- `Nacrt.izJsona` koristi `Json { ignoreUnknownKeys = true }`. Svaki izuzetak vraća praznu listu.

- [ ] **Korak 5: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.domain.*'`
Expected: PASS.

- [ ] **Korak 6: Commit**

```bash
git add app/src && git commit -m "Pravila ture: nacrt, rok bez PIN-a, pretraga, litre po kategoriji, formati"
```

---

### Task 2: Upiti baze i trajni upis

**Fajlovi:**
- Napraviti: `data/Relacije.kt`
- Izmijeniti: `data/dao/NocDao.kt`, `data/AppDatabase.kt`
- Test: `data/NocDaoTest.kt`, `data/BazaTest.kt` (dopuna)

**Interfejsi:**
- Daje (`NocDao`):
  - `fun aktivna(): Flow<NocE?>` i `suspend fun aktivnaJednom(): NocE?` — `WHERE zatvorena_u IS NULL ORDER BY id DESC LIMIT 1`
  - `fun primaoci(nocId: Long): Flow<List<NocPrimalacE>>` i `suspend fun primaociJednom(nocId: Long): List<NocPrimalacE>` — `ORDER BY redoslijed, id`
  - `suspend fun nacrt(): NacrtTureE?` (`id = 1`)
  - `suspend fun turaPoKljucu(kljuc: String): TuraE?`, `suspend fun tura(id: Long): TuraE?`, `@Update suspend fun izmijeniTuru(t: TuraE)`
  - `@Transaction fun posljednjeTure(nocId: Long, n: Int): Flow<List<TuraSaStavkama>>` — `ORDER BY id DESC LIMIT :n`
  - `fun stavkePoPrimaocu(nocId: Long): Flow<List<StavkaPrimaoca>>` — stavke neponištenih tura te noći (`t.primalac_id AS primalacId, s.mjera_u_zbiru AS uZbiru, s.mjera_redoslijed AS mjeraRedoslijed, s.kolicina`)
- `AppDatabase.napravi` pri svakom otvaranju postavlja `PRAGMA synchronous = FULL` (Room `Callback.onOpen`). Razlog: potvrđena tura ne smije nestati ni kad tablet ostane bez struje odmah poslije potvrde.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
// NocDaoTest (Robolectric, baza u memoriji)
@Test fun aktivna_samoNezatvorena() = runTest {
    dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1, zatvorenaU = 2))
    assertNull(dao.aktivnaJednom())
    val id = dao.dodajNoc(NocE(datum = 20_001, otvorenaU = 3))
    assertEquals(id, dao.aktivna().first()?.id)
}
@Test fun posljednjeTure_najnovijePrvoSaStavkamaIPrimaocem() = runTest {
    // noć, primalac „Vuleta“, 12 tura po jedna stavka
    val ture = dao.posljednjeTure(nocId, 10).first()
    assertEquals(10, ture.size)
    assertEquals(posljednjiTuraId, ture.first().tura.id)
    assertEquals("Vuleta", ture.first().primalac.ime)
    assertEquals(1, ture.first().stavke.size)
}
@Test fun stavkePoPrimaocu_bezPonistenih() = runTest {
    // dvije ture istom primaocu, druga poništena (ponistena_u != null)
    assertEquals(listOf(4), dao.stavkePoPrimaocu(nocId).first().map { it.kolicina })
}

// BazaTest (dopuna)
@Test fun synchronousJeFull() {
    val v = db.openHelper.writableDatabase.query("PRAGMA synchronous").use { it.moveToFirst(); it.getInt(0) }
    assertEquals(2, v)
}
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.data.*'`
Expected: FAIL (nema upita / synchronous je 1).

- [ ] **Korak 3: Implementirati upite i `onOpen`**

`TuraSaStavkama` i `StavkaPrimaoca` iz „Zajednički tipovi“, u `data/Relacije.kt`. Ako `execSQL("PRAGMA synchronous = FULL")` u `onOpen` baci izuzetak, koristi se `query(...).close()`.

- [ ] **Korak 4: Pokrenuti sve testove i provjeriti šemu**

Run: `./gradlew testDebugUnitTest && git diff --exit-code app/schemas`
Expected: PASS, šema nepromijenjena.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "Upiti za aktivnu noć, ture i zbirove; PRAGMA synchronous=FULL"
```

---

### Task 3: `NocRepo` — otvaranje noći

**Fajlovi:**
- Napraviti: `repo/NocRepo.kt`
- Izmijeniti: `AppContainer.kt` (`fun noc(): NocRepo`, isti obrazac kao `repo()`)
- Test: `repo/NocRepoTest.kt`

**Interfejsi:**
- Troši: `NocDao` iz Taska 2, `Validacija.provjeriIspomoc` iz Taska 1.
- Daje: `class NocRepo(db: AppDatabase, clock: Clock)` sa
  - `val aktivna: Flow<NocE?>`
  - `fun primaoci(nocId: Long): Flow<List<NocPrimalacE>>`
  - `suspend fun otvoriNoc(radniciIds: List<Long>, ispomoc: List<String>): Long` — vraća id noći.

Pravila `otvoriNoc` (jedna `withTransaction`):
- Ako aktivna noć već postoji, vraća njen id i ništa ne mijenja. To pokriva dvostruki dodir na „Otvori noć“ i pravilo „najviše jedna aktivna noć“ (napomena iz faze 1).
- `datum = LocalDate.now(clock).toEpochDay()`, `otvorena_u = clock.millis()`.
- Primaoci, redom: konobari, pa „za šankom“ (svaka grupa redoslijedom radnika), pa ispomoć redom kako je upisana, pa „Šank“ (`vrsta = SANK`, `ime = "Šank"`, `radnik_id = null`). `redoslijed` ide 0, 1, 2… Uloga i ime se uzimaju iz trenutnog reda `radnik`. Neaktivni i nepostojeći id-evi se preskaču. Ime ispomoći koje ne prođe `provjeriIspomoc` se preskače (zaštita, jer ViewModel već validira).
- Nacrt se postavlja na prazan: novi UUID `kljuc`, `primalac_id = null`, `stavke = "[]"`.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun otvoriNoc_primaociRedomISankNaKraju() = runTest {
    val (vuleta, srdjan, milos) = ids("Vuleta", "Srđan", "Miloš")
    val noc = repo.otvoriNoc(listOf(milos, vuleta, srdjan), listOf("Dejan"))
    val p = repo.primaoci(noc).first()
    assertEquals(listOf("Vuleta", "Srđan", "Miloš", "Dejan", "Šank"), p.map { it.ime })
    assertEquals(listOf(KONOBAR, KONOBAR, ZA_SANKOM, ISPOMOC, SANK), p.map { it.vrsta })
}
@Test fun otvoriNoc_bezIkogaImaSamoSank() = runTest {
    assertEquals(listOf("Šank"), repo.primaoci(repo.otvoriNoc(emptyList(), emptyList())).first().map { it.ime })
}
@Test fun otvoriNoc_dvaputVracaIstuNoc() = runTest {
    val a = repo.otvoriNoc(emptyList(), emptyList()); val b = repo.otvoriNoc(ids("Vuleta").toList(), emptyList())
    assertEquals(a, b); assertEquals(1, brojNoci())
}
@Test fun otvoriNoc_prazniNacrt() = runTest { /* nacrt sa stavkama prije → poslije: stavke "[]", primalac null, nov kljuc */ }
@Test fun otvoriNoc_preskaceIskljucenogRadnika() = runTest { /* isključen Smitran nije među primaocima */ }
@Test fun datumNociOstajePoslijePonoci() = runTest {
    // sat 20. 11. 23:50 → otvoriNoc → sat +2 h → datum aktivne noći je i dalje 20. 11.
}
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.repo.NocRepoTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati `NocRepo` i `AppContainer.noc()`**

- [ ] **Korak 4: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.repo.*'`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "Otvaranje noći: snimak primalaca, Šank uvijek, jedna aktivna noć"
```

---

### Task 4: `TuraRepo` — katalog, nacrt, potvrda, poništavanje u roku

**Fajlovi:**
- Napraviti: `repo/TuraRepo.kt`
- Izmijeniti: `AppContainer.kt` (`fun ture(): TuraRepo`)
- Test: `repo/TuraRepoTest.kt`

**Interfejsi:**
- Troši: `NocDao` (Task 2), `Nacrt`, `Obracun`, `Odbijanje` (Task 1), `ArtikalPrikaz` (faza 1, `repo/Prikazi.kt`).
- Daje: `class TuraRepo(db: AppDatabase, clock: Clock)` sa
  - `val katalog: Flow<List<ArtikalPrikaz>>` — aktivna pića čija je kategorija aktivna, samo aktivne mjere redoslijedom iz `artikal_mjera`, pića bez ijedne mjere izostavljena, redoslijedom `artikal.redoslijed, id`
  - `fun vazeceStavke(stavke: List<StavkaNacrta>, katalog: List<ArtikalPrikaz>): List<StavkaNacrta>` (čista funkcija u companion-u: zadržava stavke čiji par artikal–mjera postoji u katalogu)
  - `suspend fun nacrt(): NacrtTureE` — pravi prazan red sa novim UUID-om ako ga nema
  - `suspend fun sacuvajNacrt(kljuc: String, primalacId: Long?, stavke: List<StavkaNacrta>)`
  - `suspend fun potvrdi(kljuc: String, primalacId: Long?, stavke: List<StavkaNacrta>): Potvrda`
  - `suspend fun ponistiBezPina(turaId: Long): Ishod`
  - `fun posljednje(nocId: Long): Flow<List<TuraSaStavkama>>` (10 tura, spec 7 #32; stavke svake ture sortirane po `id`)
  - `fun zbiroviPoPrimaocu(nocId: Long): Flow<Map<Long, String>>` (vrijednost iz `Obracun.zbirMjera`; primalac bez tura nema ključ)
  - `suspend fun rokBezPinaS(): Int` i `val rokBezPina: Flow<Int>` iz `postavke`

Pravila `potvrdi`:
1. Sve u jednoj `withTransaction`. Ako tura sa tim `kljuc` već postoji, vraća `Upisana` za tu turu i ništa ne upisuje (`noviKljuc` = trenutni ključ nacrta).
2. Nema aktivne noći → `Odbijena(NEMA_NOCI)`. `primalacId` je null ili nije primalac aktivne noći → `Odbijena(BEZ_PRIMAOCA)`. Poslije `vazeceStavke` nad svježim katalogom nema stavki → `Odbijena(PRAZNA_TURA)`.
3. Upisuje turu (`potvrdjena_u = clock.millis()`) i stavke sa snimkom svih polja `StavkaTureE` iz trenutnih redova artikla, kategorije i mjere, redom kao u nacrtu. Zatim nacrt postavlja na prazan sa novim UUID-om.
4. `SQLiteConstraintException` na `kljuc` (trka mimo transakcije) → ponovo čita turu po ključu → `Upisana`. Svaki drugi `Exception` osim `CancellationException` → `Log.e` + `NijeUpisana`. Nacrt u bazi tada ostaje nepromijenjen (transakcija je vraćena).

Pravila `ponistiBezPina` (jedna transakcija): tura ne postoji ili njena noć nije aktivna → `NEMA_NOCI`. Već poništena → `VEC_PONISTENA`. `Rok.preostaloSekundi(potvrdjena_u, sada, rok) == 0` → `ROK_ISTEKAO`. Inače `ponistena_u = sada`, `ponistena_bez_pina = true`, `Ok`.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun potvrdi_upisujeSnimakIPrazniNacrt() = runTest {
    val r = repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 2), StavkaNacrta(jelen, komad, 4)))
    r as Potvrda.Upisana
    assertEquals("Vuleta", r.ime); assertEquals(2, r.brojStavki)
    val st = db.query("SELECT artikal_naziv, mjera_u_zbiru, litara, kolicina FROM stavka_ture ORDER BY id")
    // „Jack Daniel’s“, „doza“, 0.03, 2 ; „Jelen pivo“, „kom“, null, 4
    val n = repo.nacrt(); assertEquals("[]", n.stavke); assertNull(n.primalacId); assertNotEquals(kljuc, n.kljuc); assertEquals(r.noviKljuc, n.kljuc)
}
@Test fun potvrdi_istiKljucDvaputJednaTura() = runTest {
    repo.potvrdi(kljuc, vuleta, stavke); val drugi = repo.potvrdi(kljuc, vuleta, stavke)
    assertTrue(drugi is Potvrda.Upisana); assertEquals(1, brojTura())
}
@Test fun potvrdi_poslijeGreskeIstiKljucJednaTura() = runTest {
    sql("CREATE TRIGGER kvar BEFORE INSERT ON tura BEGIN SELECT RAISE(ABORT, 'kvar'); END")
    repo.sacuvajNacrt(kljuc, vuleta, stavke)
    assertEquals(Potvrda.NijeUpisana, repo.potvrdi(kljuc, vuleta, stavke))
    assertEquals(0, brojTura()); assertEquals(Nacrt.uJson(stavke), repo.nacrt().stavke)   // nacrt ostao
    sql("DROP TRIGGER kvar")
    assertTrue(repo.potvrdi(kljuc, vuleta, stavke) is Potvrda.Upisana); assertEquals(1, brojTura())
}
@Test fun potvrdi_snimakOstajePoslijeIzmjeneArtikla() = runTest {
    repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 1)))
    podesavanja.sacuvajArtikal(jack, "Jack Daniel’s No. 7", zestina, listOf(doza))
    assertEquals("Jack Daniel’s", repo.posljednje(noc).first().single().stavke.single().artikalNaziv)
}
@Test fun potvrdi_preskaceIskljucenArtikal() = runTest {
    podesavanja.postaviAktivanArtikal(jelen, false)
    val r = repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jelen, komad, 2), StavkaNacrta(jack, doza, 1))) as Potvrda.Upisana
    assertEquals(1, r.brojStavki)
}
@Test fun potvrdi_odbijanja() = runTest {
    assertEquals(Potvrda.Odbijena(Odbijanje.BEZ_PRIMAOCA), repo.potvrdi(kljuc, null, stavke))
    assertEquals(Potvrda.Odbijena(Odbijanje.PRAZNA_TURA), repo.potvrdi(kljuc, vuleta, emptyList()))
}
@Test fun ponistiBezPina_uRokuIPoslije() = runTest {
    val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
    sat.pomjeri(Duration.ofSeconds(59)); assertEquals(Ishod.Ok, repo.ponistiBezPina(t))
    assertEquals(Ishod.Odbijeno(Odbijanje.VEC_PONISTENA), repo.ponistiBezPina(t))
    val t2 = (repo.potvrdi(repo.nacrt().kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
    sat.pomjeri(Duration.ofSeconds(60)); assertEquals(Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO), repo.ponistiBezPina(t2))
}
@Test fun ponistiBezPina_satUnazadOdbija() = runTest {
    val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
    sat.pomjeri(Duration.ofMinutes(-10)); assertEquals(Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO), repo.ponistiBezPina(t))
}
@Test fun zbiroviPoPrimaocu_bezPonistenih() = runTest {
    // Vuleta: tura kom 4 + doza 2, pa tura doza 1 poništena → "kom 4 · doza 2"; Srđan bez tura → nema ključa
}
@Test fun katalog_samoAktivnoSaAktivnomKategorijom() = runTest { /* isključena kategorija skriva njena pića */ }
```

Testni podaci se prave kroz `PodesavanjaRepo` (kategorije „Pivo“, „Žestina“, pića „Jelen pivo“ / Komad i „Jack Daniel’s“ / Doza) i `NocRepo.otvoriNoc`. Sat je `MutableClock`.

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.repo.TuraRepoTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati `TuraRepo` i `AppContainer.ture()`**

- [ ] **Korak 4: Pokrenuti sve testove**

Run: `./gradlew testDebugUnitTest && git diff --exit-code app/schemas`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "Potvrda ture u jednoj transakciji, isti ključ = jedna tura, UPIS-01, poništavanje u roku"
```

---

### Task 5: Komponente za šank

**Fajlovi:**
- Napraviti: `ui/components/PlocicaOsobe.kt`, `PlocicaPica.kt`, `StepperTure.kt`, `PoljePretrage.kt`, `Znacka.kt`, `ui/sank/GornjaTraka.kt`
- Test: `ui/components/SankKomponenteTest.kt`

Izgled po Paperu O2 (pločica osobe, pločica pića, stavka ture, pretraga, značke) i S1 (gornja traka). Vrijednosti iz `get_computed_styles`.

**Interfejsi:**
- Daje:
  - `PlocicaOsobe(ime: String, zbir: String, izabrana: Boolean, sank: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)` — 228×80, radius 16. Izabrana: zlatna podloga, tamni tekst, crni krug 28 sa zlatnom kvačicom. `sank = true`: providno, isprekidan okvir `neutralLine`. Semantika `selected`.
  - `PlocicaPica(naziv: String, mjere: List<MjeraNaPlocici>, onDodaj: (mjeraId: Long) -> Unit, modifier: Modifier = Modifier)` sa `data class MjeraNaPlocici(val mjeraId: Long, val natpis: String, val opis: String, val kolicina: Int)`. Jedna mjera: cijela pločica je dugme, donji red je `opis` (`Format.mjeraNaPlocici`), a kad je `kolicina > 0` dobija zlatni okvir 2 i značku-broj. 2–3 mjere: naziv gore i red dugmadi visine 56, natpis dugmeta je `natpis` ili `Format.natpisUTuri(natpis, kolicina)`. Naziv u najviše dva reda, sa „…“.
  - `StepperTure(kolicina: Int, onMinus: () -> Unit, onPlus: () -> Unit, plusOmogucen: Boolean)` — [−] 56×56, broj 24/800 širine 48, [+] 56×56. Opisi „Manje“ i „Više“.
  - `PoljePretrage(upit: String, onUpit: (String) -> Unit, modifier: Modifier = Modifier)` — pill visine 56, lupa, placeholder „Traži piće“, zlatni okvir u fokusu, `ImeAction.Search` sakriva tastaturu.
  - `Znacka(tekst: String, vrsta: VrstaZnacke, ikona: ImageVector? = null)` sa `enum class VrstaZnacke { NOC, UPISANO, PONISTENO, PONISTENO_BEZ_PINA, ZATVORENA }`
  - `GornjaTraka(datum: String, znacka: (@Composable () -> Unit)? = null, desno: @Composable RowScope.() -> Unit)` i `TrakaDugme(tekst: String, ikona: ImageVector, onClick: () -> Unit)`. Ovo je traka koja je do sada bila unutar `PocetniEkran` (visina 72, `Znak919`, razdjelnik, datum `Tekst.section`).

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun plocicaPica_jednaMjera_dodirDodajeIZnackaBroj() { /* onClick → onDodaj(mjeraId); kolicina 2 → vidi se "2" */ }
@Test fun plocicaPica_dvijeMjere_svakoDugmeSvojaMjera() { /* "0,03" → onDodaj(doza), "Bokal · 1" → onDodaj(bokal) */ }
@Test fun plocicaOsobe_izabranaImaSelected() { /* assertIsSelected */ }
@Test fun stepperTure_plusOnemogucenNa99() { /* plusOmogucen=false → "Više" assertIsNotEnabled */ }
@Test fun poljePretrage_placeholderIUpis() { /* "Traži piće"; performTextInput("jack") → onUpit("jack") */ }
@Test fun dodirneZone_najmanje56() { /* dugmad mjera, stepper, pretraga: visina ≥ 56 dp */ }
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.components.SankKomponenteTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati komponente**

- [ ] **Korak 4: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.*'`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "Komponente šanka: pločice osobe i pića, stepper ture, pretraga, značke, gornja traka"
```

---

### Task 6: S0 Nova noć i S0.1 Ispomoć

**Fajlovi:**
- Napraviti: `ui/sank/NovaNocViewModel.kt`, `ui/sank/NovaNocEkran.kt`, `ui/sank/IspomocProzor.kt`
- Test: `ui/sank/NovaNocTest.kt`

**Interfejsi:**
- Troši: `NocRepo.otvoriNoc` (Task 3), `PodesavanjaRepo.radnici` i `postavke` (faza 1), `Validacija.provjeriIspomoc` (Task 1), `GornjaTraka`, `TrakaDugme` (Task 5), `Prozor`, `Polje`, `GlavnoDugme`, `SporednoDugme` (faza 1), `ProstorStanje.provjeri()` (Task 9; do tada se prosljeđuje `{}`).
- Daje:
  - `class NovaNocViewModel(podesavanja: PodesavanjaRepo, noc: NocRepo, clock: Clock, poslijeOtvaranja: () -> Unit) : ViewModel()` sa `val stanje: StateFlow<NovaNocStanje>` i radnjama `prebaci(radnikId: Long)`, `prebaciIspomoc(ime: String)`, `otvoriIspomoc()`, `zatvoriIspomoc()`, `imeIspomoci(s: String)`, `dodajIspomoc()`, `otvoriNoc()`. `poslijeOtvaranja` poziva provjeru prostora.
  - `data class NovaNocStanje(val danas: LocalDate, val konobari: List<RadnikE>, val zaSankom: List<RadnikE>, val oznaceni: Set<Long>, val ispomoc: List<Pair<String, Boolean>>, val ispomocDozvoljena: Boolean, val prozorIspomoci: String?, val greskaIspomoci: String?, val otvara: Boolean)` — `prozorIspomoci != null` znači da je S0.1 otvoren, a vrijednost je upisano ime.
  - `NovaNocEkran(vm: NovaNocViewModel, onPodesavanja: () -> Unit)`

Tekstovi (inventar S0, S0.1):
- traka: `Format.datumNoci(danas)`, desno samo „Podešavanja“ (ikona katanac)
- nadnaslov „Nema otvorene noći“, naslov „Ko radi večeras?“, podnaslov „Označi ko radi. Samo oni se prikazuju na ekranu za unos.“
- kolone „Konobari“, „Za šankom“, „Ispomoć · samo večeras“ (treća samo kad je `ispomocDozvoljena`, spec 7 #30). Kartica 280×80: ime i uloga „konobar“ / „za šankom“ / „ispomoć“. Isprekidana kartica „+ Dodaj ispomoć“ otvara S0.1. Ispomoć se označava i odznačava dodirom kao i ostali.
- dugme „Otvori noć · {n} {radnik|radnika}“ (`Format.brojac(n, "radnik", "radnika", "radnika")`, n = označeni + označena ispomoć, 0 je dozvoljeno), ikona mjesec, pune širine 920×64
- napomena „Šank za prosuto je uvijek uključen. Datum noći ostaje {Format.datumMalim(danas)} i poslije ponoći.“
- S0.1: naslov „Ispomoć večeras“; tekst „Ime se pamti samo za ovu noć. Ispomoć ima svoj zbir u pregledu, a sutra se ne nudi.“; polje „Ime“ (`max = Validacija.IME_MAX`), pomoćni tekst „Kako ga zovu na šanku, npr. „Dejan“ ili „Dejan M.““; dugmad „Odustani“ i „Dodaj“. Greške ispod polja: `NAZIV_PRAZAN` → „Upiši ime.“, `NAZIV_POSTOJI` → „To ime već postoji večeras.“ `zauzeta` = svi prikazani stalni radnici i sva dodata ispomoć.
- Centralni blok se skroluje kad radnika ima više nego što staje (Review 4).
- Dok `otvara`, dugme je „Upisuje se…“ i drugi dodir se ignoriše.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun s0_prikazujeAktivneRadnikeNeoznacene() { /* Vuleta, Srđan pod „Konobari“, Miloš, Smitran pod „Za šankom“; „Otvori noć · 0 radnika“ */ }
@Test fun s0_brojRadnikaSeSlaze() { /* označi Vuleta → „Otvori noć · 1 radnik“; + Srđan → „2 radnika“ */ }
@Test fun s0_ispomocSeDodajeOznacenaIBroji() { /* „+ Dodaj ispomoć“ → upiši „Dejan“ → „Dodaj“ → kartica „Dejan“ + „Otvori noć · 1 radnik“ */ }
@Test fun ispomoc_dupliNazivOstajeUProzoru() { /* „vuleta“ → „To ime već postoji večeras.“, prozor ostaje, kartice nema */ }
@Test fun ispomoc_praznoIme() { /* „  “ → „Upiši ime.“ */ }
@Test fun s0_ispomocIskljucenaUPravilima() { /* postaviIspomoc(false) → nema kolone „Ispomoć · samo večeras“ */ }
@Test fun s0_otvoriNocUpisujeOznacene() { /* označi Vuleta + Dejan → Otvori → aktivna noć ima primaoce Vuleta, Dejan, Šank */ }
@Test fun s0_napomenaSaDatumom() { /* sat 21. 11. → „…Datum noći ostaje subota 21. 11. i poslije ponoći.“ */ }
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.NovaNocTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati ViewModel, ekran i prozor (+ `Fabrika(container)` kao u fazi 1)**

- [ ] **Korak 4: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.*'`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "S0 Nova noć i S0.1 Ispomoć"
```

---

### Task 7: `UnosViewModel` — stanje S1, nacrt i potvrda

**Fajlovi:**
- Napraviti: `ui/sank/UnosViewModel.kt`
- Test: `ui/sank/UnosViewModelTest.kt`

**Interfejsi:**
- Troši: `TuraRepo` (Task 4), `NocRepo` (Task 3), `Nacrt`, `Pretraga`, `Obracun`, `Format` (Task 1).
- Daje: `class UnosViewModel(ture: TuraRepo, noc: NocRepo, clock: Clock) : ViewModel()` sa
  - `val stanje: StateFlow<UnosStanje?>` (null dok se ne učita noć i nacrt)
  - `val dogadjaji: SharedFlow<UnosDogadjaj>` sa `sealed interface UnosDogadjaj { data class Upisana(val turaId: Long, val ime: String, val brojStavki: Int, val rokS: Int) }`
  - radnje: `izaberiPrimaoca(id: Long)` (dodir na izabranog ga odznačava), `dodaj(artikalId: Long, mjeraId: Long)`, `promijeni(artikalId: Long, mjeraId: Long, delta: Int)`, `izaberiKategoriju(id: Long?)` (null = „Sve“), `upit(s: String)` (neprazan upit postavlja „Sve“, odluka #35), `potvrdi()`, `ponisti(turaId: Long)`
  - `data class UnosStanje(val noc: NocE, val primaoci: List<NocPrimalacE>, val zbirovi: Map<Long, String>, val kategorije: List<KategorijaE>, val izabranaKategorija: Long?, val upit: String, val pica: List<ArtikalPrikaz>, val imaPica: Boolean, val primalacId: Long?, val stavke: List<StavkaNacrta>, val upisuje: Boolean, val greskaUpisa: Boolean, val posljednje: List<TuraSaStavkama>, val rokS: Int)`. `pica` je katalog filtriran kategorijom i upitom, `stavke` su samo važeće (`TuraRepo.vazeceStavke`), a `imaPica` znači da katalog nije prazan.

Pravila:
- Na početku se čita `ture.nacrt()`. Njegov `kljuc` se drži u privatnom polju ViewModel-a. Primalac iz nacrta koji nije primalac aktivne noći postaje null.
- Svaka izmjena nacrta odmah mijenja stanje u memoriji, a zatim ga upisuje u bazu (`sacuvajNacrt`) **pod jednim `Mutex`-om**. Upis čita stanje tek kad dobije bravu, pa kasniji upis nikad ne pregazi noviji.
- Dok je `upisuje = true`, sve izmjene ture i drugi `potvrdi()` se ignorišu. `upisuje` se postavlja sinhrono, prije pokretanja korutine.
- `potvrdi()` uzima isti `Mutex` i zove `ture.potvrdi(kljuc, primalacId, stavke)`:
  - `Upisana` → stanje: prazne stavke, `primalacId = null`, `greskaUpisa = false`; privatni `kljuc = noviKljuc`; emituje `UnosDogadjaj.Upisana`
  - `NijeUpisana` → `greskaUpisa = true`, stavke i primalac ostaju; sljedeći `potvrdi()` je „Pokušaj ponovo“ sa istim ključem
  - `Odbijena` → bez promjene, `greskaUpisa = false`
- `ponisti(turaId)` zove `ture.ponistiBezPina`. Odbijanje ne mijenja ništa, jer traka sama skriva dugme kad rok istekne.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun potvrda_prazniTuruIOdznacavaPrimaoca() = runTest { /* izaberi Vuleta, dodaj jack/doza ×2 → potvrdi → stavke prazne, primalacId null, dogadjaj Upisana("Vuleta", 1) */ }
@Test fun potvrdi_dvaDodiraJednaTura() = runTest { /* vm.potvrdi(); vm.potvrdi() bez čekanja → 1 tura u bazi */ }
@Test fun upis01_turaOstajeIPonovoUpisujeJednom() = runTest {
    /* trigger „kvar“ → potvrdi → greskaUpisa true, stavke iste → DROP TRIGGER → potvrdi → 1 tura, greskaUpisa false */
}
@Test fun nacrt_preziviNoviViewModel() = runTest { /* izaberi Srđan, dodaj jelen ×3 → novi UnosViewModel nad istom bazom → primalac Srđan, jelen 3 */ }
@Test fun nacrt_iskljucenoPiceNestaje() = runTest { /* nacrt sa jelen i jack → isključi jelen u podešavanjima → stavke = samo jack */ }
@Test fun pretraga_biraSveIFiltrira() = runTest { /* izaberiKategoriju(pivo) → upit("jack") → izabranaKategorija null, pica = [Jack Daniel’s] */ }
@Test fun kategorije_samoSaAktivnimPicem() = runTest { /* prazna kategorija „Vino“ nije među kategorijama */ }
@Test fun izabranaKategorijaNestaneVracaSve() = runTest { /* izabrana „Pivo“, isključi oba piva → izabranaKategorija null */ }
@Test fun izaberiIstogPrimaocaOdznacava() = runTest { }
@Test fun ponistiURoku_turaPrecrtanaIVanZbira() = runTest { /* potvrdi → ponisti(turaId) → posljednje.first().tura.ponistenaBezPina, zbirovi bez nje */ }
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.UnosViewModelTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati `UnosViewModel` (+ `Fabrika(container)`)**

- [ ] **Korak 4: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.*'`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "S1 stanje: nacrt pod bravom, potvrda bez duple ture, UPIS-01, filteri"
```

---

### Task 8: S1 ekran — kolone, traka „Posljednje“, toast, T1 i T2

**Fajlovi:**
- Napraviti: `ui/sank/UnosEkran.kt`, `KoKolona.kt`, `PicaKolona.kt`, `TuraKolona.kt`, `PosljednjeTraka.kt`
- Test: `ui/sank/UnosEkranTest.kt`

**Interfejsi:**
- Troši: `UnosViewModel` (Task 7), komponente iz Taska 5, `ToastStanje`/`ToastHost`, `Baner`, `GlavnoDugme`, `SporednoDugme` (faza 1), `Rok.preostaloSekundi`, `Format.*` (Task 1).
- Daje: `UnosEkran(vm: UnosViewModel, clock: Clock, onPodesavanja: () -> Unit)`

Raspored i tekstovi (inventar S1, S1.1, T1, T2; spec 7 #9, #12, #14, #17, #18, #24, #29, #32):
- **Traka:** `Format.datumNoci(noc.datum)`, značka `NOC` „Noć otvorena od {Format.vrijeme(otvorena_u)}“, desno samo „Podešavanja“.
- **Lijevo (276):** grupe „KONOBARI“, „ZA ŠANKOM“, „ISPOMOĆ“ (samo grupe koje imaju nekoga), dole „VAN ZBIRA“ sa Šankom. Zbir je `zbirovi[id]`. Bez tura piše „još ništa“, a Šank bez tura „prosuto i ostalo“. Kolona se skroluje.
- **Sredina:**
  - Kad `imaPica == false` (T2): nema čipova ni pretrage. Krug sa čašom, „Još nema pića“, „Prije prve ture vlasnik unosi pića i mjere u podešavanjima. Treba mu PIN.“, zlatno dugme „Podešavanja“.
  - Inače red `Pilula` („Sve“ + kategorije) i `PoljePretrage`, ispod `LazyVerticalGrid` sa 3 kolone, pločice 192×120, razmak 12. Pločica dobija mjere iz `ArtikalPrikaz.mjere` sa `Nacrt.kolicina`. Dodir pri 99 se ignoriše.
  - Pretraga bez rezultata (T1): lupa u krugu, „Nema pića „{upit}““, „Provjeri naziv ili traži po prvim slovima. Novo piće dodaje vlasnik u podešavanjima.“ i sporedno dugme „Očisti pretragu“.
- **Desno (364, surface):**
  - Bez primaoca: „TURA“. Sa primaocem: „TURA ZA“ + ime 28 bold (#18).
  - Bez stavki: „+“ u krugu, „Nova tura“, „Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.“
  - Stavke (skroluju se): naziv + `Format.mjeraUStavci` + `StepperTure`.
  - Zbirni red: lijevo `Obracun.zbirMjera` ture, desno `Format.litreKategorija(Obracun.litrePoKategoriji(...))`.
  - `greskaUpisa`: na vrhu kolone `Baner(GRESKA, "Tura nije upisana.", "Tablet nije uspio da sačuva podatke. Tura je ostala ovdje, ništa nije izgubljeno.")`. Dugme je „Pokušaj ponovo“ (ikona `Refresh`), a ispod piše „Šifra: UPIS-01“.
  - Dugme dole, pune širine, visine 64: „Potvrdi · {ime}“ (ikona kvačica) ili onemogućeno „Potvrdi“ sa `Nacrt.porukaPotvrde(...)` ispod (13, muted). Dok `upisuje`: „Upisuje se…“. Dugme je uvijek u dnu kolone, van skrola.
- **Dole (72):** „POSLJEDNJE“, pa vodoravni `LazyRow` čipova najnovijih tura: vrijeme, ime (bold) i `Format.sazetakTure` u jednom redu sa „…“. Kad je `Rok.preostaloSekundi > 0`, dodaje se dugme „Poništi · {Format.odbrojavanje(s)}“. Poništena bez PIN-a: sažetak precrtan i značka „Poništeno bez PIN-a“. Bez tura piše „Još nema tura ove noći.“ „Sada“ se osvježava svake sekunde iz `clock` (`LaunchedEffect` petlja sa `delay(1000)`).
- **Toast** (#9): na `UnosDogadjaj.Upisana` → „Upisano: {ime} · {Format.brojac(n, "stavka", "stavke", "stavki")}“. Akcija „Poništi“ postoji samo kad je `rokS > 0`. Toast je dole, iznad trake.
- **Ekran ne gasi:** `DisposableEffect` postavlja `LocalView.current.keepScreenOn = true` i vraća `false` na izlasku.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
@Test fun tokTure_odIzboraDoTrake() { /* Vuleta → „Jack Daniel’s“ ×2 → „Potvrdi · Vuleta“ → toast „Upisano: Vuleta · 1 stavka“ + traka „Jack Daniel’s doza 2“ + „Poništi · 60 s“ */ }
@Test fun potvrdiOnemoguceno_porukeIzSpec17() { /* bez ičega „Izaberi konobara i dodaj piće.“; sa Vuleta „Dodaj piće u turu.“; samo piće „Izaberi konobara.“ */ }
@Test fun t2_bezPicaPokazujePodesavanja() { /* katalog prazan → „Još nema pića“, „Prvo treba dodati pića.“, dugme „Podešavanja“ zove onPodesavanja */ }
@Test fun t1_pretragaBezRezultataIOcisti() { /* upiši „mojito“ → „Nema pića „mojito““ → „Očisti pretragu“ → pločice se vrate */ }
@Test fun poništiUTraci_nestajePoslijeRoka() { /* potvrdi → sat +61 s → mainClock.advanceTimeBy(1100) → nema „Poništi ·“ */ }
@Test fun poništiUTraci_precrtanoIZnacka() { /* „Poništi · 60 s“ → „Poništeno bez PIN-a“ */ }
@Test fun upis01_banerIPokusajPonovo() { /* trigger kvar → Potvrdi → „Tura nije upisana.“, „Šifra: UPIS-01“, dugme „Pokušaj ponovo“ */ }
@Test fun ekranSeNeGasi() { /* LocalView.keepScreenOn == true dok je UnosEkran prikazan */ }
@Test fun mnogoLjudiIPica_sveDostupnoIPotvrdiVidljivo() {
    /* 10 radnika + 3 ispomoći, 60 pića, 15 stavki u turi: performScrollToNode do posljednjeg radnika i pića;
       „Potvrdi · …“ assertIsDisplayed bez skrolovanja */
}
```

Testovi koriste `@Config(qualifiers = "w1280dp-h800dp")`, bazu u memoriji i `MutableClock`, kao u fazi 1.

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.UnosEkranTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati ekran i dijelove**

- [ ] **Korak 4: Pokrenuti, očekuje se prolaz**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.ui.sank.*'`
Expected: PASS.

- [ ] **Korak 5: Commit**

```bash
git add app/src && git commit -m "S1 Unos ture: kolone, traka Posljednje sa poništavanjem u roku, toast, T1, T2, UPIS-01"
```

---

### Task 9: Navigacija šanka i PROSTOR-01

**Fajlovi:**
- Napraviti: `data/Prostor.kt`, `ui/sank/SankEkran.kt`
- Izmijeniti: `AppContainer.kt`, `ui/podesavanja/PodesavanjaNav.kt`, `MainActivity.kt`, `ui/sank/NovaNocViewModel.kt` (`poslijeOtvaranja` → `prostor.provjeri()`)
- Obrisati: `ui/pocetni/PocetniEkran.kt`
- Test: `data/ProstorTest.kt`, `ui/sank/SankNavTest.kt`; izmijeniti `ui/theme/TemaTest.kt` (crta `NovaNocEkran` umjesto `PocetniEkran`) i `ui/podesavanja/PodesavanjaZakljucanoTest.kt` (čeka „Ko radi večeras?“ umjesto teksta privremenog ekrana)

**Interfejsi:**
- Daje:
  - `fun interface Prostor { fun slobodnoBajtova(): Long }`, `class ProstorTableta(dir: File) : Prostor` (`StatFs(dir.path).availableBytes`)
  - `class ProstorStanje(prostor: Prostor) { val nizak: StateFlow<Boolean>; fun provjeri(); fun razumijem() }` — prag `200L * 1024 * 1024` bajtova. `razumijem()` skriva baner do sljedeće `provjeri()`. Izuzetak u `slobodnoBajtova` znači „nije nizak“.
  - `AppContainer(context, clock, prostor: Prostor = ProstorTableta(context.filesDir))` dobija `val prostor: ProstorStanje`. Provjera se radi kad baza postane spremna i poslije otvaranja noći.
  - `SankEkran(container: AppContainer, onPodesavanja: () -> Unit)` — čita `container.noc().aktivna`. `null` → `NovaNocEkran`, noć → `UnosEkran`, a dok se čita crta samo pozadinu. Iznad sadržaja, ispod gornje trake, stoji `Baner(UPOZORENJE, "Na tabletu je ostalo malo prostora.", "Memorija je skoro puna (ispod 200 MB). Sve je sačuvano. Novi unos još radi.", "Razumijem")` dok je `prostor.nizak`.
  - `AppNav(container: AppContainer, navController: NavHostController = rememberNavController())` — parametar `danas` se uklanja. `RUTA_POCETNI` postaje `"sank"` i crta `SankEkran` sa postojećim `PinProzor` tokom za „Podešavanja“. Zaključavanje i „Nazad na unos“ vraćaju na `"sank"`.

- [ ] **Korak 1: Napisati testove koji padaju**

```kotlin
// ProstorTest
@Test fun nizakIspod200MB() { val s = ProstorStanje { 199L * 1024 * 1024 }; s.provjeri(); assertTrue(s.nizak.value); s.razumijem(); assertFalse(s.nizak.value) }
@Test fun dovoljnoProstora() { val s = ProstorStanje { 500L * 1024 * 1024 }; s.provjeri(); assertFalse(s.nizak.value) }

// SankNavTest (AppContainer sa bazom u memoriji ili pravi CafeBarApp kao PodesavanjaZakljucanoTest)
@Test fun bezNoci_S0_otvaranje_S1() { /* „Ko radi večeras?“ → označi Vuleta → Otvori noć → „Noć otvorena od …“ */ }
@Test fun podesavanjaIzS1_iNazadNaUnos_nacrtOstaje() { /* S1: Vuleta + Jack → Podešavanja → PIN 123456 → „Nazad na unos“ → „TURA ZA“ „Vuleta“ i Jack 1 */ }
@Test fun prostor01_banerIRazumijem() { /* prostor 100 MB → baner na S0 → „Razumijem“ → nema banera */ }
```

- [ ] **Korak 2: Pokrenuti, očekuje se pad**

Run: `./gradlew testDebugUnitTest --tests 'dev.nextpixel.cafebar919.data.ProstorTest' --tests 'dev.nextpixel.cafebar919.ui.sank.SankNavTest'`
Expected: FAIL.

- [ ] **Korak 3: Implementirati, obrisati `PocetniEkran`, prepraviti dva postojeća testa**

- [ ] **Korak 4: Pokrenuti sve testove**

Run: `./gradlew testDebugUnitTest && git diff --exit-code app/schemas`
Expected: PASS (svi testovi faze 1 i 2).

- [ ] **Korak 5: Commit**

```bash
git add -A app/src && git commit -m "Šank kao početni ekran (S0/S1), PROSTOR-01, privremeni ekran uklonjen"
```

---

### Task 10: Release APK, instalacija preko faze 1, dokumentacija

**Fajlovi:**
- Izmijeniti: `app/build.gradle.kts` (`versionCode = 2`, `versionName = "1.1"`), `docs/superpowers/specs/2026-11-21-android-aplikacija-design.md` (poglavlje 7: odluke #34–#40 iz ovog plana; poglavlje 8: faza 2 urađena)

- [ ] **Korak 1: Povećati verziju i napraviti release APK**

Run: `./gradlew testDebugUnitTest assembleRelease`
Expected: BUILD SUCCESSFUL, `app/build/outputs/apk/release/app-release.apk`.

- [ ] **Korak 2: Provjeriti potpis**

Run: `$ANDROID_HOME/build-tools/36.1.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`
Expected: `CN=Caffe Bar 919`, SHA-256 počinje sa `37:67:7C:FB`.

- [ ] **Korak 3: Instalirati preko faze 1 na emulatoru tableta**

Run: `adb install -r app/build/outputs/apk/release/app-release.apk`
Expected: `Success`. Podaci koje je korisnik unio u fazi 1 (pića, kategorije, radnici, PIN) ostaju. To se provjerava ulaskom u podešavanja.

- [ ] **Korak 4: Ručna provjera na emulatoru (1280×800)**

Otvori noć sa dva konobara i ispomoći, pa uradi tri ture (jedna na Šank). Zatim:
- poništi jednu turu u roku
- dodaj piće u nacrt, ugasi aplikaciju iz Recents, otvori je ponovo: nacrt je tu
- brzo dva puta dodirni „Potvrdi“: nastaje jedna tura
- ekran se ne gasi dok si na S1
- traži „sljiva“

Slike ekrana S0, S0.1, S1, T1, T2 uporedi sa Paperom.

- [ ] **Korak 5: Upisati odluke u spec i commit**

```bash
git add app/build.gradle.kts docs && git commit -m "Faza 2: versionCode 2, odluke #34–#40 u spec"
```

- [ ] **Korak 6: Završni pregled cijele grane i PR prema `main`** (superpowers:finishing-a-development-branch)
