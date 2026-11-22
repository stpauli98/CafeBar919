package dev.nextpixel.cafebar919.domain

import org.junit.Assert.*
import org.junit.Test

class ObracunTest {
    // brojevi iz inventara E (Vuleta: kom 22, doza 26, bok. 4; žestina 1,78 l)
    @Test fun vuleta_zbirILitre() {
        val zest = { m: String, r: Int, l: Double, k: Int -> Obracun.StavkaObracuna(2, "Žestina", 1, m, r, l, k) }
        val s = listOf(
            Obracun.StavkaObracuna(1, "Pivo", 0, "kom", 0, null, 22),
            zest("doza", 1, 0.03, 26), zest("bok.", 2, 0.25, 4),
        )
        assertEquals("kom 22 · doza 26 · bok. 4", Obracun.zbirMjera(s))
        assertEquals(listOf(Obracun.KategorijaLitre("Žestina", 1.78)), Obracun.litrePoKategoriji(s))
    }

    @Test fun litre_kategorijeRedoslijedomBezNula() {
        val s = listOf(
            Obracun.StavkaObracuna(3, "Vino", 2, "čaša", 0, 0.2, 1),
            Obracun.StavkaObracuna(2, "Žestina", 1, "doza", 1, 0.03, 10),
            Obracun.StavkaObracuna(1, "Pivo", 0, "kom", 0, null, 5),
        )
        assertEquals(listOf(Obracun.KategorijaLitre("Žestina", 0.30), Obracun.KategorijaLitre("Vino", 0.20)), Obracun.litrePoKategoriji(s))
    }
}
