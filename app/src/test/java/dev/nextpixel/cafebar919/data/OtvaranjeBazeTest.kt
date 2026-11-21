package dev.nextpixel.cafebar919.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OtvaranjeBazeTest {
    @Test fun greskaPriOtvaranju_dajeGreska_paPonovoSpremna() = runTest {
        var puta = 0
        val o = OtvaranjeBaze { if (puta++ == 0) throw IllegalStateException("x") else AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null) }
        o.pokusaj(); assertTrue(o.stanje.value is StanjeBaze.Greska)
        o.pokusaj(); assertTrue(o.stanje.value is StanjeBaze.Spremna)
        (o.stanje.value as StanjeBaze.Spremna).db.close()
    }
}
