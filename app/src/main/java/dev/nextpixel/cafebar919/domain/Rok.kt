package dev.nextpixel.cafebar919.domain

object Rok {
    /** Preostale sekundi roka za poništavanje, zaokruženo naviše; 0 kad je rok istekao ili sat ide unazad. */
    fun preostaloSekundi(potvrdjenaUMs: Long, sadaMs: Long, rokS: Int): Int {
        val proteklo = sadaMs - potvrdjenaUMs
        val rokMs = rokS * 1000L
        if (proteklo < 0 || proteklo >= rokMs) return 0
        return ((rokMs - proteklo + 999) / 1000).toInt()
    }
}
