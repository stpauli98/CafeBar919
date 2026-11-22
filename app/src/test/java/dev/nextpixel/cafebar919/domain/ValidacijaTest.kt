package dev.nextpixel.cafebar919.domain

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class ValidacijaTest {
    @Test fun parsirajLitre_prihvataTackuIZarez() {
        assertEquals(0.70, Validacija.parsirajLitre("0,7")!!, 1e-9); assertEquals(0.70, Validacija.parsirajLitre("0.7")!!, 1e-9)
        assertEquals(0.71, Validacija.parsirajLitre("0,705")!!, 1e-9); assertNull(Validacija.parsirajLitre("  "))
        assertTrue(Validacija.parsirajLitre("abc")!!.isNaN())
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(0.0))
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(5.01))
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(Double.NaN))
        assertEquals(Ishod.Ok, Validacija.provjeriLitre(0.01)); assertEquals(Ishod.Ok, Validacija.provjeriLitre(5.0))
        assertEquals(Ishod.Ok, Validacija.provjeriLitre(null))
    }

    @Test fun litre_beskonacnoSeOdbija_bezIzuzetka() {
        listOf("1e400", "-1e400", "9".repeat(400)).forEach { unos ->
            val l = Validacija.parsirajLitre(unos)!!
            assertEquals(unos, Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(l))
        }
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(Double.POSITIVE_INFINITY))
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), Validacija.provjeriLitre(Double.NEGATIVE_INFINITY))
    }

    @Test fun naziv() {
        assertEquals("Jack Daniel’s", Validacija.ocisti("  Jack   Daniel’s "))
        assertEquals(Validacija.kljucNaziva("JACK daniel’s"), Validacija.kljucNaziva(" Jack Daniel’s"))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PRAZAN), Validacija.provjeriNaziv("   ", 24))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PREDUG), Validacija.provjeriNaziv("x".repeat(25), 24))
        assertEquals(Ishod.Ok, Validacija.provjeriNaziv("x".repeat(24), 24))
    }


    @Test fun provjeriIspomoc_odbijaPraznoSankIZauzeto() {
        val zauzeta = listOf("Vuleta", "Srđan")
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PRAZAN), Validacija.provjeriIspomoc("   ", zauzeta))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), Validacija.provjeriIspomoc(" vuleta ", zauzeta))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), Validacija.provjeriIspomoc("šank", zauzeta))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_PREDUG), Validacija.provjeriIspomoc("A".repeat(21), zauzeta))
        assertEquals(Ishod.Ok, Validacija.provjeriIspomoc("Dejan M.", zauzeta))
    }
}
