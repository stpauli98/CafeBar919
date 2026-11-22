package dev.nextpixel.cafebar919.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** BAZA-01: oštećen fajl baze daje grešku, a fajl se nikad ne briše niti prepisuje novom bazom. */
@RunWith(RobolectricTestRunner::class)
class OstecenaBazaTest {
    private val ctx = ApplicationProvider.getApplicationContext<Context>()

    @Test fun ostecenFajl_dajeGreskuIFajlOstajeNetaknut() = runBlocking {
        val ime = "ostecena.db"
        val fajl = ctx.getDatabasePath(ime)
        fajl.parentFile!!.mkdirs()
        val smece = ByteArray(8192) { (it * 31 + 7).toByte() }
        fajl.writeBytes(smece)

        val otvaranje = OtvaranjeBaze { AppDatabase.napravi(ctx, ime) }
        otvaranje.pokusaj()

        assertTrue("Očekivana greška, a stanje je ${otvaranje.stanje.value}", otvaranje.stanje.value is StanjeBaze.Greska)
        assertTrue("Fajl baze je obrisan", fajl.exists())
        assertArrayEquals("Fajl baze je prepisan", smece, fajl.readBytes().copyOf(smece.size))

        // „Pokušaj ponovo“ takođe ne smije napraviti novu bazu.
        otvaranje.pokusaj()
        assertTrue(otvaranje.stanje.value is StanjeBaze.Greska)
        assertArrayEquals(smece, fajl.readBytes().copyOf(smece.size))
    }
}
