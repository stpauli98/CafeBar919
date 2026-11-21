package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.CafeBarApp
import dev.nextpixel.cafebar919.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PodesavanjaSesijaTest {
    @Test fun sesija_odbrojavaIZakljucava() = runTest {
        val s = PodesavanjaSesija(backgroundScope, { 120 }); s.otkljucaj()
        advanceTimeBy(20_000); runCurrent(); assertEquals(100, s.preostaloS.value)
        s.dodir(); assertEquals(120, s.preostaloS.value)
        advanceTimeBy(120_000); runCurrent(); assertFalse(s.otkljucano.value)
    }

    @Test fun sesija_dodirNeOtkljucavaZakljucanu() = runTest {
        val s = PodesavanjaSesija(backgroundScope, { 120 })
        s.dodir()
        assertFalse(s.otkljucano.value)
    }

    @Test fun sesija_zakljucajZaustavljaOdbrojavanje() = runTest {
        val s = PodesavanjaSesija(backgroundScope, { 120 }); s.otkljucaj()
        advanceTimeBy(5_000); runCurrent()
        s.zakljucaj()
        assertFalse(s.otkljucano.value)
        val ostalo = s.preostaloS.value
        advanceTimeBy(10_000); runCurrent()
        assertEquals(ostalo, s.preostaloS.value)
    }

    @Test fun sesija_zakljucavaKadAplikacijaOdeUPozadinu() {
        val sesija = ApplicationProvider.getApplicationContext<CafeBarApp>().container.sesija
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            sesija.otkljucaj()
            assertTrue(sesija.otkljucano.value)
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertFalse(sesija.otkljucano.value)
        }
    }
}
