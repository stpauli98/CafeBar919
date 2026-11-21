package dev.nextpixel.cafebar919

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Testni sat koji se pomjera ručno; trenutak je na milisekundi jer baza čuva milisekunde. */
class MutableClock(start: Instant, private val zona: ZoneId = ZoneId.systemDefault()) : Clock() {
    private var sada: Instant = start.truncatedTo(ChronoUnit.MILLIS)

    fun pomjeri(d: Duration) { sada = sada.plus(d).truncatedTo(ChronoUnit.MILLIS) }

    override fun getZone(): ZoneId = zona
    override fun withZone(zone: ZoneId): Clock = MutableClock(sada, zone)
    override fun instant(): Instant = sada
}
