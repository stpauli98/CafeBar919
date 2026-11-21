# Inventar Paper dizajna: „Caffe Bar 919 — evidencija pića“

Fajl Paper 01M7Z35WJSKRE36Z9P9VQBPWWA. Fajl je samo čitan, ništa nije mijenjano. Na fajlu nema komentara.

Inventar je pisan za Android (Kotlin + Jetpack Compose), tablet 1280×800, samo horizontalno, samo tamna tema. Tekstovi u navodnicima su doslovno prepisani iz dizajna. Ono što je označeno sa „pretpostavka“ zaključeno je iz konteksta.

**Stranice i table:**

| Stranica | Table |
|---|---|
| Osnova | O1 Temelj · O2 Komponente · O3 Povratne informacije · O4 Greške · O5 Navigacija i struktura · O6 Forme i validacija · O7 Veza i čuvanje (1440 px, dokumentacija) · T1–T7 ivična stanja (1280×800) |
| Šank — tablet | S0 · S0.1 · S1 · S1.1 · S1.2 · S1.3 · S2 · S2.1 · S2.2 · S3 |
| Vlasnik — podešavanja (PIN) | V0 · V1 · V1.1 · V1.2 · V2 · V3 · V4 · V5 · V6 · V7 |

**Fontovi:** Inter (cijeli interfejs) i Cinzel (samo znak „919“ / „CAFFE BAR“).

---

## Zbirni spisak nedosljednosti (prvo za čitanje)

### A. PIN

1. **Broj cifara.** Odlučeno je 6 cifara, a dizajn svuda crta 4. Treba promijeniti:
   - S1.3: 4 tačke
   - V0: 4 tačke i tekst „Otključava se čim upišeš četvrtu cifru.“
   - V3: 4 tačke i tekst „PIN ima 4 cifre i zna ga samo vlasnik. Ne koristi datum rođenja ni 1234.“
   - O2: 4 tačke
   - O4: tekst „…dok nisu upisani razlog i sve četiri cifre.“

   Za 6 cifara treba „šestu cifru“, „svih šest cifara“, 6 tačaka i drugi primjer slabog PIN-a (npr. „123456“).
2. **Raspored tastature.** S1.3 ima 2 reda po 5 tastera („1 2 3 4 5 / 6 7 8 9 0“), a brisanje je pored tačaka. V0, V3 i O4 imaju 3×4 („1 2 3 / 4 5 6 / 7 8 9 / prazno 0 ⌫“). Treba izabrati jedan raspored.
3. **Potvrda PIN-a.** V0 i V3 nemaju dugme za potvrdu, PIN se provjerava čim se upiše posljednja cifra. S1.3 traži dodir na „Poništi turu“.
4. **Izgled greške.** O2 crta ispunjene tačke sa crvenim obrubom. O4 kaže da se tačke prazne i dobiju crveni okvir. O4 je opis pravila.
5. **Blokada (PIN-02).** U skladu je sa planom: 10 pogrešnih zaredom, blokada 5 minuta, unos tura radi normalno.
   - Katalog O4 i tabla T5 je i dalje vode „za potvrdu sa vlasnikom“, iako je to dogovoreno 21. 11.
   - Trajanje se vidi samo posredno, kroz „Novi pokušaj je moguć u 23:58.“
   - Na V0 i V3 nisu nacrtani PIN-01 ni PIN-02.
   - Na V3 nisu nacrtani: pogrešan trenutni PIN, nepoklapanje novog PIN-a, zabranjen PIN i poruka o uspjehu.

### B. Poništavanje

6. **Razlog „Unijeto dvaput“ / „Uneseno dvaput“.** S1.3 piše „Unijeto dvaput“, a V6 „Uneseno dvaput“. Ista lista mora imati isti tekst. Pošto lista dolazi iz podešavanja, važi tekst iz V6.
7. **Razlog „Vraćeno“** (S1.3 i V6) se kosi sa planom: „Vraćena pića ne evidentiraju se u aplikaciji…“. Treba potvrditi da li ostaje kao početni razlog.
8. **Kako se bira razlog.** O4 (PIN-01) ima tekstualno polje „Razlog“ sa vrijednošću „pogrešan konobar“, a RAZLOG-01 kaže „Upiši razlog“ i „Ispod polja“. S1.3 i plan imaju izbor dodirom na čip iz liste. Treba uzeti čipove. Tada RAZLOG-01 u praksi znači „nije izabran razlog“, i dugme ostaje onemogućeno.
9. **Poništavanje u roku ima dva ulaza:** toast „Upisano: … “ sa dugmetom „Poništi“ (4 s, O3) i čip u traci „Poništi · N s“. Na S1 i S1.1 toast nije nacrtan.
10. **Rok je upisan kao broj u tekstu** („60 s“, „2 min“, „30 dana“) u S1.2, S1.3, S3, O3, O4 i O7. Po planu se podešava u V6, pa tekstove treba sastavljati iz podešavanja.

### C. Formati i tekstovi

11. **Jedinice u zbiru osobe.**
    - S1, S1.1, T1, T5 i O2 (pločica osobe) pišu „3 bok.“.
    - S1.2, S2 i T7 pišu „4 bokala“ / „1 bokal“.
    - O1 kaže „Jedinice uvijek riječju“.

    Prijedlog: „bok.“ samo na uskoj pločici S1, a svuda drugdje puna riječ. Treba i pravilo za množinu: 1 doza / 2–4 doze / 5+ doza (ali 21 doza, 22 doze); 1 bokal / 2–4 bokala / 5+ bokala; 1 poništena / 2–4 poništene; tura/ture/tura; „4 radnika“.
12. **Skraćeni nazivi pića u traci „Posljednje“.**
    - Traka (S1, O3) piše „3 Jack · 2 Red Bull“ i „4 Jelen · 2 Jack · 1 bokal“.
    - S2.2 piše pune nazive: „4 Jelen pivo · 2 Jack Daniel’s · 1 bokal Rakija šljiva“.

    Plan nema polje za kratki naziv. Treba odlučiti: dodati kratki naziv, pravilo „prva riječ“ ili skraćivanje sa „…“. Takođe, u traci „1 bokal“ izostavlja naziv pića.
13. **Format mjere.**
    - V1 (čip): „Doza 0,03“, bez „l“
    - V1.1 i V1.2: „Doza 0,03 l“
    - S1 (pločica): „doza 0,03 l“, malim slovom
    - stavka ture: „doza · 0,06 l“ (ukupne litre)
    - V4: naziv „Boca 0,7“, a litre „0,70“

    Treba jedno pravilo za svako mjesto.
14. **Nula pića.** V4 piše „nijedno piće“, a V5 „nema pića“.
15. **Brojači u meniju podešavanja.** „3 kategorije“ ne računa isključeno Vino. „6 mjera u litrima“ računa isključenu „Čašu vina“ i „Komad“, koji nema litre. Logika brojanja nije ista.
16. **Kolone mjera u pregledu** (S2, T7) su fiksne: „Komadi / Doze / Bokali / Litre“. Kolone za boce nema, iako postoje mjere „Boca 0,7“ i „Boca 1 l“ i pločica „Jack Daniel’s 0,7“. Kolone treba praviti iz mjera koje se pojavljuju te noći. S2.1 ovo rješava kolonom „Jedinica“.
17. **Tekst ispod onemogućenog „Potvrdi“ zavisi od stanja:**
    - „Izaberi konobara i dodaj piće.“ (S1.1, T5 i V0 kad ništa nije izabrano)
    - „Dodaj piće u turu.“ (T1, konobar izabran)
    - „Prvo treba dodati pića.“ (T2, nema artikala)
    - „Prvo izaberi konobara i dodaj piće.“ (O2, primjer)

    Prijedlog za pravilo: nema artikala → „Prvo treba dodati pića.“; ništa nije izabrano → „Izaberi konobara i dodaj piće.“; ima konobara, a nema pića → „Dodaj piće u turu.“; ima pića, a nema konobara → nije nacrtano (npr. „Izaberi konobara.“).
18. **T1: izabran konobar, a prazna tura.** Desni panel piše „TURA“, bez imena, sa uputstvom „Dodirni konobara lijevo…“. Po logici S1 trebalo bi „TURA ZA Vuleta“.
19. **Prazna noć (T2 i T3).** T2 ima onemogućeno „Pregled noći“, a T3 crta prazan pregled. Treba izabrati jedno. T2 ostavlja „Završi noć“ aktivnim i kad noć nema nijednu turu, i nije definisano da li se prazna noć smije završiti.
20. **T4.** „Ove noći ima 12 tura i nijedna nije poništena.“ odnosi se na Miloša, a čita se kao ukupan broj tura noći (podnaslov kaže 64).
21. **Tekst BAZA-01.**
    - Katalog: „Ne brisati aplikaciju.“ i naslov sa tačkom.
    - Ekrani (O4 prikaz, T6): „Ne briši aplikaciju i ne čisti njene podatke.“, bez tačke, plus link „Javi vlasniku.“ koji nema akciju.
22. **Tekst ARTIKAL-01.** Katalog: „Piće sa tim nazivom već postoji.“ Ispod polja: „Taj naziv već postoji · ARTIKAL-01“.
23. **Tekst PIN-01.** Katalog ima posebno „PIN nije tačan.“ i dugme „Unesi ponovo“. Prikaz to spaja u „PIN nije tačan. Unesi ponovo. · PIN-01“, bez dugmeta.
24. **Traka i dugme.** Traka se u O3 zove „Posljednja izdavanja“, a na S1 labela glasi „Posljednje“. Primjer dugmeta u O1 je „Potvrdi izdavanje“, a na S1 piše „Potvrdi · {ime}“.
25. **Najmanji tekst.** O1 kaže „nijedan tekst nije manji od 13 px“, a token caption od 12 px se koristi za labele sekcija („KONOBARI“, „POSLJEDNJE“, „TURA ZA“). O1 za pločicu pića navodi 72 px, a na S1 pločice su 192×120. 72 je najmanja visina.

### D. Model podataka i navigacija

26. **O6 opisuje stari model artikla:** „Obavezna polja su naziv, kategorija i način brojanja. Zapremina se traži samo za bocu.“ i „Zapremina se kuca sa zarezom: 0,70.“ Po planu i V1.1 artikal ima naziv, kategoriju i 1–3 mjere iz liste, a litre se unose u mjeri (V4). Validacija artikla: naziv obavezan, najviše 24 znaka (brojač od 20. znaka), jedinstven među aktivnim (ARTIKAL-01), tačno jedna kategorija, 1–3 mjere.
27. **O5 navigacija** navodi samo „V0 → V1–V3 · artikli, ljudi, PIN“. Nedostaju V4 Mjere, V5 Kategorije, V6 Pravila rada i V7 O aplikaciji.
28. **Ispomoć na S1.** Plan predviđa grupu „Ispomoć“ u lijevoj koloni S1, ali grupa nije nacrtana, jer ta noć nema ispomoć. Na S0 grupa postoji kao „Ispomoć · samo večeras“. Pretpostavka: na S1 je treća grupa „Ispomoć“ ispod „Za šankom“, sa istom pločicom. Ista grupa nedostaje i na S2 i T7, a u S3 ispomoć se piše „Dejan (ispomoć)“.
29. **„Istorija“** se otvara samo sa S0 (dugme u gornjoj traci). S1 nema to dugme, u skladu sa O5 („S3 Istorija — sa S0“).
30. **„Nazad na unos“ iz podešavanja.** Nije definisano da li odmah zaključava podešavanja ili ostavlja da se zaključaju kad istekne rok. Postoji i „Zaključaj sada“.
31. **Kategorija „Vino“ i mjera „Čaša vina“** (isključene, V4 i V5) ne postoje u početnoj listi iz plana. To su samo primjeri. Plan kaže da su sva pića na ekranima primjer.
32. **Nisu nacrtani:**
    - prozori za novu i izmijenjenu mjeru
    - prozori za novu kategoriju i preimenovanje
    - prozori za novog radnika i izmjenu imena
    - unos novog razloga
    - validacija V1.1 (prazan ili dupli naziv, 0 ili 4. mjera)
    - blokiran prekidač mjere ili kategorije koja je u upotrebi
    - granice stepera u V6
    - prazna istorija (S3)
    - toast poslije potvrde na S1
    - stanje S1 kad je izabran „Šank“
    - ponašanje kad konobar nije izabran, a pića jesu
    - onemogućeno „Otvori noć“ kad niko nije označen

### E. Usklađeno sa planom (provjereno)

- Rokovi 60 s, 30 dana i 2 min (V6, baner, S1.2, S1.3, S3, V7), blokada PIN-a 10 / 5 min (T5), „Poništeno bez PIN-a“ (S2.2, O2, O3), ispomoć bez PIN-a samo pri otvaranju noći (S0.1, V2), datum noći ostaje isti i poslije ponoći (S0, O7), ništa se ne briše nego se isključuje (V1, V2, V4, V5), dva aktivna artikla ne mogu imati isti naziv (O4, O6).
- Kontakt u V7: „NextPixel, Nikola“, „+387 66 603 900“, „www.nextpixel.dev“. Puno poslovno ime firme se nigdje ne pojavljuje, pa nema ni starog imena vlasnika koje bi trebalo ispraviti.
- Brojevi u primjeru se međusobno slažu (S1 → S1.1 → S1.2 → S2 → S2.1 → S3): 48 komada, žestina 3,14 l, Vuleta 22 / 26 / 4 i 1,78 l. Mogu poslužiti kao test podaci za obračun.

---

# Osnova (O1–O7): biblioteka komponenti, greške, navigacija

Svi tekstovi pod navodnicima / u `kodu` prepisani su doslovno iz Paper fajla. Tablice O1–O7 su široke 1440 px (dokumentacija, ne ekrani aplikacije).

