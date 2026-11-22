package dev.nextpixel.cafebar919.domain

import org.junit.Assert.*
import org.junit.Test

class PretragaTest {
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

    @Test fun odgovara_upitSaRazdvajacima() {
        assertTrue(Pretraga.odgovara("Coca-Cola", "coca-cola"))
        assertTrue(Pretraga.odgovara("Jack Daniel’s", "daniel's"))
        assertTrue(Pretraga.odgovara("Jack Daniel’s", "daniel’s"))
        assertFalse(Pretraga.odgovara("Jack Daniel’s", "jack-x"))
    }
}
