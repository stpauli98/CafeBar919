package dev.nextpixel.cafebar919.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {
    private val DANI = listOf("Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja")

    /** Samo oblik riječi po srpskom pravilu množine. */
    fun mnozina(n: Int, jedan: String, dva: String, pet: String): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m10 == 1 && m100 != 11 -> jedan
            m10 in 2..4 && m100 !in 12..14 -> dva
            else -> pet
        }
    }

    fun brojac(n: Int, jedan: String, dva: String, pet: String): String = "$n ${mnozina(n, jedan, dva, pet)}"

    /** Potvrda smanjenja istorije (spec 7 #33); imenica, pridjev i glagol slažu se sa brojem noći. */
    fun brisanjeNoci(noci: Int, dana: Int): String {
        val pridjev = mnozina(noci, "starija", "starije", "starijih")
        val glagol = mnozina(noci, "obrisana", "obrisane", "obrisano")
        return "${brojac(noci, "noć", "noći", "noći")} $pridjev od $dana dana biće $glagol pri sljedećem pokretanju."
    }

    fun litre(l: Double): String =
        BigDecimal.valueOf(l).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')

    fun litreSaJedinicom(l: Double): String = "${litre(l)} l"

    private val KOLICINA_NA_KRAJU = Regex("""\s+\d+([.,]\d+)?\s*l?$""")

    /** Oznaka mjere na čipu (spec 7 #13): „Doza 0,03“ u listi, „Boca 0,70 l“ u prozoru; mjera bez litara samo naziv. */
    fun oznakaMjere(naziv: String, litara: Double?, saJedinicom: Boolean): String {
        if (litara == null) return naziv
        val osnova = naziv.replace(KOLICINA_NA_KRAJU, "")
        return "$osnova ${litre(litara)}" + if (saJedinicom) " l" else ""
    }

    data class MjeraBroj(val uZbiru: String, val redoslijed: Int, val kolicina: Int)

    fun zbirMjera(stavke: List<MjeraBroj>): String {
        val grupe = stavke.groupBy { it.uZbiru }
            .map { (naziv, lista) -> Triple(naziv, lista.minOf { it.redoslijed }, lista.sumOf { it.kolicina }) }
            .filter { it.third != 0 }
            .sortedBy { it.second }
        if (grupe.isEmpty()) return "još ništa"
        return grupe.joinToString(" · ") { "${it.first} ${it.third}" }
    }

    fun odbrojavanje(sekundi: Int): String {
        val min = sekundi / 60
        val s = sekundi % 60
        return when {
            min == 0 -> "$s s"
            s == 0 -> "$min min"
            else -> "$min min $s s"
        }
    }

    fun datumNoci(d: LocalDate): String =
        "${DANI[d.dayOfWeek.value - 1]} ${d.dayOfMonth}. ${d.monthValue}."

    private val SR = Locale.forLanguageTag("sr")

    private fun osnova(naziv: String): String = naziv.replace(KOLICINA_NA_KRAJU, "").lowercase(SR)

    /** Mjera na pločici (spec 7 #13): „komad“, „doza 0,03 l“, „boca 0,70 l“. */
    fun mjeraNaPlocici(naziv: String, litara: Double?): String =
        if (litara == null) naziv.lowercase(SR) else "${osnova(naziv)} ${litreSaJedinicom(litara)}"

    /** Mjera u stavci ture: „komad“, „doza · 0,06 l“. */
    fun mjeraUStavci(naziv: String, litara: Double?, kolicina: Int): String =
        if (litara == null) naziv.lowercase(SR)
        else "${osnova(naziv)} · ${litreSaJedinicom(BigDecimal.valueOf(litara).multiply(BigDecimal(kolicina)).toDouble())}"

    fun natpisUTuri(natpis: String, kolicina: Int): String = "$natpis · $kolicina"

    data class StavkaSazetka(val naziv: String, val uZbiru: String, val litara: Double?, val kolicina: Int)

    /** Sažetak ture (spec 7 #12): „Jelen pivo 4 · Jack Daniel’s doza 2“; piće bez litara bez oznake mjere. */
    fun sazetakTure(stavke: List<StavkaSazetka>): String = stavke.joinToString(" · ") {
        if (it.litara == null) "${it.naziv} ${it.kolicina}" else "${it.naziv} ${it.uZbiru} ${it.kolicina}"
    }

    fun litreKategorija(l: List<Obracun.KategorijaLitre>): String =
        l.joinToString(" · ") { "${it.naziv} ${litreSaJedinicom(it.litara)}" }

    fun vrijeme(ms: Long, zona: ZoneId): String =
        DateTimeFormatter.ofPattern("HH:mm").format(Instant.ofEpochMilli(ms).atZone(zona))

    fun datumMalim(d: LocalDate): String =
        "${DANI[d.dayOfWeek.value - 1].lowercase(SR)} ${d.dayOfMonth}. ${d.monthValue}."
}
