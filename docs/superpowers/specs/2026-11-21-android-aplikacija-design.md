# Caffe Bar 919 — Android aplikacija za evidenciju pića: dizajn izrade

21. 11. 2026 · NextPixel

Izvori:
- `../../../../Plan Android aplikacije za evidenciju pića.md` (u daljem tekstu **plan**) — šta aplikacija radi.
- Paper fajl „Caffe Bar 919 — evidencija pića“ (id `01M7Z35WJSKRE36Z9P9VQBPWWA`) — kako izgleda.
- `2026-11-21-paper-inventar.md` (pored ovog fajla) — tekstualni popis svih 34 ekrana sa doslovnim tekstovima. Kod se piše po njemu i po `get_jsx` / `get_computed_styles` iz Papera, nikad po slici.

Ovaj dokument ne ponavlja plan. On kaže **kako** se aplikacija gradi i kako se rješavaju rupe i nedosljednosti u dizajnu.

---

## 1. Svrha

Caffe Bar 919 hoće da zna koliko je koji konobar preuzeo pića tokom noći. Jedan radnik na šanku na Android tabletu bilježi svaku turu: kome je data i šta je u njoj. Na kraju noći vidi se zbir po konobaru i po piću, u komadima, dozama, bocama i litrima.

Vlasnik (Marinko) sam održava sve liste kroz podešavanja zaštićena PIN-om. Aplikacija i baza postoje samo na tabletu, a NextPixel nema pristup tabletu poslije predaje. Zato **ništa od podataka koji se mijenjaju u radu ne smije biti u kodu.**

## 2. Ciljevi, ne-ciljevi, anti-ciljevi

**Ciljevi**
- Tura se unese u nekoliko dodira, bez gledanja u ekran duže nego što treba (dugmad ≥ 56 px, „Potvrdi“ uvijek dole desno).
- Zbir po konobaru i piću je tačan i ostaje tačan i kad vlasnik kasnije promijeni naziv, mjeru ili litre.
- Ništa potvrđeno se ne gubi: ni pri gašenju tableta, ni pri restartu, ni pri grešci u upisu.
- Vlasnik može sve sam: artikli, kategorije, mjere, ljudi, pravila rada, PIN.

**Ne-ciljevi (v1, iz plana, tačka 9):** cijene i novac, zalihe, izvoz i dijeljenje, rezervna kopija, vraćena pića, posebne oznake za čašćenje i prosipanje, internet i sinhronizacija, uspravni prikaz, svijetla tema.

**Anti-ciljevi (ovo bi bio neuspjeh i kad tehnički radi):**
- Bilo koja lista, mjera, rok ili ime radnika upisan u kod, tako da promjena traži novu verziju.
- Stara noć koja poslije izmjene artikla pokazuje drugačiji zbir nego te noći.
- Dupla tura zbog dvostrukog dodira na „Potvrdi“.
- Izmišljeni podaci u bazi koju dobija Marinko.
- Instalacija nove verzije koja briše bazu (vidi 9.3).

## 3. Ograničenja

- Android tablet, horizontalno, 1280×800 dp kao referentna veličina. Model tableta nije poznat → `minSdk 26` (Android 8.0), `targetSdk`/`compileSdk 36`.
- Potpuno offline. Aplikacija ne traži dozvolu za internet.
- Tamna tema iz brenda 919: tokeni iz Papera (`--color-*`, `--text-*`, `--spacing-*`, `--radius-*`). Fontovi Inter (cijeli UI) i Cinzel (samo znak „919 CAFFE BAR“) se pakuju u aplikaciju.
- Jezik interfejsa: srpski, ijekavica, latinica. Tekstovi doslovno iz Papera, osim gdje ovaj dokument kaže drugačije.
- Na Macu za izradu: nema Android Studija. Gradi se iz komandne linije (Gradle wrapper) sa JDK 21 (Homebrew `openjdk@21`); openjdk 27 koji postoji je prenov za Gradle. SDK 36 i build-tools 36.1 postoje. Za probu se pravi AVD tableta 1280×800.
- Globalno pravilo „backend se testira u Dockeru“ se ovdje ne primjenjuje: nema backenda.

## 4. Pristupi (izabran 1)

