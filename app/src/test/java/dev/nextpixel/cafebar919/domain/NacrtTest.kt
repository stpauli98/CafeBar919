package dev.nextpixel.cafebar919.domain

import org.junit.Assert.*
import org.junit.Test

class NacrtTest {
    @Test fun dodaj_novaStavkaNaKrajIstaSePovecava() {
        val a = Nacrt.dodaj(Nacrt.dodaj(emptyList(), 1, 10), 2, 20)
        assertEquals(listOf(StavkaNacrta(1, 10, 1), StavkaNacrta(2, 20, 1)), a)
        assertEquals(listOf(StavkaNacrta(1, 10, 2), StavkaNacrta(2, 20, 1)), Nacrt.dodaj(a, 1, 10))
    }

    @Test fun promijeni_minusNaJedanUklanja() =
        assertEquals(emptyList<StavkaNacrta>(), Nacrt.promijeni(listOf(StavkaNacrta(1, 10, 1)), 1, 10, -1))

    @Test fun kolicina_najvise99() {
        val s = listOf(StavkaNacrta(1, 10, 99))
        assertEquals(s, Nacrt.dodaj(s, 1, 10)); assertEquals(s, Nacrt.promijeni(s, 1, 10, +1))
    }

    @Test fun kolicina_nemaStavkeJeNula() = assertEquals(0, Nacrt.kolicina(emptyList(), 1, 10))

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
}
