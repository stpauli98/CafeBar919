package dev.nextpixel.cafebar919.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProstorTest {
    @Test fun nizakIspod200MB() {
        val s = ProstorStanje { 199L * 1024 * 1024 }
        s.provjeri()
        assertTrue(s.nizak.value)
        s.razumijem()
        assertFalse(s.nizak.value)
    }

    @Test fun dovoljnoProstora() {
        val s = ProstorStanje { 500L * 1024 * 1024 }
        s.provjeri()
        assertFalse(s.nizak.value)
    }

    @Test fun tacno200MB_nijeNizak() {
        val s = ProstorStanje { 200L * 1024 * 1024 }
        s.provjeri()
        assertFalse(s.nizak.value)
    }

    @Test fun izuzetakZnaciNijeNizak() {
        val s = ProstorStanje { throw IllegalStateException("StatFs") }
        s.provjeri()
        assertFalse(s.nizak.value)
    }

    @Test fun razumijemVracaBanerTekPriSljedecojProvjeri() {
        val s = ProstorStanje { 1L }
        s.provjeri(); s.razumijem()
        assertFalse(s.nizak.value)
        s.provjeri()
        assertTrue(s.nizak.value)
    }
}