## O1 · Temelj

Podnaslov: „Boje, slova, razmaci i pravila koja važe za svaki ekran aplikacije za evidenciju pića. Tamna tema iz brenda Caffe Bar 919: tablet na šanku ne bliješti u mraku.“

**Znak:** krug zlatne boje sa „919“ (Cinzel bold) + „CAFFE BAR“ (Cinzel, razmak slova caps). Isti znak stoji gore lijevo na svim ekranima.

**Boje** („Zlatna je jedina jaka boja. Na ekranu uvijek označava jednu stvar: šta je izabrano i šta se potvrđuje. Status nikad nije samo boja, uvijek ide uz ikonu i riječ.“)

| Token | Vrijednost · namjena (doslovno) |
|---|---|
| `--color-bg` | #191919 · podloga |
| `--color-surface` | #232323 · paneli |
| `--color-tile` | #262626 · pločice i dugmad |
| `--color-line` | #363636 · linije |
| `--color-ink` | #F5F1E6 · glavni tekst |
| `--color-muted` | #A8A8A8 · pomoćni tekst |
| `--color-primary` | #D3B574 · izbor, potvrda, fokus |
| `--color-on-primary` | #191919 · tekst na zlatnoj |
| `--color-primary-tint` | #2E2A20 · izabrano, blago |
| `--color-success-tint` | #2E4B3E · zelena 919, uspjeh |
| `--color-danger` | #E5735F · poništavanje, greška |
| `--color-warning` | #E3AE55 · upozorenje |

Ostali tokeni u fajlu (bez opisa na tabli): `--color-link` #D3B574, `--color-warning-tint` #33291A, `--color-success` #7CC49A, `--color-danger-tint` #3A211D, `--color-placeholder` #858585, `--color-disabled-ink` #6E6E6E, `--color-disabled-bg` #262626, `--color-primary-pressed` #B39A5E, `--color-pressed` #2E2E2E, `--color-warning-line` #6B5326, `--color-danger-line` #6E3A31, `--color-info-line` #5E5238, `--color-neutral-line` #444444, `--color-toast-action` #7A5E1E.

**Tipografija · tablet:** „Inter za cijeli interfejs, Cinzel samo za znak 919. Tablet stoji na šanku na oko 60 cm od očiju, zato nijedan tekst nije manji od 13 px, a tekst za čitanje nije manji od 16 px.“

| Uloga (veličina/težina) | Primjer |
|---|---|
| Količina u turi · 40/800 | „12 doza“ |
| Naslov ekrana · 28/800 | „Pregled noći · petak 20. 11.“ |
| Ime konobara · 20/700 | „Vuleta“ |
| Naziv pića na pločici · 18/600 | „Jack Daniel’s“ |
| Tekst, unos, dugmad · 16/500 | „Potvrdi izdavanje“ |
| Labela, jedinica · 14/500 | „komadi · doze · bokali“ |
| Vrijeme, pomoćno (najmanje) · 13/500 | „23:47 · prije 2 min“ |
| Znak (samo logo) · 20/400 | „CAFFE BAR“ (Cinzel) |

Tokeni veličina: caption 12, label 13, body 16, section 20, title 28, display 40; tracking tight −0.02em, caps 0.08em; leading body 24, title 34.

**Razmaci, zaobljenja, dodir:** „Na šanku se radi brzo, često mokrim prstima. Zato je najmanja dodirna površina 56 px, a ne 48 px, i između dva dugmeta je najmanje 8 px.“
- Razmaci: 4 · 8 · 12 · 16 · 24 · 32.
- Zaobljenja: „8 · značka“, „12 · dugme“, „16 · pločica“ (+ pill 999).
- Dodir: „56 · najmanje“, „72 · pločica pića“.
- Ugniježđeno zaobljenje: „Unutrašnje = spoljašnje − razmak. Okvir ture 16 px sa razmakom 8 px → red u turi 8 px. Pločica 16 px sa razmakom 4 px → polje „0,03“ 12 px.“

**Ikone:** „Linijske ikone, 24 px, debljina 2. Ikona uvijek ide uz riječ, osim plus i minus pored količine. Poništavanje se nikad ne radi samo ikonom.“
Rječnik ikona (naziv · značenje): Dodaj · količina +1; Oduzmi · količina −1; Potvrdi · tura, PIN; Poništi · potvrđenu turu; PIN vlasnika · zaštićena radnja (katanac); Istorija · ranije noći; Pregled noći · zbirovi (stubičasti grafikon); Podešavanja · artikli, konobari; Pretraga · naziv pića; Ukloni · stavku iz ture; Noć · nova, završena (mjesec); Upozorenje · uz tekst (trougao).

**Pristupačnost i detalji** (doslovno):
- Kontrast: „Krem na crnoj 15,6:1, sivi pomoćni 7,4:1, zlatna na crnoj 8,9:1, crna na zlatnoj 8,9:1. Sve iznad AA.“
- Izbor se vidi bez boje: „Izabran konobar ima zlatnu podlogu i kvačicu. Poništena tura je precrtana i ima riječ „Poništeno“, a ne samo crvenu boju.“
- Brojevi: „Sve količine, litre i vremena su tabularni (tabular-nums) i poravnati desno u kolonama. Decimalni zarez: 0,30 l.“
- Jedinice uvijek riječju: „„3 kom“, „5 doza“, „2 bokala“, „1 boca“. Litre samo uz žestinu: „0,15 l“.“
- Ekran ne gasne: „Dok je noć otvorena, ekran ostaje upaljen. Osvjetljenje prati sistem, a tamna tema je jedina tema.“
- Bez dvostrukog unosa: „Poslije prvog dodira na „Potvrdi“ dugme je zaključano dok se tura ne upiše (ispod 0,3 s).“
- Bez ukrasa: „Nema kartica oko svega, nema senki ni gradijenata. Površine se razlikuju samo tonom crne.“
- Mono font: „Ne koristi se. Aplikacija nema šifre koje se prepisuju, a šifre grešaka su obične riječi u tekstu.“

## O2 · Komponente

Podnaslov: „Dijelovi od kojih su sastavljeni svi ekrani, u svim stanjima. Visine su iste kao na ekranima: glavna radnja 64 px, ostala dugmad i polja 56 px.“

### Dugmad
„Glavno dugme je jedno po ekranu i uvijek je „Potvrdi“ ili radnja koja završava korak. Onemogućeno dugme uvijek ima napisan razlog ispod.“

Četiri varijante × pet stanja (Mirovanje · Pritisnuto · Fokus · Onemogućeno · U toku). Radius 12, ikona 22 px lijevo od teksta.