| | Pristup | Za | Protiv |
|---|---|---|---|
| **1 ✓** | Jedan Gradle modul: Kotlin, Jetpack Compose, Room, Navigation Compose; ViewModel po ekranu; ručno povezivanje kroz `AppContainer` | najmanje zavisnosti, najbrži build, pregledno za jednog programera i ~20 ekrana | granice između slojeva čuvaju se paketima i disciplinom, ne modulima |
| 2 | Isti alati, više modula (core / data / feature-*), Hilt | stroge granice | sporiji build, više kalupa; isplati se tek za tim ili dijeljeni kod |
| 3 | Flutter / React Native | — | plan je već odredio Kotlin i Compose; samo Android; dodatni sloj bez koristi |

Korisnik je izabrao **pristup 1** i izradu u tri faze (poglavlje 8).

## 5. Odluke korisnika (iz razgovora 21. 11. 2026)

- PIN postavlja vlasnik. **PIN ima 6 cifara.** Početni (probni) PIN je **123456**.
- Pristup 1, faze kao u poglavlju 8, APK poslije svake faze.
- **Litre se sabiraju po kategoriji** pića i pišu sa njenim imenom („Žestina 3,14 l“, „Vino 1,20 l“). Nema posebne oznake „žestina“.
- **Zbir po mjerama piše se kratkim imenom mjere, pa brojem**: „kom 18 · doza 24 · bok. 3“. Mjera dobija polje „u zbiru“.
- Sve ostale odluke su u planu (tačke 1–11) i važe.

## 6. Arhitektura

### 6.1 Paketi (`dev.nextpixel.cafebar919`)

```
data/        Room: entiteti, DAO-i, AppDatabase, migracije, početni podaci
domain/      čista Kotlin pravila, bez Androida: obračun, rokovi, PIN, formati, validacija
repo/        repozitoriji: jedina mjesta koja pišu u bazu, svaka radnja = jedna transakcija
ui/theme/    boje, tipografija, oblici, razmaci iz Paper tokena
ui/components/  dugmad, pločice, stepper, čipovi, PIN tastatura, baneri, toast, prozori
ui/sank/     S0, S0.1, S1, S1.1, S1.2, S1.3
ui/pregled/  S2, S2.1, S2.2, S3 (+ T7 baner stare noći)
ui/podesavanja/  V0–V7 i ljuska podešavanja
AppContainer.kt  pravi bazu, repozitorije, sat (Clock)
```

- Svaki ekran: `XxxScreen` (Compose, bez logike) + `XxxViewModel` (stanje kao `StateFlow`, radnje kao funkcije). Čitanje iz baze je `Flow`, pa se ekrani sami osvježavaju.
- Vrijeme se uvijek čita iz ubrizganog `java.time.Clock`, da bi testovi mogli pomjerati sat (rok 60 s, ponoć, 30 dana, blokada PIN-a).
- Navigacija: Navigation Compose sa dva grafa, **šank** (S0/S1/S2/S3) i **podešavanja** (V1–V7). Prozori (S0.1, S1.2, S1.3, V0, V1.1, V1.2 i ostali) su Compose `Dialog`-zi nad ekranom, nikad prozor preko prozora (O5).

### 6.2 Model podataka (Room)

Imena tabela i kolona na srpskom, bez dijakritika.

| Tabela | Kolone | Napomena |
|---|---|---|
| `kategorija` | id, naziv, redoslijed, aktivna | |
| `mjera` | id, naziv (u pregledu), natpis (na dugmetu), u_zbiru (kratko ime u zbiru, ≤ 8 znakova), litara (`Double?`, null = komad), redoslijed, aktivna | početna lista iz plana, tačka 3 |
| `artikal` | id, naziv, kategorija_id, redoslijed, aktivan | jedinstven naziv među aktivnima (bez obzira na velika/mala slova i razmake na krajevima) provjerava repozitorij → ARTIKAL-01 |
| `artikal_mjera` | artikal_id, mjera_id, redoslijed | 1–3 reda po artiklu; redoslijed = redoslijed dugmadi na pločici |
| `radnik` | id, ime, uloga (`KONOBAR` / `ZA_SANKOM`), aktivan, redoslijed | stalni radnici |
| `razlog_ponistavanja` | id, tekst, redoslijed, aktivan | lista iz V6 |
| `postavke` | jedan red: rok_bez_pina_s, istorija_dana, zakljucavanje_s, ispomoc_dozvoljena, pin_hash, pin_so, pin_probni (bool), pin_pogresnih_zaredom, pin_blokiran_do | |
| `noc` | id, datum (`LocalDate`, datum otvaranja), otvorena_u, zatvorena_u (null = aktivna) | najviše jedna aktivna noć |
| `noc_primalac` | id, noc_id, vrsta (`KONOBAR` / `ZA_SANKOM` / `ISPOMOC` / `SANK`), radnik_id (null za ispomoć i Šank), ime, redoslijed | **snimak** ko radi te noći i u kojoj ulozi; „Šank“ se pravi automatski uz svaku noć |
| `tura` | id, noc_id, primalac_id, potvrdjena_u, kljuc (UUID, `UNIQUE`), ponistena_u, ponistena_bez_pina (bool), razlog_tekst | |
| `stavka_ture` | id, tura_id, artikal_id, artikal_naziv, kategorija_id, kategorija_naziv, mjera_id, mjera_naziv, mjera_natpis, mjera_u_zbiru, mjera_redoslijed, litara, kolicina | **snimak** naziva, mjere i litara u trenutku unosa |
| `nacrt_ture` | jedan red: primalac_id, stavke (JSON), izmijenjen_u | nepotvrđena tura preživljava restart (O7) |

