package dev.nextpixel.cafebar919.e2e

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.nextpixel.cafebar919.MainActivity
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * BAZA-01 (spec): kad se baza ne može otvoriti, aplikacija prikazuje grešku i NIKAD ne pravi novu bazu preko stare.
 * Fajl baze se ovdje namjerno kvari prije pokretanja aplikacije, kao poslije kvara memorije.
 */
@RunWith(AndroidJUnit4::class)
class BazaE2ETest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun osteceniFajlBaze_BAZA01_iFajlOstajeNetaknut() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val fajl = ctx.getDatabasePath("cafebar919.db")
        fajl.parentFile!!.mkdirs()
        val smece = ByteArray(8192) { (it * 31 + 7).toByte() }
        fajl.writeBytes(smece)

        ActivityScenario.launch(MainActivity::class.java).use {
            val vidiGresku = runCatching {
                compose.waitUntil(15_000) {
                    compose.onAllNodesWithText("BAZA-01", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                        compose.onAllNodesWithText("Ko radi večeras?").fetchSemanticsNodes().isNotEmpty()
                }
            }.isSuccess && compose.onAllNodesWithText("BAZA-01", substring = true).fetchSemanticsNodes().isNotEmpty()

            val novaBaza = compose.onAllNodesWithText("Ko radi večeras?").fetchSemanticsNodes().isNotEmpty()
            assertTrue(
                "Oštećena baza nije dala BAZA-01" + if (novaBaza) ": aplikacija je napravila novu, praznu bazu (S0 „Ko radi večeras?“)" else "",
                vidiGresku,
            )
            assertTrue("Fajl baze je obrisan", fajl.exists())
            assertArrayEquals("Fajl baze je prepisan", smece, fajl.readBytes().copyOf(smece.size))
        }
    }
}
