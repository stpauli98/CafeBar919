package dev.nextpixel.cafebar919.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object Validacija {
    const val NAZIV_ARTIKLA_MAX = 24
    const val BROJAC_OD = 20
    const val NAZIV_KATEGORIJE_MAX = 16
    const val NAZIV_MJERE_MAX = 16
    const val NATPIS_MAX = 8
    const val U_ZBIRU_MAX = 8
    const val IME_MAX = 20
    const val RAZLOG_MAX = 32

    private val RAZMACI = Regex("\\s+")

    fun ocisti(naziv: String): String = naziv.trim().replace(RAZMACI, " ")

    fun kljucNaziva(naziv: String): String = ocisti(naziv).lowercase(Locale.forLanguageTag("sr"))

    /** "0,7"/"0.7" -> 0.70; prazno -> null; nevažeće -> NaN. */
    fun parsirajLitre(tekst: String): Double? {
        val t = tekst.trim()
        if (t.isEmpty()) return null
        val broj = t.replace(',', '.').toBigDecimalOrNull() ?: return Double.NaN
        return broj.setScale(2, RoundingMode.HALF_UP).toDouble().takeIf { it.isFinite() } ?: Double.NaN
    }

    fun provjeriLitre(l: Double?): Ishod {
        if (l == null) return Ishod.Ok
        if (!l.isFinite()) return Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA)
        val v = BigDecimal.valueOf(l)
        return if (v >= BigDecimal("0.01") && v <= BigDecimal("5.00")) Ishod.Ok
        else Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA)
    }

    fun provjeriNaziv(naziv: String, max: Int): Ishod {
        val c = ocisti(naziv)
        return when {
            c.isEmpty() -> Ishod.Odbijeno(Odbijanje.NAZIV_PRAZAN)
            c.length > max -> Ishod.Odbijeno(Odbijanje.NAZIV_PREDUG)
            else -> Ishod.Ok
        }
    }
}