Zašto snimci: plan kaže „Ranije ture i noći čuvaju naziv, mjeru i litre iz trenutka unosa“ i „Promjena uloge važi od sljedeće noći“. Pregledi i istorija čitaju **samo** snimke, nikad trenutni artikal ili radnika.

Brisanje: ništa iz podešavanja se ne briše, samo se isključuje. Jedino automatsko brisanje su zatvorene noći starije od `istorija_dana` (sa svim turama, stavkama i primaocima). Pokreće se pri pokretanju aplikacije i pri otvaranju nove noći. Aktivna noć se nikad ne briše.

**Početni podaci** (pri prvom pokretanju, samo stvarni podaci):
- mjere iz plana (naziv / dugme / u zbiru / litre): Komad / Komad / kom / —; Doza / 0,03 / doza / 0,03; Bokal / Bokal / bok. / 0,25; Boca 0,7 / Boca / boca 0,7 / 0,70; Boca 1 l / Boca 1 l / boca 1 l / 1,00;
- radnici: Vuleta i Srđan (konobari), Miloš i Smitran (za šankom);
- razlozi poništavanja: Pogrešan konobar, Pogrešno piće, Pogrešna količina, Uneseno dvaput;
- postavke: 60 s, 30 dana, 2 min, ispomoć uključena, PIN 123456 (`pin_probni = true`);
- **bez kategorija i artikala.** Marinko ih unosi sam (T2 to pokriva).

### 6.3 Ključna pravila i kako se ostvaruju

| Pravilo | Ostvarenje |
|---|---|
| Svaka potvrđena tura se odmah upisuje | `potvrdiTuru` = jedna Room transakcija (tura + stavke + brisanje nacrta). UI se prazni tek kad transakcija uspije. Pri grešci ostaje tura na ekranu + baner UPIS-01. |
| Dvostruki dodir = jedna tura | dugme prelazi u „Upisuje se…“ i zaključava se, a nacrt nosi UUID `kljuc` koji se upisuje u `tura.kljuc UNIQUE`. Drugi upis istog ključa se ignoriše. |
| Poslije potvrde izbor konobara se briše | ViewModel briše primaoca i stavke nacrta poslije uspješnog upisa. |
| Poništavanje bez PIN-a u roku | dozvoljeno ako `sada − potvrdjena_u < rok_bez_pina_s`; odbrojavanje u traci „Posljednje“ i u toastu (4 s). Oznaka „Poništeno bez PIN-a“. |
| Poslije roka: PIN + razlog | S1.3: izbor razloga (čip) + 6 cifara; provjera na šestoj cifri ako je razlog izabran. Poništena tura ostaje vidljiva i precrtana, ne ulazi u zbir. U zatvorenoj noći poništavanja nema. |
| Noć zadržava datum | `noc.datum` se postavlja pri otvaranju i nikad ne mijenja; ponoć ne utiče ni na šta. |
| PIN | Čuva se samo PBKDF2-SHA256 heš sa solju. 10 pogrešnih zaredom → `pin_blokiran_do = sada + 5 min` (PIN-02), stanje u bazi pa preživljava restart. Tačan PIN briše brojač. Pogrešan trenutni PIN u V3 i pogrešan PIN u S1.3 broje se u istu blokadu. Unos tura za vrijeme blokade radi normalno. |
| Zaključavanje podešavanja | Otključano stanje drži `PodesavanjaSesija` u memoriji. Svaki dodir u podešavanjima vraća odbrojavanje na `zakljucavanje_s`. Istek, „Zaključaj sada“, „Nazad na unos“ i odlazak aplikacije u pozadinu zaključavaju. |
| Isključivanje mjere ili kategorije u upotrebi | repozitorij odbija ako je koristi aktivan artikal; prekidač je u UI onemogućen, sa objašnjenjem ispod liste. |
| Ekran se ne gasi dok je noć otvorena | `FLAG_KEEP_SCREEN_ON` na S1. |
| Samo horizontalno | `screenOrientation="sensorLandscape"` u manifestu. |
| Malo prostora | pri pokretanju i otvaranju noći: slobodno < 200 MB → baner PROSTOR-01. |
| Baza se ne otvori | hvata se greška pri otvaranju → cijeli ekran BAZA-01 sa „Pokušaj ponovo“. Nikad se ne pravi nova baza preko stare (bez `fallbackToDestructiveMigration`). |

