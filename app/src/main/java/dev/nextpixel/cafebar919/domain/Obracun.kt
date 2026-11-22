package dev.nextpixel.cafebar919.domain

import java.math.BigDecimal

object Obracun {
    data class StavkaObracuna(
        val kategorijaId: Long,
        val kategorijaNaziv: String,
        val kategorijaRedoslijed: Int,
        val uZbiru: String,
        val mjeraRedoslijed: Int,
        val litara: Double?,
        val kolicina: Int,
    )

    data class KategorijaLitre(val naziv: String, val litara: Double)

    fun zbirMjera(s: List<StavkaObracuna>): String =
        Format.zbirMjera(s.map { Format.MjeraBroj(it.uZbiru, it.mjeraRedoslijed, it.kolicina) })

    /** Litre po kategoriji (BigDecimal da zbir bude tačan); kategorije bez litara i sa zbirom 0 se izostavljaju. */
    fun litrePoKategoriji(s: List<StavkaObracuna>): List<KategorijaLitre> =
        s.filter { it.litara != null }
            .groupBy { it.kategorijaId }
            .mapNotNull { (_, lista) ->
                val zbir = lista.fold(BigDecimal.ZERO) { a, st -> a + BigDecimal.valueOf(st.litara!!) * BigDecimal(st.kolicina) }
                if (zbir.signum() == 0) null
                else Triple(lista.first().kategorijaRedoslijed, lista.first().kategorijaId, KategorijaLitre(lista.first().kategorijaNaziv, zbir.toDouble()))
            }
            .sortedWith(compareBy({ it.first }, { it.second }))
            .map { it.third }
}
