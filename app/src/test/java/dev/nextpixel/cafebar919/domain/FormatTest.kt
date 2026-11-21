package dev.nextpixel.cafebar919.domain

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

    @Test fun datumNoci() { assertEquals("Subota 21. 11.", Format.datumNoci(LocalDate.of(2026, 11, 21))) }
}
