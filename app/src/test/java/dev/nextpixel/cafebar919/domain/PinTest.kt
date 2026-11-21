package dev.nextpixel.cafebar919.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinTest {
    @Test fun hesIProvjera() {
        val (h, s) = Pin.hesiraj("123456")
        assertTrue(Pin.provjeri("123456", h, s))
        assertFalse(Pin.provjeri("123457", h, s))
    }

    @Test fun zabranjeni() {
        assertTrue(Pin.zabranjen("000000"))
        assertTrue(Pin.zabranjen("123456"))
        assertTrue(Pin.zabranjen("654321"))
        assertFalse(Pin.zabranjen("482915"))
    }
}
