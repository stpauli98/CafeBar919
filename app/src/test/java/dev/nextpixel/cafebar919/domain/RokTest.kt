package dev.nextpixel.cafebar919.domain

import org.junit.Assert.*
import org.junit.Test

class RokTest {
    @Test fun preostalo_zaokruzujeNavise() {
        assertEquals(60, Rok.preostaloSekundi(1_000_000, 1_000_000, 60))
        assertEquals(41, Rok.preostaloSekundi(1_000_000, 1_019_500, 60))
        assertEquals(1, Rok.preostaloSekundi(1_000_000, 1_059_999, 60))
        assertEquals(0, Rok.preostaloSekundi(1_000_000, 1_060_000, 60))
    }

    @Test fun preostalo_rokNulaNikad() = assertEquals(0, Rok.preostaloSekundi(1_000_000, 1_000_000, 0))

    @Test fun preostalo_satUnazadNemaRoka() = assertEquals(0, Rok.preostaloSekundi(1_000_000, 999_000, 60))
}