### 6.4 Obračun

`domain/Obracun` je čista funkcija nad stavkama (samo neponištene ture):
- po primaocu i mjeri: zbir količina (npr. Vuleta: Komad 22, Doza 26, Bokal 4);
- po artiklu i mjeri;
- litre **po kategoriji** = Σ količina × litara za stavke sa litrama, grupisano po `kategorija_id` iz snimka, prikazano pod `kategorija_naziv` iz snimka; kategorija bez litara se ne prikazuje u litrima;
- ukupno noći.

Format zbira po mjerama (jedna funkcija `domain/Format.zbirMjera`), svuda isti: `{u_zbiru} {broj}`, spojeno „ · “, redoslijedom mjera, samo mjere sa brojem > 0. Primjer: „kom 22 · doza 26 · bok. 4“. Nula svega: „još ništa“ (T2).

Gdje se pojavljuje: pločica osobe (S1), traka i prozor S1.2, S2 po konobaru, S3 istorija. Litre po kategoriji: dno kolone ture na S1 („Žestina 0,31 l“), S2.1 podnožje („Žestina ukupno 3,14 l“), S3 (kolona „Litre“ sa jednim redom po kategoriji, npr. „Žestina 3,14 l“).

Testira se brojevima iz Paper primjera koji se međusobno slažu (inventar, odjeljak E): 48 komada, žestina 3,14 l, Vuleta 22 / 26 / 4 i 1,78 l.

## 7. Rješenja za rupe i nedosljednosti u dizajnu

Ovo su moje odluke (graditelj ih ne mijenja bez pitanja). Na kraju izrade prepravljam Paper da se poklapa.

