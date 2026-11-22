package dev.nextpixel.cafebar919.domain

import java.text.Normalizer
import java.util.Locale

object Pretraga {
    private val SR = Locale.forLanguageTag("sr")
    private val OZNAKE = Regex("\\p{Mn}+")
    private val RAZDVAJANJE = Regex("[\\s\\-–’'.,/]+")

    fun normalizuj(s: String): String =
        Normalizer.normalize(s.lowercase(SR).replace('đ', 'd'), Normalizer.Form.NFD).replace(OZNAKE, "")

    /** Svaka riječ upita mora biti početak neke riječi naziva; prazan upit odgovara svemu. */
    fun odgovara(naziv: String, upit: String): Boolean {
        val rijeciUpita = normalizuj(upit).split(RAZDVAJANJE).filter { it.isNotEmpty() }
        if (rijeciUpita.isEmpty()) return true
        val rijeciNaziva = normalizuj(naziv).split(RAZDVAJANJE).filter { it.isNotEmpty() }
        return rijeciUpita.all { r -> rijeciNaziva.any { it.startsWith(r) } }
    }
}