| Varijanta | Primjer (ikona + tekst) | Izgled | Pritisnuto | Fokus | Onemogućeno + razlog ispod | U toku |
|---|---|---|---|---|---|---|
| glavno | ✓ „Potvrdi“ | zlatna podloga, crni tekst | tamnija zlatna (#B39A5E) | zlatna + spoljni zlatni prsten (2 px razmak) | siva podloga, sivi tekst; „Prvo izaberi konobara i dodaj piće.“ | spinner + „Upisuje se…“ na zlatnoj |
| sporedno | mjesec + „Završi noć“ | providno, okvir #444 | podloga #2E2E2E | zlatni prsten | sivo; „Nema otvorene noći.“ | spinner + „Upisuje se…“ |
| tiho | grafikon + „Pregled noći“ | podloga tile #262626, bez okvira | #2E2E2E | zlatni prsten | sivo; „Još nema izdavanja.“ | spinner + „Upisuje se…“ |
| opasno | strelica nazad + „Poništi turu“ | providno, crveni tekst i crveni okvir (danger-line) | crvena tint podloga (#3A211D) | zlatni prsten | sivo; „Tura je već poništena.“ | crveni spinner + „Upisuje se…“ |

### Konobar i Šank (pločica osobe, 228×80, radius 16)
„Ispod imena je zbir te noći, odvojeno komadi, doze i bokali, jer se različite jedinice ne sabiraju. Šank je odvojen isprekidanom linijom: tu ide prosuto i ostalo što ne ulazi u zbir konobara.“

| Stanje | Sadržaj |
|---|---|
| Mirovanje | „Vuleta“ / „6 kom · 9 doza · 1 bok.“ — tile podloga |
| Pritisnuto | isto, svjetlija podloga (#2E2E2E) |
| Izabran | zlatna podloga, tamni tekst, crni krug sa zlatnom kvačicom desno |
| Još bez izdavanja | „Srđan“ / „još ništa“ |
| Šank | „Šank“ / „prosuto i ostalo“ — providno, isprekidan okvir |

Format zbira: `N kom · N doza · N bok.` (izostavlja jedinice sa 0; npr. „9 kom · 7 doza“, „2 doze“).

### Pločica pića (192×120 na S1, radius 16)
„Jedan dodir dodaje jedan komad, jednu dozu ili jednu bocu. Dupla je dva dodira. Točena žestina ima dva polja: „0,03“ i „Bokal“ (0,25 l). Kad je piće u turi, pločica dobija zlatni okvir i broj.“

| Stanje | Naziv / donji red |
|---|---|
| Komad | „Jelen pivo“ / „komad“ |
| Doza | „Jack Daniel’s“ / „doza 0,03 l“ |
| U turi | „Jack Daniel’s“ + zlatna značka-broj „2“ gore desno, zlatni okvir 2 px / „doza 0,03 l“ |
| Pritisnuto | „Coca-Cola“ / „komad“, svjetlija podloga |
| Boca | „Jack Daniel’s 0,7“ / „boca 0,70 l“ |
| Točena žestina (2 mjere) | „Rakija šljiva“ + dva unutrašnja dugmeta visine 56: „0,03 · 2“ i „Bokal · 1“ (u turi: zlatni tint + zlatni okvir; van ture: „0,03“ / „Bokal“ na tamnoj podlozi) |

Pravilo: pločica s jednom mjerom = cijela pločica je dugme, donji red je mjera; pločica sa 2 (do 3) mjere = naziv gore + red dugmadi, svako dugme = natpis mjere (`buttonLabel`), u turi dobija „ · N“.

### Stavka ture i količina (stepper)
Red: naziv (16/600) + ispod mjera (13, muted) | [−] 56×56 | broj (24/800, širina 48) | [+] 56×56; separator linija ispod reda.
- „Jack Daniel’s“ / „doza · 0,09 l“ / 3
- „Jelen pivo“ / „komad“ / 4
- „Rakija šljiva“ / „bokal · 0,25 l“ / 1

Pravila količine: „Minus na 1 uklanja stavku iz ture, bez pitanja, jer tura još nije potvrđena. Količina se ne kuca, samo plus i minus. Litre se prikazuju samo za žestinu.“
Format mjere u redu: `doza · <ukupno litara> l` (3 doze → „0,09 l“), `bokal · 0,25 l`, za komad samo „komad“.

### Kategorije, pretraga, PIN i značke
„Značka uvijek ima riječ. „Poništeno bez PIN-a“ označava turu poništenu u prvih 60 sekundi, da vlasnik vidi i te ispravke.“
- **Kategorije (chips, pill, visina 48):** „Sve“, „Pivo“ (izabrano: krem podloga #F5F1E6, tamni tekst), „Žestina“ (neizabrano: okvir linija).
- **Pretraga (pill, visina 56):** prazno — lupa + placeholder „Traži piće“; aktivno — zlatni okvir, upisano „jack“.
- **PIN vlasnika (tačkice):** 4 tačkice; prazno (sivi obrub), djelimično (2 ispunjene krem), greška (sve 4 sa crvenim obrubom).
- **Značke (radius 8, ikona + riječ):** „Noć otvorena“ (mjesec, zlatni tint), „Upisano“ (kvačica, zelena), „Poništeno“ (strelica nazad, crvena), „Poništeno bez PIN-a“ (trougao upozorenja, narandžasto-žuta), „Zatvorena noć“ (siva, bez ikone).

## O3 · Povratne informacije

Podnaslov: „Kako aplikacija javlja da je nešto urađeno, da nešto traje ili da treba odluka. Pravilo: najmanji znak koji je dovoljan. Radnik gleda u čaše, ne u tablet.“

### Potvrda ture i poništavanje u 60 s
„Kratko obavještenje javlja da je tura upisana i nestaje poslije 4 s. „Poništi“ zatim ostaje uz tu turu u traci „Posljednja izdavanja“, sa odbrojavanjem. Poslije 60 s dugme nestaje, a tura se poništava samo PIN-om vlasnika (S1.3).“
1. „1 · Odmah poslije potvrde (4 s)“ — toast (svijetla krem podloga, zelena kvačica): „Upisano: Vuleta · 3 stavke“ + tekst-dugme „Poništi“ (tamnozlatno #7A5E1E).
2. „2 · Do isteka 60 s, u traci dole“ — čip u traci: „23:47“ „Vuleta“ „3 Jack · 4 Jelen · 1 bokal“ + dugme (ikona nazad) „Poništi · 54 s“ (odbrojava).
3. „3 · Poništeno u roku“ — „23:47“ „Vuleta“ „3 Jack · 4 Jelen“ precrtano + značka „Poništeno bez PIN-a“.

Format sažetka ture u traci: `<količina> <kratki naziv>` spojeno „ · “ (npr. „3 Jack · 4 Jelen · 1 bokal“ — napomena: za bokal se piše mjera, ne naziv pića).

### Baner i prozor za potvrdu
„Baner stoji dok stanje traje: otključan način vlasnika i gledanje stare noći. To su jedini „načini rada“ i uvijek se vide. Prozor se otvara samo kad se nešto ne može lako vratiti, a dugme kaže radnju („Završi noć“, a ne „Da“).“
- Baner upozorenja (warning tint, trougao): **„Otključano za vlasnika.“** „Zaključava se samo za 2 min bez dodira.“ + dugme „Zaključaj“.
- Baner info (i u krugu): **„Pregled zatvorene noći.“** „Petak 13. 11. · samo za čitanje.“ + dugme „Nazad na unos“.
- Prozor · arhiviranje: naslov „Završiti noć?“; tekst „Petak 20. 11. se arhivira i ostaje u istoriji 30 dana. Ekran za unos ostaje prazan dok ne otvoriš novu noć.“; dugmad „Nastavi unos“ (sporedno) i „Završi noć“ (glavno, zlatno).

### Vrste radnji
| Vrsta | Radnje | Kako se radi |
|---|---|---|
| Povratne | Izmjena ture prije potvrde · poništavanje ture u prvih 60 s | Bez pitanja i bez PIN-a. Bilježi se u pregledu kao „Poništeno bez PIN-a“. |
| Arhiviranje | Završi noć · deaktiviranje konobara ili artikla | Prozor za potvrdu. Podaci ostaju u istoriji, a deaktivirani se mogu ponovo uključiti u podešavanjima. |
| Konačne (crvene) | Poništavanje ture poslije 60 s | PIN vlasnika i razlog. Tura ostaje vidljiva i precrtana, ali ne ulazi u zbir. Ne može se vratiti, nego se unosi ponovo. |
| Automatske | Brisanje zatvorenih noći starijih od 30 dana | Bez pitanja, pri pokretanju i pri otvaranju nove noći. Aktivna noć se nikad ne briše. |

### Učitavanje i čuvanje
- „Do 0,3 s · ništa“ — „Upis ture, promjena konobara, otvaranje pregleda. Baza je na tabletu, pa je skoro sve u ovom roku.“
- „Do 1 s · spinner u dugmetu“ — „„Potvrdi“ prelazi u „Upisuje se…“ i zaključan je. Drugi dodir se ignoriše.“
- „Preko 1 s · skelet“ — „Samo pregled duge noći i istorija sa 30 noći. Skelet ima oblik tabele.“
- „Nema mreže“ — „Aplikacija ne koristi internet. Nema stanja „nema veze“ ni sinhronizacije (O7).“

## O4 · Greške

Podnaslov: „Svaka greška kaže pet stvari: šta se desilo, zašto, šta je sa podacima, koje dugme rješava i šifru. Unesena tura se nikad ne briše zbog greške.“

Napomena na tabli: „Aplikacija radi bez interneta i na jednom tabletu, pa nema grešaka mreže (MREZA-01/02), servera (SERVER-01) ni istovremene izmjene. Pristup (PRISTUP-01) ovdje je PIN vlasnika: PIN-01 i PIN-02.“

### Katalog (doslovno)
| Šifra | Šta se desilo | Zašto | Podaci | Dugme | Prikaz |
|---|---|---|---|---|---|
| UPIS-01 | Tura nije upisana. | Tablet nije uspio da sačuva podatke. | Tura ostaje na ekranu, ništa nije izgubljeno. | Pokušaj ponovo | Baner u koloni ture |
| PROSTOR-01 | Na tabletu je ostalo malo prostora. | Memorija je skoro puna (ispod 200 MB). | Sve je sačuvano. Novi unos još radi. | Razumijem | Baner na vrhu dok traje |
| PIN-01 | PIN nije tačan. | Unesene cifre se ne poklapaju sa PIN-om vlasnika. | Ništa nije promijenjeno. | Unesi ponovo | Ispod polja za PIN |
| PIN-02 | Previše pogrešnih pokušaja. | Deset puta zaredom pogrešan PIN. | Ništa nije promijenjeno. Unos tura radi normalno. | Pokušaj za 5 min | U prozoru za PIN · za potvrdu sa vlasnikom |
| RAZLOG-01 | Nedostaje razlog. | Poništavanje poslije 60 s traži razlog. | Tura još nije poništena. | Upiši razlog | Ispod polja |
| BAZA-01 | Evidencija se ne može otvoriti. | Baza na tabletu se nije učitala pri pokretanju. | Podaci su i dalje na tabletu. Ne brisati aplikaciju. | Pokušaj ponovo | Cijeli ekran |
| ARTIKAL-01 | Piće sa tim nazivom već postoji. | Dva aktivna artikla ne mogu imati isti naziv. | Ništa nije sačuvano, uneseno ostaje u formi. | Promijeni naziv | Ispod polja |

### Kako izgledaju
„Poslije pogrešnog PIN-a tačkice se prazne i dobiju crveni okvir, a razlog ostaje upisan. „Poništi turu“ je onemogućeno dok nisu upisani razlog i sve četiri cifre.“

1. **„UPIS-01 · u koloni ture“** — crveni baner na vrhu kolone ture (danger tint, ikona ×): **„Tura nije upisana.“** „Tablet nije uspio da sačuva podatke. Tura je ostala ovdje, ništa nije izgubljeno.“ Ispod ostaju stavke („Jack Daniel’s“ „doza · 0,09 l“ 3; „Jelen pivo“ „komad“ 4) sa stepperima; glavno dugme postaje „Pokušaj ponovo“ (ikona istorija/ponovo); ispod sitno „Šifra: UPIS-01“.
2. **„PIN-01 · ispod polja, PIN se briše“** — prozor: katanac + „PIN vlasnika“; tekst „Tura Vuleta 23:47 je starija od 60 s. Za poništavanje treba PIN vlasnika.“; labela „Razlog“ + polje (vrijednost „pogrešan konobar“); 4 prazne tačkice sa crvenim obrubom; crveno sa trouglom: „PIN nije tačan. Unesi ponovo. · PIN-01“; numerička tastatura 3×4: 1 2 3 / 4 5 6 / 7 8 9 / (prazno) 0 ⌫; dugmad „Odustani“ (sporedno) i „Poništi turu“ (onemogućeno).
3. **„BAZA-01 · cijeli ekran“** — crveni krug sa „!“; naslov „Evidencija se ne može otvoriti“; „Baza na tabletu se nije učitala pri pokretanju.“; „Podaci su i dalje na tabletu. Ne briši aplikaciju i ne čisti njene podatke.“; glavno dugme „Pokušaj ponovo“; zlatni link „Javi vlasniku.“; sitno „Šifra: BAZA-01“.

## O5 · Navigacija i struktura

Podnaslov: „Jedan tablet, jedan radnik, dva načina rada: unos (svi) i podešavanja (vlasnik, PIN). Sve je najviše jedan korak udaljeno od unosa ture.“

### Tok ekrana
„Glavni tok je zlatni. Svi ostali ekrani vraćaju se na unos jednim dugmetom „Nazad na unos“ gore lijevo.“

Glavni tok (strelice): **S0 Nova noć** („Ko · 276“) → **S1 Unos ture** („glavni ekran, cijelu noć“, istaknut zlatno) → **S1.2 Završi noć** („prozor sa zbirom“) → **S0 Nova noć** („prazan ekran do sljedeće noći“).

Sporedni ekrani:
- **S2 · S2.1 · S2.2 Pregled noći** — „po konobaru, po piću, ture · iz trake dole“
- **S1.3 Poništavanje** — „dodir na turu u traci ili u S2.2“
- **S3 Istorija** — „sa S0 · 30 dana, samo čitanje“
- **V0 → V1–V3 Podešavanja** — „PIN vlasnika · artikli, ljudi, PIN“

### Anatomija ekrana za unos (S1)
- „Vrh · 72 · znak, datum noći, Podešavanja, Završi noć“
- lijevo „Ko“ · 276 px
- sredina „Pića · kategorije, pretraga, pločice 3 u redu“ (ostatak širine)
- desno „Tura · 364 · Potvrdi dole“ (zlatno isprekidano)
- „Posljednje · 72 · Poništi 60 s, Pregled noći“

Pravila (doslovno):
- **Jedna glavna radnja** — „Na unosu je to „Potvrdi“, uvijek dole desno, na istom mjestu. Ruka ga nađe bez gledanja.“
- **Prozor, nikad prozor preko prozora** — „Završi noć, poništavanje, PIN i novi artikal su prozori. Iz prozora se ne otvara drugi prozor.“
- **Izbor se ne pamti poslije potvrde** — „Poslije „Potvrdi“ konobar se odznači, da sljedeća tura ne ode istom konobaru greškom.“
- **Samo horizontalno** — „Aplikacija radi samo u horizontalnom položaju tableta. Uspravni prikaz se ne podržava.“

## O6 · Forme i validacija

Podnaslov: „Forme ima samo vlasnik: novi artikal, izmjena artikla, novi radnik. Radnik na šanku kuca samo ime ispomoći. Sve ostalo je dodir.“

### Polje kroz stanja
„Labela je uvijek iznad polja. Greška se javlja kad se izađe iz polja, ne dok se kuca. Uneseno se nikad ne briše zbog greške.“
Polje visine 56, radius 12, labela „Naziv“ iznad (14–16/600).
- Prazno: placeholder „npr. Jägermeister 0,7“ (siv), okvir linija.
- Fokus, kuca se: „Jäger“ + kursor, zlatni okvir.
- Popunjeno: „Jägermeister 0,7“, normalan okvir.
- Greška pri izlasku iz polja: labela crvena, okvir crven, vrijednost „Jack Daniel’s“, ispod crveno sa trouglom: „Taj naziv već postoji · ARTIKAL-01“.

### Čuvanje i izlazak
„Poslije čuvanja prozor se zatvara, a artikal je odmah na šanku. Ako se izađe bez čuvanja, pita se, a podrazumijevano dugme je „Nastavi unos“. Obavezna polja su naziv, kategorija i način brojanja. Zapremina se traži samo za bocu.“
1. „1 · Sačuvaj“ — glavno dugme ✓ „Sačuvaj artikal“.
2. „2 · Čuvanje u toku“ — spinner „Upisuje se…“.
3. „3 · Sačuvano“ — toast (krem, zelena kvačica): „Sačuvano: Jägermeister 0,7 · već je na šanku“.
4. „4 · Izlazak bez čuvanja“ — prozor „Odbaciti novi artikal?“ / „Uneseni naziv i mjera se ne čuvaju.“ / „Nastavi unos“ (sporedno) i „Odbaci“ (opasno, crveni tekst i okvir).

### Pravila za artikal
- **Promjena mjere** — „Nova mjera važi od sljedeće ture. Ranije ture i noći čuvaju mjeru i naziv iz trenutka unosa.“
- **Decimale** — „Zapremina se kuca sa zarezom: 0,70. Tastatura je numerička, sa zarezom.“
- **Naziv** — „Najviše 24 znaka, da stane na pločicu u dva reda. Brojač se pojavi od 20. znaka.“
- **Isti naziv** — „Dva aktivna artikla ne mogu imati isti naziv (ARTIKAL-01). Isključen artikal se može ponovo uključiti.“

## O7 · Veza i čuvanje

Podnaslov: „Aplikacija ne koristi internet. Nema sinhronizacije, servera ni stanja „nema veze“. Sve se čuva u bazi na tabletu, odmah.“

### Šta se dešava kad…
„Rezervne kopije i izvoza nema. Ako se tablet pokvari ili izgubi, istorija noći se gubi.“

| Situacija | Radnik vidi | Radnik smije | Sistem radi |
|---|---|---|---|
| Potvrdi | „Upisano“ 4 s, tura u traci dole | odmah unositi sljedeću turu | upisuje turu u bazu prije nego što isprazni kolonu ture |
| Tablet se ugasi ili restartuje | isti ekran, ista otvorena noć | nastaviti rad | otvorena noć ostaje; nepotvrđena tura se vraća kao nacrt |
| Prođe ponoć | datum noći se ne mijenja | raditi do kraja smjene | noć zadržava datum početka (petak ostaje petak) |
| Nema interneta | ništa, jer internet ne treba | sve | ništa ne šalje i ništa ne čeka |
| Malo prostora | baner PROSTOR-01 | nastaviti rad | nastavlja upis; briše noći starije od 30 dana |
| Baza se ne otvori | cijeli ekran BAZA-01 | pokušati ponovo, zvati pomoć | ne briše ništa i ne pravi novu bazu |

Implementacijske posljedice: nepotvrđena tura (nacrt) mora se trajno čuvati (Room/DataStore) da preživi restart; upis ture je transakcija prije čišćenja UI stanja; FLAG_KEEP_SCREEN_ON dok je noć otvorena; samo landscape.

## Nedosljednosti uočene u O1–O7

1. **PIN 4 cifre** — O2 (4 tačkice), O4 („sve četiri cifre“, 4 tačkice u prozoru). Korisnik je odlučio 6 cifara → svuda 6 tačkica i tekst „svih šest cifara“.
2. **Stanje greške PIN tačkica** — O2 prikazuje 4 *ispunjene* tačkice sa crvenim obrubom; O4 tekst i prikaz kažu „tačkice se prazne i dobiju crveni okvir“ (prazne). O4 je opisno pravilo → prazne + crveni okvir.
3. **BAZA-01 tekst se razlikuje** — katalog: „Podaci su i dalje na tabletu. Ne brisati aplikaciju.“ i naslov sa tačkom „Evidencija se ne može otvoriti.“; prikaz: „Podaci su i dalje na tabletu. Ne briši aplikaciju i ne čisti njene podatke.“ i naslov bez tačke. Dugmad u prikazu: „Pokušaj ponovo“ + „Javi vlasniku.“ (katalog navodi samo „Pokušaj ponovo“).
4. **ARTIKAL-01 tekst** — katalog „Piće sa tim nazivom već postoji.“ vs. ispod polja „Taj naziv već postoji · ARTIKAL-01“.
5. **PIN-01 tekst** — katalog „PIN nije tačan.“ + dugme „Unesi ponovo“; prikaz spaja u „PIN nije tačan. Unesi ponovo. · PIN-01“ (nema posebnog dugmeta).
6. **PIN-02 „za potvrdu sa vlasnikom“** — prikaz još nije potvrđen; plan (11. dogovor) potvrđuje 10 pokušaja / 5 min, pa je pravilo usaglašeno, ali tabla ga i dalje vodi kao otvoreno.
7. **Forma artikla („način brojanja“, „Zapremina se traži samo za bocu“)** — O6 opisuje stari model (način brojanja + zapremina). Plan: mjere su lista u podešavanjima, artikal ima 1–3 mjere, litre su na mjeri, ne na artiklu. Validaciju treba pisati po planu (obavezno: naziv, kategorija, ≥1 mjera; litre se unose u V4 Mjere).
8. **Navigacija O5** — „V0 → V1–V3 … artikli, ljudi, PIN“ ne pominje V4 Mjere, V5 Kategorije, V6 Pravila rada, V7 O aplikaciji; O1 ikona „Podešavanja · artikli, konobari“ isto zastarjelo.
9. **Fiksni rokovi u tekstu** — „60 s“, „2 min“, „30 dana“ su na tabli tvrdo napisani (npr. RAZLOG-01 „Poništavanje poslije 60 s traži razlog.“, baner „Zaključava se samo za 2 min bez dodira.“, prozor „…ostaje u istoriji 30 dana.“, O7 „briše noći starije od 30 dana“). Po planu su podesivi (V6) → tekstove generisati iz podešavanja.
10. **Jedinice riječju vs. skraćenice** — O1 „Jedinice uvijek riječju: „3 kom“, „5 doza“, „2 bokala“, „1 boca““, a pločice osoba koriste „1 bok.“ (O2, S1). Takođe „2 doze“ vs „9 doza“ — treba pravilo za padeže (1 doza, 2–4 doze, 5+ doza; 1 bokal, 2–4 bokala…).
11. **Najmanji tekst** — O1 „nijedan tekst nije manji od 13 px“, a token `--text-caption` je 12 px i koristi se za labele sekcija („KONOBARI“, „POSLJEDNJE“, „TURA ZA“) na S1.
12. **Dodir za pločicu** — O1 „72 · pločica pića“, a na S1 pločice su 192×120; 72 je očito minimalna visina.
13. **O1 primjer „Potvrdi izdavanje“** — na S1 glavno dugme glasi „Potvrdi · Vuleta“ (ime izabranog), u O2 samo „Potvrdi“.
14. **Toast „Poništi“ u prvih 4 s + čip u traci** — oba nude poništavanje; O3 traka u tekstu se zove „Posljednja izdavanja“, a na S1 labela trake je „Posljednje“.
15. **O3 „Bilježi se u pregledu kao „Poništeno bez PIN-a““** važi i za izmjenu ture prije potvrde po tabeli „Povratne“ — izmjena prije potvrde se ne bilježi (nema ture), samo poništavanje u roku.

---

# Stranica „Šank — tablet“ (S0–S3)

Svi ekrani 1280×800, tamna tema. Zajednički gornji bar (S0, S1.x): visina 72 px, donja linija `--color-line`. Lijevo logo (krug 40 px `--color-primary` sa „919“ u Cinzel bold + „CAFFE BAR“ Cinzel, razmaknuta slova), vertikalni razdjelnik, datum noći bold 20 px (npr. „Petak 20. 11.“). Desno dugmad „outline“ 48 px visine, radijus 12, ikona 22 px + tekst 15 px semibold.

Format datuma: `<Dan u sedmici velikim početnim slovom> <d>. <M>.` sa razmakom („Petak 20. 11.“, „Nedjelja 8. 11.“). Vrijeme 24 h „23:47“. Decimalni zarez, litre na dvije decimale sa „ l“ („0,36 l“, „3,14 l“). Raspon vremena sa crticom „21:04 – 03:12“. Separator stavki je „ · “ (srednja tačka sa razmacima). Množina po srpskom pravilu: „2 doze / 24 doze“, „7 doza / 26 doza / 16 doza“; „1 bokal / 4 bokala“; „1 poništena / 2 poništene“; „4 radnika“.

---

## S0 · Nova noć

**Svrha:** početni ekran kad nema otvorene noći; radnik označava ko radi večeras i otvara noć.

**Raspored:** gornji bar 72 px; centralni blok širine ~920 px (x 180–1100), vertikalno centriran.

**Gornji bar:** logo · „Subota 21. 11.“ (današnji datum, bez značke „Noć otvorena“). Desno: dugme „Istorija“ (ikona sat/strelka unazad) → S3; dugme „Podešavanja“ (ikona katanac) → V0 (PIN).

**Centar:**
- Nadnaslov (caps, `--color-primary`, mali): „Nema otvorene noći“
- Naslov (display ~40 px, extra bold): „Ko radi večeras?“
- Podnaslov (muted): „Označi ko radi. Samo oni se prikazuju na ekranu za unos.“
- Tri kolone (svaka ~280 px, razmak 40 px), naslovi kolona caps muted:
  - „Konobari“: kartice „Vuleta / konobar“, „Srđan / konobar“ (obje označene)
  - „Za šankom“: „Miloš / za šankom“ (označen), „Smitran / za šankom“ (neoznačen)
  - „Ispomoć · samo večeras“: „Dejan / ispomoć“ (označen) + isprekidana kartica „+ Dodaj ispomoć“ → S0.1
- Kartica osobe (280×80, radijus 16): checkbox 32 px lijevo, ime bold 20 px, uloga muted 14 px. Označeno: pozadina `--color-primary-tint`, okvir 1,5–2 px `--color-primary`, checkbox popunjen zlatnom sa crnom kvačicom. Neoznačeno: pozadina `--color-tile`, prazan checkbox sa sivim okvirom, bez okvira kartice. Dodir prebacuje stanje.
- Primarno dugme pune širine (920×64, zlatno, ikona mjesec): „Otvori noć · 4 radnika“ — broj = broj označenih (uključuje ispomoć). Vodi na S1.1 (prazan unos). (Pretpostavka: onemogućeno kad je 0 označenih — nije nacrtano.)
- Napomena ispod (muted, 14 px): „Šank za prosuto je uvijek uključen. Datum noći ostaje subota 21. 11. i poslije ponoći.“

**Pravila na ekranu:** samo označeni se vide na unosu; „Šank“ je uvijek uključen (ne bira se); datum noći = datum otvaranja i poslije ponoći; ispomoć važi samo večeras.

---

## S0.1 · Dodaj ispomoć (prozor)

**Svrha:** unos imena ispomoći pri otvaranju noći.

**Raspored:** S0 zatamnjen (scrim ~60 % crno); dijalog ~520 px širok, gore centriran (y 64–412), radijus 20, pozadina `--color-surface`; sistemska tastatura dole (visina ~260 px) sa srpskom latinicom (q w e r t z u i o p š / a s d f g h j k l č ć / y x c v b n m đ ž).

**Sadržaj dijaloga:**
- Naslov: „Ispomoć večeras“
- Tekst: „Ime se pamti samo za ovu noć. Ispomoć ima svoj zbir u pregledu, a sutra se ne nudi.“
- Labela polja: „Ime“
- Polje za tekst (h 56, fokus: zlatni okvir 2 px, kursor), primjer unosa „Dej“
- Pomoćni tekst: „Kako ga zovu na šanku, npr. „Dejan“ ili „Dejan M.““
- Dugmad desno: „Odustani“ (outline) → zatvara; „Dodaj“ (zlatno, primarno) → dodaje karticu u kolonu „Ispomoć · samo večeras“ označenu (kao „Dejan / ispomoć“ na S0).

**Validacija:** nije nacrtana na ovom ekranu (vidi O6 za pravila imena). Ispomoć bez PIN-a.

---

## S1 · Unos ture (glavni ekran, tura u toku)

**Svrha:** glavni radni ekran tokom noći; izbor konobara + pića, korekcija količina i potvrda ture.

**Raspored (1280×800):**
- Gornji bar 72 px
- Lijeva kolona 276 px (border desno), padding 24 px, kartice 228×80
- Sredina ~640 px (grow), padding 20 px
- Desna kolona 364 px, pozadina `--color-surface`, border lijevo
- Donja traka 72 px (border gore)

**Gornji bar:** logo · „Petak 20. 11.“ · značka (zlatni tint, ikona mjesec) „Noć otvorena od 21:04“. Desno: „Podešavanja“ (katanac) → V0; „Završi noć“ (ikona mjesec) → S1.2. (Na S1 nema dugmeta „Istorija“.)

**Lijeva kolona (osobe):**
- Grupa „Konobari“: „Vuleta“ / „18 kom · 24 doze · 3 bok.“ — IZABRAN: cijela kartica zlatna (`--color-primary`), tekst `--color-on-primary`, desno crni krug 28 px sa zlatnom kvačicom. „Srđan“ / „11 kom · 16 doza · 1 bok.“ — neizabran (`--color-tile`, ime ink, zbir muted).
- Grupa „Za šankom“: „Miloš“ / „9 kom · 7 doza“; „Smitran“ / „6 kom · 12 doza“.
- (prazan prostor, gura dole)
- Grupa „Van zbira“: „Šank“ / „2 doze“ — kartica bez pozadine, isprekidani okvir `--color-neutral-line`.
- Zbir po osobi: samo mjere koje postoje, skraćeno („kom“, „doza/doze“, „bok.“), tabularne cifre. Mjera sa nulom se izostavlja („9 kom · 7 doza“).
- Grupa „Ispomoć“ nije nacrtana na S1 (ta noć je nema) — vidi napomene.
- Dodir na osobu = izbor primaoca ture (jedan izabran u isto vrijeme).

**Sredina (pića):**
- Red čipova kategorija (h 48, pill): „Sve“ (aktivan: popunjen `--color-ink`, tekst tamni), „Pivo“, „Bezalkoholno“, „Žestina“ (neaktivni: outline `--color-line`). Desno polje pretrage 150×48 pill, ikona lupa, placeholder „Traži piće“.
- Mreža pločica 3 kolone × 4 reda, pločica 192×120, razmak 12, radijus 16, `--color-tile`.
- **Pločica sa jednom mjerom:** naziv gore (18 px semibold), mjera dole muted 14 px: „komad“, „doza 0,03 l“, „boca 0,70 l“. Dodir = +1 u turi. Kada je u turi: okvir 2 px zlatni + zlatna značka-brojač gore desno (min 28 px, broj 15 px extra bold), npr. „Jelen pivo“ 4, „Jack Daniel’s“ 2.
- **Pločica sa 2 mjere** (Rakija šljiva, Rakija dunja): naziv gore, ispod dva dugmeta jedno pored drugog (h 56, radijus 12, pozadina `--color-bg`, okvir `--color-line`) sa natpisima „0,03“ i „Bokal“. Aktivna mjera u turi: dugme `--color-primary-tint` sa zlatnim okvirom i tekstom „Bokal · 1“ (natpis · količina); cijela pločica dobija zlatni okvir.
- Prikazani artikli: Jelen pivo (komad), Heineken (komad), Coca-Cola (komad), Jack Daniel’s (doza 0,03 l), Johnnie Walker (doza 0,03 l), Smirnoff (doza 0,03 l), Rakija šljiva (0,03 | Bokal), Rakija dunja (0,03 | Bokal), Red Bull (komad), Kisela voda (komad), Schweppes Tonic (komad), Jack Daniel’s 0,7 (boca 0,70 l). (Primjeri — stvarni artikli dolaze iz podešavanja.)

**Desna kolona (tura):**
- Labela caps „Tura za“, ispod ime izabranog bold 28 px: „Vuleta“.
- Lista stavki (redovi sa donjom linijom, py 8): naziv 16 px semibold + ispod muted 13 px mjera; desno stepper: „−“ dugme 56×56 (`--color-tile`, radijus 12), količina 24 px extra bold u polju 48 px, „+“ dugme 56×56.
  - „Jelen pivo“ / „komad“ / 4
  - „Jack Daniel’s“ / „doza · 0,06 l“ / 2 (litre = količina × litre mjere)
  - „Rakija šljiva“ / „bokal · 0,25 l“ / 1
  - Komad nema litre (samo „komad“).
- (prostor)
- Zbirni red: lijevo muted „4 kom · 2 doze · 1 bokal“, desno semibold „žestina 0,31 l“.
- Primarno dugme pune širine, h 64, zlatno, ikona kvačica: „Potvrdi · Vuleta“ → upisuje turu, prazni turu i poništava izbor konobara (→ stanje S1.1), tura se pojavljuje prva u „Posljednje“ sa odbrojavanjem.
- Pretpostavka: „−“ na 1 uklanja stavku iz ture.

**Donja traka „Posljednje“:**
- Labela caps „Posljednje“.
- Čip ture (pozadina `--color-surface`, radijus 12): vrijeme muted „23:47“ · ime bold „Srđan“ · sadržaj „2 Heineken · 1 Coca-Cola“ · dugme outline h 40 sa ikonom „undo“: „Poništi · 41 s“ (odbrojavanje do isteka roka bez PIN-a; dodir poništava turu bez PIN-a).
- Starija tura bez dugmeta: „23:44“ · „Miloš“ · „3 Jack · 2 Red Bull“ (skraćeni nazivi pića!). Dodir na takvu turu (van roka) vjerovatno otvara S1.3.
- Desno dugme (pozadina `--color-tile`, h 48, ikona grafikon): „Pregled noći“ → S2.

---

## S1.1 · Nova tura (prazno)

**Svrha:** stanje glavnog ekrana odmah poslije potvrde / na početku noći — nijedan konobar nije izabran, tura prazna.

**Razlike u odnosu na S1:**
- Lijevo: nijedna kartica nije izabrana. Vuleta sada „22 kom · 26 doza · 4 bok.“ (zbir porastao za potvrđenu turu 4 Jelen + 2 doze + 1 bokal); Srđan „11 kom · 16 doza · 1 bok.“; Miloš „9 kom · 7 doza“; Smitran „6 kom · 12 doza“; Šank „2 doze“.
- Sredina: nijedna pločica nije označena (bez brojača i zlatnih okvira); oba dugmeta rakija „0,03“ / „Bokal“ neutralna.
- Desno: labela caps „Tura“ (bez imena). Prazno stanje u centru: krug 64 px sa „+“, naslov „Nova tura“, tekst „Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.“ Dole onemogućeno dugme (h 64, `--color-disabled-bg`, tekst/ikona `--color-disabled-ink`): „✓ Potvrdi“; ispod pomoćni tekst muted 13 px: „Izaberi konobara i dodaj piće.“
- Posljednje: „23:52“ · „Vuleta“ · „4 Jelen · 2 Jack · 1 bokal“ · „Poništi · 58 s“; zatim „23:47“ · „Srđan“ · „2 Heineken · 1 Coca-Cola“ (bez dugmeta — rok istekao). „Pregled noći“ desno.

**Pravila:** potvrda je onemogućena dok nema i primaoca i bar jednog pića. Redoslijed izbora (konobar/pića) slobodan.

---

## S1.2 · Završi noć (prozor)

**Svrha:** potvrda zatvaranja i arhiviranja noći uz kratak zbir.

**Raspored:** S1.1 zatamnjen; dijalog ~520 px, centriran (y 176–626), radijus 20.

**Sadržaj:**
- Naslov: „Završiti noć?“
- Tekst: „Petak 20. 11. se arhivira i ostaje u istoriji 30 dana. Ekran za unos ostaje prazan dok ne otvoriš novu noć.“ (30 = podešavanje istorije)
- Tabela (ime lijevo bold, zbir desno, linije između):
  - „Vuleta“ — „22 kom · 26 doza · 4 bokala“
  - „Srđan“ — „11 kom · 16 doza · 1 bokal“
  - „Miloš“ — „9 kom · 7 doza“
  - „Smitran“ — „6 kom · 12 doza“
  - „Šank · van zbira“ — „2 doze“
  (Ovdje pune riječi „bokala/bokal“, na S1 skraćeno „bok.“.)
- Red ispod: muted „Od 21:04 · 64 ture · 2 poništene“; desno zlatni link „Cijeli pregled“ → S2.
- Dugmad: „Nastavi unos“ (outline, h 56) → zatvara dijalog; „Završi noć“ (zlatno, h 56) → arhivira, vodi na S0 (prazan ekran).
- Nema PIN-a za završetak noći.

---

## S1.3 · Poništavanje s PIN-om (prozor)

**Svrha:** poništavanje ture poslije isteka roka bez PIN-a: izbor razloga + PIN vlasnika.

**Raspored:** S1.1 zatamnjen; dijalog ~560 px (x 360–920, y 88–712), radijus 20.

**Sadržaj:**
- Naslov sa zlatnom ikonom katanca: „Poništiti turu?“
- Kartica ture (pozadina tamnija): „23:47“ · „Srđan“ · „2 Heineken · 1 Coca-Cola“
- Tekst: „Prošlo je više od 60 s, zato treba PIN vlasnika. Tura ostaje u pregledu, precrtana, i ne ulazi u zbir.“ (60 s = podešeni rok)
- Labela „Razlog“; čipovi (h 48–56, radijus 12, jedan izbor): „Pogrešan konobar“ (izabran: `--color-primary-tint`, zlatni okvir, zlatna kvačica + tekst), „Pogrešno piće“, „Pogrešna količina“, „Unijeto dvaput“, „Vraćeno“ (neizabrani: `--color-tile`). Lista dolazi iz Podešavanja → Pravila rada.
- Red „PIN vlasnika“ (labela lijevo), desno **4 tačke** (2 popunjene ink, 2 prazne sa sivim okvirom) + dugme za brisanje (⌫, 56×56).
- Numerička tastatura 2 reda × 5: „1 2 3 4 5“ / „6 7 8 9 0“, tasteri ~94×56, `--color-tile`, cifre 22 px.
- Dugmad: „Odustani“ (outline) → zatvara; „Poništi turu“ (ikona undo) — ONEMOGUĆENO dok PIN nije potpun (i razlog izabran); kad je omogućeno, upisuje poništenje (razlog + vrijeme) i ture se precrtava u pregledu.
- Pogrešan PIN / blokada: vidi O4 (PIN-01/PIN-02) i T5.

---

## S2 · Pregled noći — po konobaru

**Svrha:** zbir noći po osobi i artiklu, po mjerama i litrama.

**Raspored:** gornji bar 72 px; lijeva lista osoba ~300 px (border desno); desno tabela (~950 px, padding 32).

**Gornji bar:** dugme outline sa „<“: „Nazad na unos“ → S1. Naslov bold „Pregled noći“ + muted „Petak 20. 11. · od 21:04 · 64 ture · 2 poništene“. Desno segmentni pill tabovi (h 48): „Po konobaru“ (aktivan, popunjen ink) → S2, „Po piću“ → S2.1, „Ture“ → S2.2.

**Lijevo:** grupe caps „Konobari“, „Za šankom“, „Van zbira“. Stavke (ime bold 18 + zbir muted 14): „Vuleta“ / „22 kom · 26 doza · 4 bokala“ (izabran: `--color-primary-tint` + zlatni okvir 1 px, radijus 12); „Srđan“ / „11 kom · 16 doza · 1 bokal“; „Miloš“ / „9 kom · 7 doza“; „Smitran“ / „6 kom · 12 doza“; dole „Šank“ + muted „prosuto i ostalo“ / „2 doze“. Dodir bira osobu za tabelu.

**Desno:** naslov osobe bold 28 px „Vuleta“; desno muted „24 ture · 1 poništena (ne ulazi u zbir)“.
Tabela, zaglavlja caps muted: „Piće“ | „Komadi“ | „Doze“ | „Bokali“ | „Litre“ (brojevi desno poravnati, „—“ za nulu/nepostojeće):
| Piće | Komadi | Doze | Bokali | Litre |
|---|---|---|---|---|
| Jelen pivo | 10 | — | — | — |
| Heineken | 6 | — | — | — |
| Coca-Cola | 4 | — | — | — |
| Red Bull | 2 | — | — | — |
| Jack Daniel’s | — | 12 | — | 0,36 l |
| Johnnie Walker | — | 8 | — | 0,24 l |
| Smirnoff | — | 6 | — | 0,18 l |
| Rakija šljiva | — | — | 3 | 0,75 l |
| Rakija dunja | — | — | 1 | 0,25 l |
| **Ukupno** | **22** | **26** | **4** | **1,78 l** |

Redoslijed redova: komadi pa žestina (po redoslijedu artikala). Poništene ture se ne broje.

---

## S2.1 · Pregled noći — po piću

**Svrha:** zbirna potrošnja svakog artikla po mjeri, po osobama, uz litre.

**Gornji bar:** isti, aktivan tab „Po piću“. Nema lijeve kolone — tabela preko cijele širine (padding 32).

**Tabela**, zaglavlja caps: „Piće“ | „Jedinica“ | „Vuleta“ | „Srđan“ | „Miloš“ | „Smitran“ | „Šank“ | „Ukupno“ | „Litre“ (kolone osoba dinamične = ko je radio + Šank). Jedan red po paru artikal+mjera (Rakija šljiva se pojavljuje dva puta: doza i bokal). Jedinica malim slovima: „komad“, „doza“, „bokal“.
| Piće | Jedinica | Vuleta | Srđan | Miloš | Smitran | Šank | Ukupno | Litre |
|---|---|---|---|---|---|---|---|---|
| Jelen pivo | komad | 10 | 5 | 4 | — | — | 19 | — |
| Heineken | komad | 6 | 4 | — | — | — | 10 | — |
| Coca-Cola | komad | 4 | 2 | — | 3 | — | 9 | — |
| Red Bull | komad | 2 | — | 2 | — | — | 4 | — |
| Kisela voda | komad | — | — | 3 | — | — | 3 | — |
| Schweppes Tonic | komad | — | — | — | 3 | — | 3 | — |
| Jack Daniel’s | doza | 12 | 6 | 3 | — | 2 | 23 | 0,69 l |
| Johnnie Walker | doza | 8 | 4 | — | 6 | — | 18 | 0,54 l |
| Smirnoff | doza | 6 | — | 4 | — | — | 10 | 0,30 l |
| Rakija šljiva | doza | — | 6 | — | 6 | — | 12 | 0,36 l |
| Rakija šljiva | bokal | 3 | — | — | — | — | 3 | 0,75 l |
| Rakija dunja | bokal | 1 | 1 | — | — | — | 2 | 0,50 l |

Podnožje: lijevo muted „Šank je u zbiru pića, ali ne i u zbiru konobara. Rakija šljiva ukupno: 0,36 l + 0,75 l = 1,11 l.“; desno bold 20 px „Žestina ukupno 3,14 l“.

(Brojevi su međusobno usklađeni sa S1.1/S1.2/S2/S3: 48 komada, 3,14 l.)

---

## S2.2 · Pregled noći — ture

**Svrha:** hronološka lista pojedinačnih tura sa oznakama poništenih.

**Gornji bar:** isti, aktivan tab „Ture“.

**Filter red (ispod bara, padding 32):** pill čipovi h 48: „Svi“ (aktivan), „Vuleta“, „Srđan“, „Miloš“, „Smitran“, „Šank“ (jedan izbor). Desno toggle-čip: „Samo poništene · 2“.

**Lista** (najnovije gore, redovi ~53 px sa donjom linijom): vrijeme muted | ime bold (kolona ~136 px) | sadržaj ture sa punim nazivima | desno oznaka.
- „23:52“ „Vuleta“ „4 Jelen pivo · 2 Jack Daniel’s · 1 bokal Rakija šljiva“ (mjera ispred naziva kad nije podrazumijevana: „1 bokal Rakija šljiva“)
- „23:47“ „Srđan“ „2 Heineken · 1 Coca-Cola“
- „23:44“ „Miloš“ „3 Jack Daniel’s · 2 Red Bull“
- „23:31“ „Vuleta“ „3 Johnnie Walker · 2 Coca-Cola“ — PRECRTANO (ime i sadržaj, muted); značka danger-tint sa ikonom undo „Poništeno“ + muted „Pogrešan konobar · PIN-om u 23:36“
- „23:18“ „Šank“ „2 Jack Daniel’s“
- „23:02“ „Smitran“ „3 Schweppes Tonic · 6 Rakija šljiva“
- „22:47“ „Srđan“ „1 Heineken“ — PRECRTANO; značka warning-tint sa ikonom trougao „Poništeno bez PIN-a“ + muted „Poništeno u 22:47“
- „22:41“ „Miloš“ „3 Kisela voda · 4 Smirnoff“
- „22:30“ „Vuleta“ „6 Jelen pivo · 4 Jack Daniel’s“
(Lista se skroluje; prazno stanje za filter → T4.)

Format oznake: PIN poništenje = „<razlog> · PIN-om u HH:MM“; bez PIN-a = „Poništeno u HH:MM“ (bez razloga).

---

## S3 · Istorija

**Svrha:** lista zatvorenih noći (30 dana) sa sažetkom; ulaz u pregled zatvorene noći (T7).

**Gornji bar:** dugme outline „< Nazad“ → S0 (ulaz je sa S0). Naslov bold „Istorija“ + muted „Zatvorene noći se čuvaju 30 dana, pa se brišu same“.

**Tabela** (padding 32), zaglavlja caps: „Noć“ | „Vrijeme“ | „Ture“ | „Ko je radio“ | „Komadi“ | „Žestina“ | (strelica „>“). Redovi 64 px, cijeli red klikabilan → pregled te noći (T7, samo čitanje).
| Noć | Vrijeme | Ture | Ko je radio | Komadi | Žestina |
|---|---|---|---|---|---|
| Petak 20. 11. | 21:04 – 03:12 | 64 · 2 poništene | Vuleta, Srđan, Miloš, Smitran | 48 | 3,14 l |
| Nedjelja 15. 11. | 20:48 – 01:30 | 31 | Vuleta, Miloš | 22 | 1,46 l |
| Subota 14. 11. | 21:10 – 03:55 | 78 · 1 poništena | Vuleta, Srđan, Miloš, Smitran | 57 | 4,02 l |
| Petak 13. 11. | 21:00 – 03:40 | 71 | Vuleta, Srđan, Miloš, Dejan (ispomoć) | 51 | 3,71 l |
| Nedjelja 8. 11. | 20:55 – 01:15 | 27 | Srđan, Smitran | 19 | 1,12 l |
| Subota 7. 11. | 21:06 – 04:02 | 83 · 3 poništene | Vuleta, Srđan, Miloš, Smitran | 61 | 4,48 l |
| Petak 6. 11. | 21:02 – 03:20 | 66 | Vuleta, Srđan, Miloš | 45 | 3,30 l |
| *(razdjelnik muted)* „Još 5 noći ispod · listaj“ |
| Petak 23. 10. + ispod zlatno/warning „Briše se sutra“ | 21:00 – 03:05 | 59 | Vuleta, Srđan, Smitran | 42 | 2,95 l |

- „Ture“: broj, a „ · N poništen(a/e)“ samo ako ima poništenih.
- Ispomoć u listi imena: „Dejan (ispomoć)“.
- Oznaka „Briše se sutra“ (`--color-warning`, 12–13 px) za noć kojoj ističe rok sutra.
- Prazna istorija: nije nacrtana (vidi O-katalog ako postoji).

---

# Osnova: ivična stanja (T1–T7, 1280×800)

## T1 · Pretraga bez rezultata (S1)

**Svrha:** S1 (unos ture) kad pretraga pića nema pogodaka.

**Raspored:** isti kao S1. Gornja traka 72 px, lijeva kolona 276 px, sredina oko 640 px, desni panel 364 px, donja traka 72 px.

- **Gornja traka:** isto kao S1.
  - Logo „919“ + „CAFFE BAR“
  - „Petak 20. 11.“
  - Značka „Noć otvorena od 21:04“
  - Dugmad „Podešavanja“ i „Završi noć“
- **Lijeva kolona:** isto kao S1.
  - „KONOBARI“: Vuleta je izabran (zlatna pločica sa kvačicom), „18 kom · 24 doze · 3 bok.“. Srđan „11 kom · 16 doza · 1 bok.“.
  - „ZA ŠANKOM“: Miloš „9 kom · 7 doza“, Smitran „6 kom · 12 doza“.
  - „VAN ZBIRA“: Šank „2 doze“ (isprekidan okvir).
- **Sredina:**
  - Čipovi kategorija „Sve“ (aktivan), „Pivo“, „Bezalkoholno“, „Žestina“.
  - Polje pretrage je u fokusu, sa zlatnim okvirom i unesenim tekstom „mojito“. Širina polja je oko 200 px, šire nego u S1 (150 px).
  - Mreža pločica je zamijenjena praznim stanjem:
    - ikona lupe u krugu
    - naslov „Nema pića „mojito““ (traženi pojam u navodnicima „…“)
    - tekst „Provjeri naziv ili traži po prvim slovima. Novo piće dodaje vlasnik u podešavanjima.“
    - sekundarno dugme „Očisti pretragu“, koje briše upit i vraća mrežu pića
- **Desni panel (prazna tura):**
  - Oznaka „TURA“, bez imena konobara.
  - Ikona „+“ u krugu, naslov „Nova tura“, tekst „Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.“
  - Dugme „✓ Potvrdi“ je onemogućeno (siva podloga, siv tekst).
  - Ispod dugmeta: „Dodaj piće u turu.“
- **Donja traka:**
  - „POSLJEDNJE“: jedna stavka „23:47 Srđan 2 Heineken · 1 Coca-Cola“, bez dugmeta „Poništi“ (rok za poništavanje je istekao).
  - Desno dugme „Pregled noći“.

**Pravila na ekranu:** pretraga radi po prvim slovima naziva. Novo piće ne dodaje radnik, nego vlasnik u podešavanjima.

## T2 · Prvo pokretanje — još nema pića (S1)

**Svrha:** S1 neposredno poslije otvaranja prve noći, dok u podešavanjima još nema nijednog artikla.

**Raspored:** isti kao S1 (276 / oko 640 / 364, gornja i donja traka po 72 px).

- **Gornja traka:**
  - „Subota 21. 11.“
  - Značka „Noć otvorena od 21:02“
  - „Podešavanja“ i „Završi noć“, oba aktivna
- **Lijeva kolona:**
  - „KONOBARI“: Vuleta „još ništa“, Srđan „još ništa“.
  - „ZA ŠANKOM“: samo Miloš „još ništa“. Smitran nije prikazan jer nije označen pri otvaranju noći.
  - „VAN ZBIRA“: Šank „prosuto i ostalo“.
  - Ništa nije izabrano.
  - Kad osoba nema nijednu turu, umjesto zbira stoji „još ništa“. Kad Šank nema ture, ispod naziva stoji „prosuto i ostalo“.
- **Sredina:**
  - Nema čipova kategorija ni pretrage.
  - Prazno stanje: ikona čaše u krugu, naslov „Još nema pića“, tekst „Prije prve ture vlasnik unosi pića i mjere u podešavanjima. Treba mu PIN.“
  - Primarno zlatno dugme „Podešavanja“ vodi u V0 (PIN za podešavanja).
- **Desni panel:**
  - „TURA“, „Nova tura“, „Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.“
  - Dugme „✓ Potvrdi“ je onemogućeno.
  - Ispod dugmeta: „Prvo treba dodati pića.“
- **Donja traka:**
  - „POSLJEDNJE“ i tekst „Još nema tura ove noći.“
  - Dugme „Pregled noći“ je onemogućeno (siva ikona i tekst).

## T3 · Pregled — još nema tura (S2)

**Svrha:** pregled noći (S2) dok tekuća noć nema nijednu potvrđenu turu.

**Raspored:** gornja traka 72 px, a ispod nje prazno stanje centrirano preko cijele širine. Nema lijeve kolone ni tabele.

- **Gornja traka:**
  - Lijevo dugme sa obrisom „‹ Nazad na unos“ vodi na S1.
  - Naslov „Pregled noći“.
  - Podnaslov „Subota 21. 11. · od 21:02 · 0 tura“ (format: dan datum · od HH:MM · N tura).
  - Desno segmentni tabovi „Po konobaru“ (aktivan, svijetla puna pilula), „Po piću“ (S2.1) i „Ture“ (S2.2).
- **Sadržaj:**
  - ikona grafikona u krugu
  - naslov „Još nema tura ove noći“ (bez tačke)
  - tekst „Pregled se puni čim se potvrdi prva tura. Prethodne noći su u istoriji.“
  - primarno zlatno dugme „Nazad na unos“ (vodi na S1)

**Napomena:** na ekranu postoje dvije akcije „Nazad na unos“, jedna u gornjoj traci i jedna u sadržaju.

## T4 · Ture — nema za filter (S2.2)

**Svrha:** tab „Ture“ (S2.2) kad zadati filteri ne daju nijednu turu.

**Raspored:** gornja traka 72 px, red filtera oko 64 px, ispod prazno stanje centrirano.

- **Gornja traka:**
  - „‹ Nazad na unos“
  - „Pregled noći“
  - Podnaslov „Petak 20. 11. · od 21:04 · 64 ture · 2 poništene“
  - Tabovi „Po konobaru“, „Po piću“, „Ture“ (aktivan „Ture“)
- **Red filtera:**
  - Lijevo čipovi osoba: „Svi“, „Vuleta“, „Srđan“, „Miloš“ (aktivan, svijetla puna pilula), „Smitran“, „Šank“. Biraju jednu osobu.
  - Desno preklopni čip „Samo poništene“ (aktivan, pun).
- **Prazno stanje:**
  - ikona strelice za poništavanje u krugu
  - naslov „Miloš nema poništenih tura“ (šablon: „{ime} nema poništenih tura“)
  - tekst „Ove noći ima 12 tura i nijedna nije poništena.“ (12 je broj Miloševih tura, a ne ukupan broj tura noći)
  - sekundarno dugme „Očisti filtere (2)“, gdje je u zagradi broj aktivnih filtera (osoba + „Samo poništene“); vraća na „Svi“ i isključuje „Samo poništene“

## T5 · PIN zaključan (PIN-02) · za potvrdu

**Svrha:** modalni prozor poslije 10 pogrešnih PIN-ova zaredom. Sufiks „· za potvrdu“ u nazivu artboarda govori da ovaj ekran još čeka odobrenje.

**Raspored:**
- Ispod prozora je S1 zatamnjen. Ništa nije izabrano, desni panel je prazan, a pod dugmetom „Potvrdi“ (onemogućenim) stoji „Izaberi konobara i dodaj piće.“
- Prozor je centriran, širok oko 460 px.

**Pozadinski podaci (S1):**
- Vuleta „22 kom · 26 doza · 4 bok.“, Srđan „11 kom · 16 doza · 1 bok.“, Miloš „9 kom · 7 doza“, Smitran „6 kom · 12 doza“, Šank „2 doze“.
- Pločice:

  | Piće | Mjera na pločici |
  | --- | --- |
  | Jelen pivo | komad |
  | Heineken | komad |
  | Coca-Cola | komad |
  | Jack Daniel’s | doza 0,03 l |
  | Johnnie Walker | doza 0,03 l |
  | Smirnoff | doza 0,03 l |
  | Rakija šljiva | dugmad „0,03“ i „Bokal“ |
  | Rakija dunja | dugmad „0,03“ i „Bokal“ |
  | Red Bull | komad |
  | Kisela voda | komad |
  | Schweppes Tonic | komad |
  | Jack Daniel’s 0,7 | boca 0,70 l |

- Donja traka:
  - „23:52 Vuleta 4 Jelen · 2 Jack · 1 bokal“, sa dugmetom „Poništi · 58 s“
  - „23:47 Srđan 2 Heineken · 1 Coca-Cola“
  - „Pregled noći“

**Prozor:**
- ikona katanca u krugu (boja upozorenja)
- naslov „Previše pogrešnih pokušaja“
- tekst „PIN je deset puta zaredom bio pogrešan. Novi pokušaj je moguć u 23:58.“ (vrijeme otključavanja je apsolutno, HH:MM)
- naglašen tekst „Ništa nije promijenjeno. Unos tura radi normalno.“
- primarno zlatno dugme preko cijele širine, „Nazad na unos“, zatvara prozor
- sitno ispod: „Šifra: PIN-02“

**Pravila:** 10 pogrešnih PIN-ova zaredom zaključava unos PIN-a. Prikazuje se vrijeme kad je novi pokušaj moguć. Vrijeme na ekranu (23:58 naspram posljednje ture u 23:52) odgovara bloku od oko 5 minuta. Unos tura i dalje radi.

## T6 · Evidencija se ne može otvoriti (BAZA-01)

**Svrha:** ekran greške pri pokretanju, kad se lokalna baza ne učita.

**Raspored:**
- Gornja traka 72 px sa logom „919“ i „CAFFE BAR“, bez datuma i dugmadi.
- Ispod je blok centriran, širine oko 400 px.

**Sadržaj:**
- ikona „!“ u crvenom krugu (opasnost)
- naslov „Evidencija se ne može otvoriti“
- prigušen tekst „Baza na tabletu se nije učitala pri pokretanju.“
- naglašen tekst „Podaci su i dalje na tabletu. Ne briši aplikaciju i ne čisti njene podatke.“
- primarno zlatno dugme preko cijele širine, „Pokušaj ponovo“, koje ponovo pokušava otvoriti bazu
- „Javi vlasniku.“, tekst u boji linka (`--color-link`) bez definisane akcije (vjerovatno samo uputstvo, ne dugme)
- sitno: „Šifra: BAZA-01“

## T7 · Pregled zatvorene noći (S3 → S2)

**Svrha:** pregled arhivirane noći otvoren iz Istorije (S3). Ovo je S2 u režimu samo za čitanje.

**Raspored:**
- gornja traka 72 px
- baner upozorenja oko 52 px
- lijeva kolona oko 300 px
- desno sadržaj oko 980 px (tabela)

**Gornja traka:**
- Dugme sa obrisom „‹ Istorija“ vodi nazad na S3.
- Naslov „Petak 20. 11.“.
- Podnaslov „21:04 – 03:12 · 64 ture · 2 poništene“ (format: početak – kraj · N ture · M poništene).
- Tabovi „Po konobaru“ (aktivan), „Po piću“, „Ture“.

**Baner (pozadina `--color-warning-tint`):** ikona istorije, zatim podebljano „Zatvorena noć, samo za čitanje.“ i obično „Ture se ovdje ne poništavaju. Briše se 20. 12.“ Datum brisanja je datum noći plus rok istorije od 30 dana.

**Lijeva kolona (lista, ne pločice):**
- „KONOBARI“: Vuleta (izabran, zlatni okvir) „22 kom · 26 doza · 4 bokala“, Srđan „11 kom · 16 doza · 1 bokal“.
- „ZA ŠANKOM“: Miloš „9 kom · 7 doza“, Smitran „6 kom · 12 doza“.
- „VAN ZBIRA“: „Šank“ uz sitno „prosuto i ostalo“, ispod „2 doze“.
- Izbor osobe mijenja tabelu desno.

**Desno:**
- Naslov osobe „Vuleta“, a desno od njega „24 ture · 1 poništena (ne ulazi u zbir)“.
- Tabela sa kolonama „PIĆE“, „KOMADI“, „DOZE“, „BOKALI“, „LITRE“. Brojevi su poravnati desno. Prazna ćelija je „—“. Litre se pišu sa zarezom i oznakom „ l“.

  | Piće | Komadi | Doze | Bokali | Litre |
  | --- | --- | --- | --- | --- |
  | Jelen pivo | 10 | — | — | — |
  | Heineken | 6 | — | — | — |
  | Coca-Cola | 4 | — | — | — |
  | Red Bull | 2 | — | — | — |
  | Jack Daniel’s | — | 12 | — | 0,36 l |
  | Johnnie Walker | — | 8 | — | 0,24 l |
  | Smirnoff | — | 6 | — | 0,18 l |
  | Rakija šljiva | — | — | 3 | 0,75 l |
  | Rakija dunja | — | — | 1 | 0,25 l |
  | **Ukupno** | **22** | **26** | **4** | **1,78 l** |

  Litre se računaju samo za mjere koje imaju litre. Komad nema litre.
- Nema kolone za boce, iako postoje mjere „Boca 0,7“ i „Boca 1 l“.

**Interakcije:**
- Nema dugmadi „Poništi“ ni „Nazad na unos“.
- Nema „Završi noć“ ni „Podešavanja“.

## Uočene nedosljednosti (T1–T7)

1. **Format zbira u lijevoj koloni:** u S1, T1 i T5 piše „3 bok.“ / „1 bok.“ (skraćeno), a u T7 „4 bokala“ / „1 bokal“ (puna riječ). Treba odlučiti jedan format ili svjesno koristiti skraćenicu samo na uskim pločicama S1.
2. **T1, izabran konobar a prazan panel:** Vuleta je izabran (kvačica), a desni panel pokazuje „TURA“ bez imena i uputstvo „Dodirni konobara lijevo…“. U S1 stoji „TURA ZA Vuleta“. Kad je konobar izabran, a tura prazna, logičnije je „TURA ZA Vuleta“ i helper „Dodaj piće u turu.“. Helper je dobar, naslov nije.
3. **Tri različita helpera ispod onemogućenog „Potvrdi“:**
   - „Dodaj piće u turu.“ (T1, konobar izabran)
   - „Prvo treba dodati pića.“ (T2, nema artikala)
   - „Izaberi konobara i dodaj piće.“ (T5, ništa izabrano)

   Ovo izgleda namjerno (zavisi od stanja), pa ih treba uvesti kao pravilo.
4. **T5 i trajanje blokade:** tekst kaže „deset puta zaredom“, što se slaže sa planom (10 pokušaja). Trajanje od 5 minuta nigdje nije napisano, nego se vidi samo kroz apsolutno vrijeme „23:58“. Prozor je otvoren iznad S1, ali nije jasno iz kog toka dolazi (Podešavanja / V0 ili poništavanje s PIN-om / S1.3). Na samom T5 broj cifara PIN-a se ne vidi (vidi V0 / S1.3 / V3 za 4 naspram 6 cifara). Artboard je označen „za potvrdu“, dakle nije konačno odobren.
5. **T4:** „Ove noći ima 12 tura…“ se odnosi na Miloša, a zvuči kao ukupan broj tura noći (podnaslov kaže 64). Prijedlog: „Miloš ima 12 tura ove noći i nijedna nije poništena.“. Čip osoba „Svi“ se razlikuje od čipa kategorija „Sve“ u S1, ali to je gramatički ispravno.
6. **T2:**
   - „Završi noć“ je aktivno iako noć nema nijednu turu. Treba definisati da li se prazna noć može završiti.
   - „Pregled noći“ je onemogućen, iako T3 dizajnira prazan pregled. Dva puta je riješeno isto stanje: dugme je u T2 onemogućeno, a T3 pokazuje prazan pregled. Treba izabrati jedno.
7. **T6:** „Javi vlasniku.“ je u boji linka, a nema akciju. U Compose ga treba prikazati kao običan tekst ili mu definisati akciju.
8. **T7:**
   - Tabela nema kolonu za boce (mjere „Boca 0,7“ / „Boca 1 l“ postoje u planu i na pločici „Jack Daniel’s 0,7“ — „boca 0,70 l“). Kolone treba generisati dinamički iz aktivnih mjera.
   - Lijeva kolona je lista bez pločica (drugačije nego S1).

---

# Stranica „Vlasnik — podešavanja (PIN)“ (V0–V7)

Svi ekrani su 1280×800, landscape, tamna tema. Tekstovi su prepisani tačno kako stoje u dizajnu.

## Zajednička ljuska podešavanja (V1–V7, i pozadina V1.1/V1.2)

**Regioni (odozgo nadolje):**
1. **Gornja traka** (~72 px visine, cijela širina):
   - lijevo dugme sa strelicom „‹“ i natpisom **„Nazad na unos“** (outline, ~168×48). Vodi nazad na S1 (unos ture). Pretpostavka: podešavanja se pri tome zaključavaju ili ostaju otključana do isteka roka (dizajn to ne kaže).
   - naslov **„Podešavanja“** (bold ~24 px) i pored njega sivi tekst **„samo vlasnik“**.
2. **Baner „otključano“** (~52 px, cijela širina, tamno-zlatna/warning podloga, zlatna linija ispod):
   - ikona katanca, bold tekst **„Otključano za vlasnika.“**, iza njega običan tekst **„Zaključava se samo za 2 min bez dodira.“**
   - Vrijeme u tekstu je **živo odbrojavanje**. Na V2 piše „Zaključava se samo za 1 min 40 s bez dodira.“, na V3 „Zaključava se samo za 1 min 52 s bez dodira.“, na ostalima „… za 2 min …“. Format je „X min“ kad su sekunde 0, inače „X min Y s“. Svaki dodir vraća brojač na podešenu vrijednost (V6, početno 2 min).
   - desno outline dugme sa katancem **„Zaključaj sada“**: odmah zaključava podešavanja i vraća na unos (S1).
3. **Lijevi meni** (~276 px širine, desna linija razdvajanja):
   - natpis grupe **„PODEŠAVANJA“** (caps, caption)
   - 7 stavki. Svaka ima ikonu, naslov (bold ~17 px) i podnaslov (sivo ~13 px), visine ~56 px:
     | Stavka | Podnaslov | Ekran |
     |---|---|---|
     | Artikli (ikona čaše) | „15 pića · redoslijed“ | V1 |
     | Kategorije (ikona mreže) | „3 kategorije“ | V5 |
     | Mjere (ikona mjerne posude) | „6 mjera u litrima“ | V4 |
     | Ljudi (ikona ljudi) | „4 stalna radnika“ | V2 |
     | Pravila rada (ikona klizača) | „poništavanje, istorija“ | V6 |
     | PIN vlasnika (ikona katanca) | „promjena“ | V3 |
     | O aplikaciji (ikona „i“) | „verzija, kontakt“ | V7 |
   - Aktivna stavka ima zlatni okvir (1,5 px), tamno-zlatnu podlogu i zlatnu ikonu.
   - Podnaslovi su dinamički brojači: broj pića, broj kategorija, broj mjera, broj stalnih radnika.
   - Na dnu menija stoji sivi tekst **„Sve promjene važe odmah, bez nove verzije aplikacije.“**
4. **Radni dio** (~1004 px, padding ~28 px): sadržaj izabrane sekcije.

Zajednički obrasci u radnom dijelu:
- Liste imaju redove visine ~60–64 px, tanke linije između redova, prekidač (uklj./isklj.) desno i strelicu „›“ za izmjenu.
- Isključen red ima sivi naziv i sivi prekidač.
- Ručica za prevlačenje (⋮⋮) na lijevom kraju reda služi za promjenu redoslijeda.
- Primarno dugme („+ …“) je gore desno, zlatno, ~48 px visine.
- Ispod liste stoji sivi tekst s pravilima.

---

## V0 · PIN za podešavanja (prozor)

**Svrha:** vlasnik upisuje PIN da otključa podešavanja. Prozor se otvara preko S1 kad se dodirne „Podešavanja“ u gornjoj traci.

**Pozadina:** zatamnjeni S1 sa praznom turom. Desni panel ima natpis „TURA“, krug sa „+“, naslov „Nova tura“, tekst „Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.“, onemogućeno dugme „✓ Potvrdi“ i ispod njega „Izaberi konobara i dodaj piće.“ U traci „Posljednje“ stoje dva izdavanja:
- „23:52 · Vuleta · 4 Jelen · 2 Jack · 1 bokal“ sa dugmetom „Poništi · 58 s“
- „23:47 · Srđan · 2 Heineken · 1 Coca-Cola“

Vuletin zbir je „22 kom · 26 doza · 4 bok.“, što je dosljedno S1 plus tura od 23:52.

**Prozor** (~420×556, centriran, surface, radius ~16, scrim preko cijelog ekrana):
- naslov sa zlatnom ikonom katanca: **„Podešavanja“**
- tekst: **„Samo vlasnik mijenja artikle, mjere i radnike. Otključava se čim upišeš četvrtu cifru.“**
- **4 tačke-indikatora PIN-a.** Popunjena tačka je kremasta, prazna je krug sa sivim obrubom. Na slici je upisana 1 cifra.
- numerička tastatura 3×4, dugmad ~114×60:
  - redovi „1 2 3“, „4 5 6“, „7 8 9“
  - posljednji red: prazno polje, „0“, „⌫“ (briše posljednju cifru)
- dugme preko cijele širine (outline, ~56 px): **„Odustani“** zatvara prozor i vraća na S1.

**Ponašanje:** nema dugmeta „Potvrdi“, jer se PIN provjerava automatski čim je unesena posljednja cifra. Tačan PIN vodi na V1 (Artikli). Pogrešan PIN i blokada nisu nacrtani na ovoj stranici. Blokada je na stranici Osnova, ekran T5 (PIN-02).

---

## V1 · Artikli

**Svrha:** lista svih pića sa filterom po kategoriji, redoslijedom pločica na šanku, uključivanjem i isključivanjem i ulazom u izmjenu.

**Radni dio:**
- **Red filtera** (pilule ~48 px):
  - „Sve · 15“, „Pivo“, „Bezalkoholno“, „Žestina“
  - Na slici je izabrana „Žestina“ (kremasta puna pilula, tamni tekst). Ostale su outline.
  - Broj stoji samo uz „Sve“.
- Desno je zlatno dugme **„+ Novi artikal“**, koje otvara V1.1.
- **Lista artikala** (redovi ~60 px). Kolone:
  - ručica ⋮⋮
  - naziv (bold ~17 px), u koloni od ~210 px
  - kategorija (sivo), u koloni od ~130 px
  - čipovi mjera (tamni okvir, bold ~13 px, format „Naziv litre“ bez „l“)
  - prekidač
  - „›“ otvara V1.2 za taj artikal
- Podaci na slici (filter „Žestina“):

  | Naziv | Kategorija | Mjere (čipovi) | Uključen |
  |---|---|---|---|
  | Jack Daniel’s | Žestina | Doza 0,03 | da |
  | Johnnie Walker | Žestina | Doza 0,03 | da |
  | Smirnoff | Žestina | Doza 0,03 | da |
  | Rakija šljiva | Žestina | Doza 0,03 · Bokal 0,25 | da |
  | Rakija dunja | Žestina | Doza 0,03 · Bokal 0,25 | da |
  | Jack Daniel’s 0,7 | Žestina | Boca 0,70 | da |
  | Ballantine’s | Žestina | Doza 0,03 | **ne** (sivo) |

- Tekst ispod liste: **„Drži ručicu i povuci da promijeniš redoslijed pločica na šanku. Isključeno piće se ne nudi na šanku, a stare noći ga i dalje prikazuju.“**

**Prazno stanje** artikala u podešavanjima nije nacrtano. Prazan šank je pokriven ekranom T2 na stranici Osnova.

---

## V1.1 · Novi artikal (prozor)

**Svrha:** dodavanje novog pića. Pozadina je V1, zatamnjena.

**Prozor** (~820×530, radius ~16) ima dvije kolone: formu (~520 px) lijevo i pregled (~210 px) desno.
- Naslov: **„Novi artikal“**
- **Polje „Naziv“:** labela „Naziv“, input ~56 px, upisana vrijednost „Viljamovka“.
- **„Kategorija“:** jednostruki izbor. Čipovi ~48 px:
  - „Pivo“, „Bezalkoholno“, „Žestina“
  - Izabrana je „✓ Žestina“ (zlatni okvir, tamno-zlatna podloga, zlatni tekst i kvačica). Neizabrani su tamni bez okvira.
  - Lista dolazi iz Kategorija (V5). Isključene kategorije, npr. Vino, se ne nude.
- **„Dugmad na pločici · najviše 3“:** višestruki izbor mjera, najviše 3. Čipovi:
  - „Komad“, „✓ Doza 0,03 l“, „✓ Bokal 0,25 l“, „Boca 0,70 l“, „Boca 1,00 l“
  - Izabrane su Doza i Bokal.
  - Format čipa je „Naziv litre l“, s litrama na dva decimalna mjesta.
  - Lista dolazi iz Mjera (V4). Isključena „Čaša vina“ se ne nudi.
- Pomoćni tekst: **„Mjere se uređuju u meniju Mjere. Litre u pregledu se računaju po mjeri.“**
- **Desna kolona, „Na šanku izgleda ovako“:** živi pregled pločice sa nazivom „Viljamovka“ i dugmadima „0,03“ i „Bokal“. Na dugmadima su natpisi mjera iz V4, kolona „Dugme“.
- Dugmad dolje desno:
  - **„Odustani“** (outline) zatvara prozor bez čuvanja.
  - **„✓ Sačuvaj artikal“** (zlatno, ~190×56) čuva artikal i zatvara prozor.

**Validacija nije nacrtana:**
- prazan naziv
- duplikat naziva (pravilo iz plana: „Dva aktivna artikla ne mogu imati isti naziv.“)
- nijedna izabrana mjera
- pokušaj izbora 4. mjere

Na prozoru nema poruka o greškama ni onemogućenog stanja dugmeta „Sačuvaj artikal“. Za te poruke treba vidjeti O6, Forme i validacija.

---

## V1.2 · Izmjena artikla (prozor)

**Svrha:** izmjena postojećeg artikla. Otvara se preko „›“ u V1. Raspored je isti kao V1.1.
- Naslov: **„Izmjena · Rakija šljiva“** (format „Izmjena · {naziv}“).
- Naziv: „Rakija šljiva“. Kategorija: „✓ Žestina“.
- Mjere: „Komad“, „✓ Doza 0,03 l“, „✓ Bokal 0,25 l“, „✓ Boca 0,70 l“, „Boca 1,00 l“. Izabrane su 3, što je maksimum.
- Pregled „Na šanku izgleda ovako“: „Rakija šljiva“ i tri dugmeta „0,03“, „Bokal“, „Boca“.
- Napomena dolje lijevo, pored dugmadi: **„Promjena važi od sljedeće ture. Ove noći već ima 3 bokala, oni ostaju kako su uneseni.“** Broj i mjera su dinamički, a tekst se prikazuje samo kad artikal ima unose u aktivnoj noći.
- Dugmad:
  - **„Odustani“**
  - **„✓ Sačuvaj izmjenu“** (zlatno)
- U prozoru nema dugmeta za isključivanje ni brisanje. Isključivanje ide prekidačem u listi V1.

---

## V2 · Ljudi

**Svrha:** stalni radnici: uloga, uključen/isključen, dodavanje.

**Radni dio:**
- Naslov **„Stalni radnici“** i zlatno dugme **„+ Novi radnik“**. Prozor za novog radnika nije nacrtan.
- Redovi (~76 px):
  - lijevo: ime (bold ~18 px) i opis uloge (sivo)
  - u sredini: **segmentirani prekidač uloge** sa dvije opcije, „Konobar“ i „Za šankom“ (~204×58). Aktivna opcija ima svjetliju podlogu i bijeli tekst.
  - desno: sivi natpis „radi“ i prekidač

  | Ime | Opis | Uloga | radi |
  |---|---|---|---|
  | Vuleta | nosi piće gostima | Konobar | uklj. |
  | Srđan | nosi piće gostima | Konobar | uklj. |
  | Miloš | ne izlazi iz šanka, prodaje piće | Za šankom | uklj. |
  | Smitran | ne izlazi iz šanka, prodaje piće | Za šankom | uklj. |

  Opis se izvodi iz uloge:
  - Konobar → „nosi piće gostima“
  - Za šankom → „ne izlazi iz šanka, prodaje piće“
- **Info kartica** (surface, ikona ljudi):
  - naslov: **„Ispomoć se ne dodaje ovdje“**
  - tekst: **„Radnik upiše ime ispomoći kad otvara noć, bez PIN-a. Ime važi samo te noći i vidi se u istoriji sa oznakom „ispomoć“.“**
- Tekst ispod: **„Dodir na ime otvara izmjenu imena. Isključen radnik se ne nudi pri otvaranju noći, a ranije noći ga i dalje prikazuju. Promjena uloge važi od sljedeće noći.“**
- Prozor za izmjenu imena nije nacrtan.
- Baner na ovom ekranu prikazuje odbrojavanje „1 min 40 s“.

---

## V3 · Promjena PIN-a

**Svrha:** promjena PIN-a vlasnika u 3 koraka.

**Radni dio ima dvije kolone:**
- **Lijevo** (~330 px, počinje na x≈380):
  - naslov **„Promjena PIN-a“** (bold ~28 px)
  - stepper sa 3 koraka:
    1. „Trenutni PIN“: završen korak, zlatni puni krug sa kvačicom, sivi tekst
    2. „Novi PIN“: aktivan korak, krug sa zlatnim okvirom i brojem „2“, bijeli bold tekst
    3. „Ponovi novi PIN“: budući korak, sivi krug sa „3“, sivi tekst
  - pravila (sivo):
    - **„PIN ima 4 cifre i zna ga samo vlasnik. Ne koristi datum rođenja ni 1234.“**
    - **„Zaboravljen PIN niko ne može poništiti. Zapiši ga na sigurno mjesto.“**
- **Desno** (~400 px):
  - natpis caps zlatno: **„KORAK 2 OD 3 · NOVI PIN“** (izvorni tekst „Korak 2 od 3 · Novi PIN“, format „Korak {n} od 3 · {naziv koraka}“)
  - 4 tačke. Na slici su 3 popunjene, 1 prazna.
  - tastatura 3×4, dugmad ~128×60: „1–9“, prazno polje, „0“, „⌫“

**Ponašanje:** nema dugmeta za potvrdu ni za odustajanje. Korak se automatski završava kad se upiše posljednja cifra. Izlaz je preko menija ili dugmeta „Nazad na unos“.

**Nisu nacrtani:**
- pogrešan trenutni PIN
- novi PIN se ne poklapa u koraku 3
- zabranjeni PIN (1234 ili datum rođenja)
- poruka o uspješnoj promjeni

Za te poruke treba vidjeti O4, O6 i T5.

Baner na ovom ekranu prikazuje „1 min 52 s“.

---

## V4 · Mjere

**Svrha:** lista mjera: naziv u pregledu, litre, natpis na dugmetu, upotreba i uključenost.

**Radni dio:**
- Naslov **„Mjere“** i podnaslov **„Svaka mjera je jedno dugme na pločici. Litre se sabiraju u pregledu. Komad nema litre.“**
- Zlatno dugme **„+ Nova mjera“**. Prozor nije nacrtan.
- Zaglavlje tabele (caps, sivo):

  | Kolona | Širina | Poravnanje |
  |---|---|---|
  | „NAZIV U PREGLEDU“ | ~300 px | lijevo |
  | „LITARA“ | ~60 px | desno |
  | „DUGME“ | ~120 px | lijevo |
  | „KORISTE JE“ | ostatak | lijevo |

  Iza kolona su prekidač i „›“.
- Redovi (~64 px):

  | Naziv u pregledu | Litara | Dugme (prikazano kao čip/input) | Koriste je | Uklj. |
  |---|---|---|---|---|
  | Komad | — | Komad | 8 pića | da |
  | Doza | 0,03 | 0,03 | 6 pića | da |
  | Bokal | 0,25 | Bokal | Rakija šljiva, Rakija dunja | da |
  | Boca 0,7 | 0,70 | Boca | Jack Daniel’s 0,7 | da |
  | Boca 1 l | 1,00 | Boca 1 l | nijedno piće | da |
  | Čaša vina | 0,20 | Čaša | nijedno piće | **ne** (sivo) |

- Formati:
  - litre se pišu sa zarezom i dvije decimale; kod komada stoji „—“
  - „Koriste je“ prikazuje imena pića kad ih je malo (1–2), „N pića“ kad ih je više, i „nijedno piće“ kad je 0
- Tekst ispod: **„Promjena litara važi od sljedeće ture. Stare ture čuvaju litre iz trenutka unosa. Mjera koju koristi neko piće ne može se isključiti dok se ne skine sa tog pića.“**
- Prekidač mjere koju koristi aktivno piće je po pravilu blokiran. Vizuelno blokirano stanje nije posebno nacrtano.
- Za razliku od Artikala i Kategorija, ovdje nema ručice za redoslijed.

---

## V5 · Kategorije

**Svrha:** kategorije (filter-dugmad na šanku): redoslijed, uključen/isključen, dodavanje.

**Radni dio:**
- Naslov **„Kategorije“** i podnaslov **„Kategorije su dugmad iznad pića na šanku. Redoslijed ovdje je redoslijed na šanku.“**
- Zlatno dugme **„+ Nova kategorija“**. Prozor nije nacrtan.
- Redovi (~64 px): ručica ⋮⋮, naziv, broj pića (sivo, kolona na x≈968), prekidač, „›“ (preimenovanje; prozor nije nacrtan)

  | Kategorija | Pića | Uklj. |
  |---|---|---|
  | Pivo | 4 pića | da |
  | Bezalkoholno | 4 pića | da |
  | Žestina | 7 pića | da |
  | Vino | nema pića | **ne** (sivo) |

- Format broja: „N pića“, a za 0 piše „nema pića“. U V4 se za 0 piše „nijedno piće“, što nije usklađeno.
- Tekst ispod: **„Isključena kategorija se ne vidi na šanku, a njena pića ostaju u istoriji. Kategorija sa aktivnim pićima ne može se isključiti dok se pića ne premjeste ili isključe.“**

---

## V6 · Pravila rada

**Svrha:** podesivi rokovi, ispomoć i lista razloga poništavanja.

**Radni dio:**
- Naslov **„Pravila rada“** i podnaslov **„Važe od sljedeće radnje. Ture koje su već unesene se ne mijenjaju.“**
- Redovi postavki sa naslovom (bold), opisom (sivo) i kontrolom desno. Stepper ima dugmad „−“ i „+“ (56×56) i vrijednost između njih, broj bold, a jedinica siva.

  | Postavka | Opis | Kontrola | Vrijednost |
  |---|---|---|---|
  | **Poništavanje bez PIN-a** | „Koliko dugo poslije potvrde radnik može sam poništiti turu.“ | stepper | „60 s“ |
  | **Istorija noći** | „Starije zatvorene noći se brišu same.“ | stepper | „30 dana“ |
  | **Zaključavanje podešavanja** | „Poslije koliko vremena bez dodira se podešavanja zaključaju.“ | stepper | „2 min“ |
  | **Ispomoć** | „Radnik na šanku može upisati ime ispomoći pri otvaranju noći.“ | prekidač | uključen |
  | **Razlozi poništavanja** | „Nude se kao dugmad kad vlasnik poništava turu.“ | čipovi | vidi ispod |

- Koraci stepera i granice (min/max) nisu navedeni u dizajnu.
- **Razlozi poništavanja:** tamni čipovi (~48 px) sa „×“ za uklanjanje:
  - „Pogrešan konobar ×“
  - „Pogrešno piće ×“
  - „Pogrešna količina ×“
  - „Uneseno dvaput ×“
  - „Vraćeno ×“
  - zatim isprekidani zlatni čip **„+ Razlog“** za dodavanje. Unos novog razloga nije nacrtan.

---

## V7 · O aplikaciji

**Svrha:** informacije i kontakt za pomoć, samo za čitanje.

**Radni dio:**
- Naslov **„O aplikaciji“** i podnaslov **„Evidencija pića za Caffe Bar 919.“**
- Tabela ključ–vrijednost. Ključ je siv (kolona ~216 px), vrijednost bold, redovi ~56 px:

  | Ključ | Vrijednost |
  |---|---|
  | Verzija | 1.0 |
  | Podaci | samo na ovom tabletu, bez interneta |
  | Istorija | 30 dana · podešava se u Pravilima rada |
  | Izradio | NextPixel · www.nextpixel.dev |

  Vrijednost „30 dana“ je dinamička i dolazi iz V6.
- **Kontakt kartica** (surface, radius ~16, ikona telefona u zlatnom krugu):
  - mali tekst: **„Pomoć oko aplikacije · NextPixel, Nikola“**
  - veliki broj (bold ~28 px): **„+387 66 603 900“**
  - tekst: **„Za greške u radu aplikacije i kad se evidencija ne otvara (BAZA-01). Zaboravljen PIN se ne može poništiti.“**
- Nema interaktivnih elemenata osim menija i ljuske. Da li je broj klikabilan nije označeno.

---

## Nedosljednosti i rupe (V-stranica)

1. **Broj cifara PIN-a:** dizajn je svuda pravljen za 4 cifre, a korisnik je odlučio da PIN ima 6. Mjesta koja treba promijeniti:
   - V0: tekst „Otključava se čim upišeš četvrtu cifru.“ i 4 tačke
   - V3: tekst „PIN ima 4 cifre …“, primjer „Ne koristi … 1234“ i 4 tačke
   - isto važi za S1.3 i T5 na drugim stranicama
   Za 6 cifara treba „šestu cifru“, 6 tačaka i drugi primjer slabog PIN-a (npr. 123456).
2. **Blokada poslije 10 pogrešnih PIN-ova na 5 minuta:** na V0 i V3 nema stanja greške ni blokade. Plan pravilo ima, a crta ga samo T5 (PIN-02) na stranici Osnova. Nije nacrtano ni kako se pogrešan „Trenutni PIN“ u V3 broji u blokadu.
3. **Automatsko zaključavanje (2 min), poništavanje bez PIN-a (60 s) i istorija (30 dana)** se slažu sa planom, u V6, banneru i V7.
4. **Kontakt u V7** se slaže sa planom: „NextPixel, Nikola“, „+387 66 603 900“, „www.nextpixel.dev“. Pravno ime firme se ne pojavljuje, pa ga ne treba ispravljati.
5. **Razlog „Vraćeno“** u V6 je u napetosti sa planom: „Vraćena pića ne evidentiraju se u aplikaciji, nego se rješavaju na kraju noći.“ Treba potvrditi da li „Vraćeno“ ostaje kao razlog poništavanja.
6. **Format mjera nije ujednačen:**
   - u V1 čip piše „Doza 0,03“ i „Boca 0,70“, bez „l“
   - u V1.1/V1.2 čip piše „Doza 0,03 l“ i „Boca 0,70 l“
   - na pločici u S1 piše „doza 0,03 l“ i „boca 0,70 l“, malim slovom
   - u V4 naziv glasi „Boca 0,7“, a litre „0,70“
   Treba odrediti jedno pravilo formatiranja.
7. **Broj 0 nije ujednačen:** u V5 piše „nema pića“, u V4 „nijedno piće“.
8. **Brojači u meniju:**
   - „3 kategorije“, a V5 prikazuje 4 (Vino je isključen). Ako se broje samo aktivne, to je u redu, ali onda „15 pića“ i „6 mjera“ treba brojati istom logikom.
   - „6 mjera u litrima“ obuhvata isključenu „Čašu vina“ i „Komad“ bez litara. To nije dosljedno sa „3 kategorije“.
9. **Kategorija „Vino“ i mjera „Čaša vina“** ne postoje u početnoj listi iz plana. To su primjeri isključenih stavki, a plan kaže da Marinko sam unosi stvarna pića.
10. **Nedostaju prozori i stanja:**
    - nova i izmijenjena mjera
    - nova kategorija i preimenovanje
    - novi radnik i izmjena imena
    - unos novog razloga
    - validacija V1.1 (prazan ili dupli naziv, 0 ili 4 mjere)
    - blokirani prekidač mjere ili kategorije koja je u upotrebi
    - greške koraka 1 i 3 u V3 i poruka o uspješnoj promjeni PIN-a
    - granice stepera u V6
11. **V0 nema dugme „Potvrdi“:** PIN se provjerava automatski na posljednjoj cifri. Kod 6 cifara to treba zadržati ili dodati dugme.
12. **Ponašanje dugmeta „Nazad na unos“** nije definisano: zaključava li odmah ili ostavlja podešavanja otključana do isteka roka.