| # | Dizajn kaže | Odluka |
|---|---|---|
| 1 | PIN 4 cifre, „četvrtu cifru“, „ni 1234“ | 6 tačaka, „šestu cifru“, V3: „PIN ima 6 cifara i zna ga samo vlasnik. Ne koristi datum rođenja ni 123456.“ |
| 2 | S1.3 tastatura 2×5, V0/V3 3×4 | svuda **3×4** (jedna komponenta `PinTastatura`) |
| 3 | V0/V3 nemaju stanje greške | PIN-01 ispod tačaka: tačke se prazne i dobiju crveni okvir, tekst „PIN nije tačan. Unesi ponovo. · PIN-01“. PIN-02 isti izgled kao T5, tastatura onemogućena do isteka. |
| 4 | V3 nema greške koraka | korak 1 pogrešan → PIN-01, ostaje na koraku 1; korak 3 se ne poklapa → „PIN-ovi se ne poklapaju. Upiši novi PIN ponovo.“ i povratak na korak 2; zabranjen PIN (svih 6 istih cifara, 123456, 654321) → „Taj PIN je lako pogoditi. Izaberi drugi.“; uspjeh → toast „PIN je promijenjen.“ |
| 5 | probni PIN | dok je `pin_probni = true`, u ljusci podešavanja ispod banera „otključano“ stoji upozorenje „Još se koristi probni PIN 123456. Promijeni ga u meniju PIN vlasnika.“ |
| 6 | razlog „Vraćeno“ (S1.3, V6) | **ne ulazi u početnu listu** jer se kosi sa planom („vraćena pića se ne evidentiraju“). Vlasnik ga može dodati sam. |
| 7 | „Unijeto dvaput“ / „Uneseno dvaput“ | „Uneseno dvaput“ (V6 je izvor liste) |
| 8 | O4/RAZLOG-01 polje „Upiši razlog“ | razlog se bira čipom (S1.3, plan). „Poništi turu“ je onemogućeno dok razlog nije izabran; RAZLOG-01 se ne prikazuje kao greška. |
| 9 | toast poslije potvrde nije na S1 | dodaje se po O3: „Upisano: {ime} · {n} stavke“ + „Poništi“, 4 s, dole iznad trake |
| 10 | „60 s“, „2 min“, „30 dana“ u tekstu | svi takvi tekstovi se sastavljaju iz `postavke` |
| 11 | zbir na pločici osobe: „18 kom · 24 doze · 3 bok.“, ali mjere su lista vlasnika | **odluka korisnika**: „kom 18 · doza 24 · bok. 3“ (6.4) |
| 12 | kratki nazivi u traci „Posljednje“ („3 Jack“) | isto pravilo „ime pa broj“: `{naziv pića} {broj}`, a za mjeru sa litrama `{naziv pića} {u_zbiru} {broj}`. Primjer: „Jelen pivo 4 · Jack Daniel’s doza 2 · Rakija šljiva bok. 1“. Red se skraćuje sa „…“. Isto u S2.2 bez skraćivanja. |
| 13 | format mjere različit po ekranima | V1 čip i V1.1 čip: „Doza 0,03 l“; pločica S1: natpis mjere malim slovom + litre („doza 0,03 l“, „komad“); stavka ture: „doza · 0,06 l“ (ukupno); V4 kolona litara: „0,03“, za komad „—“ |
| 14 | „nijedno piće“ / „nema pića“ | svuda „nema pića“ |
| 15 | brojači u meniju podešavanja | broje se samo **aktivne** stavke: „N pića“, „N kategorija“, „N mjera“, „N stalnih radnika“ (sa pravilnom množinom) |
| 16 | fiksne kolone „Komadi/Doze/Bokali/Litre“ u S2/T7, „Komadi/Žestina“ u S3 | S2/T7: kolone se prave iz mjera koje se pojavljuju u toj noći (naslov = `u_zbiru` iz snimka, redoslijed = `mjera_redoslijed`), plus „Litre“. S3: kolone „Ture“, „Mjere“ (zbir u formatu 6.4) i „Litre“ (po kategoriji). |
| 17 | tekst ispod onemogućenog „Potvrdi“ | nema artikala → „Prvo treba dodati pića.“; ništa izabrano → „Izaberi konobara i dodaj piće.“; ima primaoca, nema pića → „Dodaj piće u turu.“; ima pića, nema primaoca → „Izaberi konobara.“ |
| 18 | T1: izabran konobar, panel kaže „TURA“ | „TURA ZA {ime}“ čim je primalac izabran |
| 19 | prazna noć: T2 gasi „Pregled noći“, T3 ga crta | „Pregled noći“ je uvijek aktivan, prazan pregled izgleda kao T3. „Završi noć“ na praznoj noći je dozvoljen (prozor S1.2 sa nulama). |
| 20 | BAZA-01 dva teksta, „Javi vlasniku.“ bez akcije | tekst sa ekrana (T6); umjesto linka: „Pomoć: NextPixel · +387 66 603 900“ kao običan tekst |
| 21 | ARTIKAL-01 dva teksta | ispod polja: „Taj naziv već postoji · ARTIKAL-01“ |
| 22 | O6 stari model („način brojanja“, „zapremina za bocu“) | važi model iz plana/V1.1: naziv (obavezan, ≤ 24 znaka, brojač od 20.), jedna kategorija, 1–3 mjere; „Sačuvaj artikal“ onemogućeno dok to nije ispunjeno; 4. mjera se ne može izabrati (ostali čipovi onemogućeni) |
| 23 | O5 ne navodi V4–V7 | meni V1–V7 kao u ljusci |
| 24 | Ispomoć nije nacrtana na S1/S2/T7 | treća grupa „ISPOMOĆ“ ispod „ZA ŠANKOM“, ista pločica; u S2/S3 ime sa oznakom „(ispomoć)“ |
| 25 | „Nazad na unos“ iz podešavanja | zaključava odmah (kao „Zaključaj sada“) |
| 26 | nenacrtani prozori (mjera, kategorija, radnik, razlog) | prave se iz istih komponenti kao V1.1: naslov, polja sa labelom iznad, „Odustani“ + zlatno „✓ Sačuvaj …“. Mjera: naziv, natpis na dugmetu (≤ 8 znakova), u zbiru (≤ 8 znakova, obavezno), litre (opciono, zarez, 0,01–5,00). V4 tabela dobija kolonu „U ZBIRU“. Kategorija: naziv. Radnik: ime + uloga. Razlog: tekst (≤ 32 znaka). |
| 27 | granice stepera V6 | poništavanje bez PIN-a 0–300 s, korak 10 s; istorija 7–90 dana, korak 1 dan; zaključavanje 1–10 min, korak 1 min |
| 28 | prazna istorija (S3) | prazan prikaz kao T3: „Još nema zatvorenih noći.“ |
| 29 | stanje S1 kad je izabran „Šank“ | isto kao konobar, panel „TURA ZA Šank“, dugme „Potvrdi · Šank“ |
| 30 | „Otvori noć“ bez ikoga označenog | dozvoljeno (Šank postoji uvijek); natpis „Otvori noć · 0 radnika“. Prozor ispomoći (S0.1) se ne nudi ako je ispomoć isključena u V6. |
| 31 | najmanji tekst 12 px (caption) vs „ništa ispod 13 px“ | ostaje 12 px samo za CAPS labele sekcija, kao u dizajnu |
| 32 | odakle se otvara S1.3 (poništavanje poslije roka) | po O5: dodir na turu u traci „Posljednje“ **ili** na turu u S2.2 dok je noć otvorena. Traka drži posljednjih 10 tura (vodoravni skrol). |
| 33 | smanjenje „Istorija noći“ u V6 trajno briše noći | ako bi nova vrijednost obrisala neku postojeću noć, prozor „Obrisati stare noći?“ / „{n} noći starijih od {d} dana biće obrisano pri sljedećem pokretanju.“ / „Nastavi unos“ i „Smanji istoriju“ |
| 34 | Početno stanje S0 | nijedan radnik nije označen. Radnik označava dodirom. Spisak čine aktivni radnici redoslijedom iz V2. |
| 35 | Pretraga (T1) | ne razlikuje velika i mala slova ni kvačice („sljiva“ nalazi „Rakija šljiva“, „đ“ = „d“). Svaka upisana riječ mora biti početak neke riječi u nazivu („jack d“ nalazi „Jack Daniel’s“, „cola“ nalazi „Coca-Cola“). Pretraga ide kroz **sva** pića: upis u polje bira čip „Sve“. |
| 36 | Čipovi kategorija na S1 | „Sve“ plus aktivne kategorije koje imaju bar jedno aktivno piće, redoslijedom iz V5. Ako izabrana kategorija nestane, bira se „Sve“. |
| 37 | Količina | jedna stavka ide od 1 do 99. Na 99 se plus i dodir na pločicu ignorišu. |
| 38 | Sat vraćen unazad | ako je `sada < potvrdjena_u`, rok bez PIN-a se smatra isteklim. Takva tura se poništava PIN-om (faza 3). |
| 39 | Piće ili mjera isključeni dok su u nacrtu | stavka nestaje iz ture na ekranu i ne upisuje se. Ako ne ostane nijedna stavka, „Potvrdi“ je onemogućeno kao za praznu turu. |
| 40 | Ime ispomoći | očišćeno (`Validacija.ocisti`), 1–20 znakova (`Validacija.IME_MAX`), ne smije biti jednako (bez obzira na velika slova) imenu stalnog radnika na spisku, drugoj ispomoći te noći ni riječi „Šank“. |
| 41 | odbrojavanje na traci „Posljednje“ | traka uvijek prikazuje „Poništi · {s} s“ dok traje rok bez PIN-a; poslije roka dugme nestaje, a tura se poništava PIN-om (faza 3). |

