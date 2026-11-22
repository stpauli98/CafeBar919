package dev.nextpixel.cafebar919.domain

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
    POSLJEDNJI_RAZLOG,                           // poništavanje traži izabran razlog: posljednji aktivan se ne uklanja
    VRIJEDNOST_VAN_OPSEGA                        // stepper V6
}
