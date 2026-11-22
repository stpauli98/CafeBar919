package dev.nextpixel.cafebar919.domain

import java.time.ZoneId
import java.time.LocalDateTime
import java.time.LocalDate
import org.junit.Test
import org.junit.Assert.assertEquals

class FormatTest {
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
        assertEquals("1,00", Format.litre(1.0))
    }

    @Test fun oznakaMjere_nazivBezKolicineIzNazivaPaLitreIzMjere() {
        assertEquals("Doza 0,03", Format.oznakaMjere("Doza", 0.03, false))
        assertEquals("Boca 0,70 l", Format.oznakaMjere("Boca 0,7", 0.70, true))
        assertEquals("Boca 1,00 l", Format.oznakaMjere("Boca 1 l", 1.0, true))
        assertEquals("Komad", Format.oznakaMjere("Komad", null, true))
        assertEquals("Čaša vina 0,20 l", Format.oznakaMjere("Čaša vina", 0.2, true))
        assertEquals("Boca 0,70", Format.oznakaMjere("Boca 0.7 l", 0.70, false))
    }

    @Test fun zbirMjera_kratkoImePaBroj_poRedoslijedu_bezNula() {
        val s = listOf(Format.MjeraBroj("bok.", 2, 4), Format.MjeraBroj("kom", 0, 22), Format.MjeraBroj("doza", 1, 26), Format.MjeraBroj("boca 1 l", 4, 0))
        assertEquals("kom 22 · doza 26 · bok. 4", Format.zbirMjera(s))
        assertEquals("još ništa", Format.zbirMjera(emptyList()))
    }

    @Test fun zbirMjera_sabiraIsteUZbiru() {
        val s = listOf(Format.MjeraBroj("bok.", 2, 3), Format.MjeraBroj("bok.", 2, 1), Format.MjeraBroj("kom", 0, 1))
        assertEquals("kom 1 · bok. 4", Format.zbirMjera(s))
    }

    @Test fun odbrojavanje() {
        assertEquals("2 min", Format.odbrojavanje(120)); assertEquals("1 min 40 s", Format.odbrojavanje(100)); assertEquals("45 s", Format.odbrojavanje(45))
        assertEquals("0 s", Format.odbrojavanje(0))
    }

    @Test fun brisanjeNoci_cijelaRecenicaSlazeSeSaBrojem() {
        assertEquals("1 noć starija od 19 dana biće obrisana pri sljedećem pokretanju.", Format.brisanjeNoci(1, 19))
        assertEquals("2 noći starije od 19 dana biće obrisane pri sljedećem pokretanju.", Format.brisanjeNoci(2, 19))
        assertEquals("4 noći starije od 19 dana biće obrisane pri sljedećem pokretanju.", Format.brisanjeNoci(4, 19))
        // Množina prema spec 7 #33.
        assertEquals("5 noći starijih od 19 dana biće obrisano pri sljedećem pokretanju.", Format.brisanjeNoci(5, 19))
        assertEquals("11 noći starijih od 19 dana biće obrisano pri sljedećem pokretanju.", Format.brisanjeNoci(11, 19))
        assertEquals("21 noć starija od 19 dana biće obrisana pri sljedećem pokretanju.", Format.brisanjeNoci(21, 19))
    }

    @Test fun datumNoci() { assertEquals("Subota 21. 11.", Format.datumNoci(LocalDate.of(2026, 11, 21))) }

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
}