## 8. Faze

Svaka faza završava APK-om koji se proba na emulatoru tableta, i prolaskom svih testova.

**Faza 1 — temelj i podešavanja**
- Gradle projekat, JDK 21, AVD tableta, ikona aplikacije, potpis (9.3).
- Tema iz tokena, fontovi, komponente iz O2 koje trebaju podešavanjima (dugmad, čipovi, prekidač, stepper, lista sa ručicom, PIN tastatura, baner, toast, prozor).
- Cijela Room baza (svi entiteti iz 6.2, i oni za noći), početni podaci, BAZA-01.
- V0 (PIN), ljuska sa banerom i odbrojavanjem, V1, V1.1, V1.2, V2, V3, V4, V5, V6, V7 sa svim prozorima iz tačke 26.
- Privremeni početni ekran sa dugmetom „Podešavanja“ (S0 dolazi u fazi 2).

**Faza 2 — noć i unos ture**
- S0, S0.1, S1 (sa pretragom T1 i stanjem bez pića T2), nacrt ture koji preživljava restart, potvrda (transakcija, UUID, toast), traka „Posljednje“ sa poništavanjem u roku, UPIS-01, PROSTOR-01, `KEEP_SCREEN_ON`.
- **Urađeno 22. 11. 2026** (versionCode 2, „1.1“). APK je instaliran preko faze 1 i podaci su ostali.
  Kod i JVM testovi su gotovi, a APK je instaliran preko faze 1. Ručne provjere sa turama još nisu urađene, jer u bazi emulatora nema pića; ostaju otvorene dok vlasnik ne unese prava pića u V4: tri ture (jedna na Šank), poništavanje u roku, nacrt sa pićem koji preživljava restart, dvostruki dodir na „Potvrdi“ i pretraga „šljiva“ / T1.

**Faza 3 — poništavanje, pregled, istorija**
- S1.3 (PIN + razlog, T5), S2 / S2.1 / S2.2 (T3, T4), S1.2 Završi noć, S3 Istorija + T7 (baner stare noći, samo čitanje), automatsko brisanje starih noći.
- Prepravka Paper dizajna prema poglavlju 7.

## 9. Testiranje i isporuka

### 9.1 Testovi
- **JVM unit testovi** (`domain/`): obračun (Paper brojevi), rok poništavanja, blokada PIN-a sa pomjerenim satom, množina, formati brojeva i litara, validacija artikla, zabranjeni PIN-ovi.
- **Room testovi** (Robolectric, baza u memoriji): transakcija potvrde, idempotencija `kljuc`, snimci poslije izmjene artikla, brisanje starih noći, odbijanje isključivanja mjere u upotrebi.
- **Compose UI testovi** na emulatoru, samo za glavne tokove: otključavanje PIN-om, novi artikal, otvaranje noći → tura → potvrda.
- Prije predaje: ručna provjera na AVD-u 1280×800 (i na pravom tabletu kad ga dobijemo).

### 9.2 Bez izmišljenih podataka
Testovi prave svoje podatke u bazi u memoriji. U APK ulaze samo početni podaci iz 6.2. Ne postoji „demo“ režim.

### 9.3 Potpis i nadogradnje
Baza postoji samo na tabletu. Nova verzija mora se instalirati **preko** stare (isti `applicationId`, isti potpis), inače Android traži deinstalaciju i baza nestaje.
- Release keystore se pravi jednom i čuva van projekta, u NextPixel dokumentima, sa lozinkom u menadžeru lozinki. Nikad u gitu.
- `versionCode` raste sa svakom verzijom. Room migracije se pišu za svaku promjenu šeme (od verzije 1 nadalje), uz test migracije.
- `android:allowBackup="false"`: u skladu sa odlukom da nema rezervne kopije i da podaci ne izlaze sa tableta.

## 10. Otvoreno za korisnika

Oba pitanja iz provjere (litre i zbir po mjerama) korisnik je riješio, a upisana su u poglavlje 5.

Novi tekstovi faze 2 koji nisu u Paperu i čekaju odobrenje korisnika:
- „Upiši ime.“ (prazno ime ispomoći)
- „To ime već postoji večeras.“ (duplo ime ispomoći)
- „Ime je predugo.“ (ime ispomoći duže od 20 znakova)
- „Noć nije otvorena.“ / „Tablet nije uspio da sačuva podatke. Pokušaj ponovo.“ (baner greške na S0)

## 11. Ostaje graditelju

- Tačne verzije AGP, Kotlina, Compose BOM-a i Rooma (najnovije stabilne koje rade sa JDK 21 i SDK 36).
- Raspored koda unutar paketa, imena klasa, ikone (Material Symbols ili SVG iz Papera).
- Animacije prelaza (kratke, ≤ 200 ms, bez ukrasa).
- Ikona aplikacije: zlatni krug sa „919“ (Cinzel) na tamnoj podlozi, iz znaka u Paperu.
