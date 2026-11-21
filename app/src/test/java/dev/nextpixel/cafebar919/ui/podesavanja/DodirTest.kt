package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.nextpixel.cafebar919.repo.BrojaciMenija
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Spec 6.3: svaki dodir u podešavanjima vraća odbrojavanje, i u prozorima (poseban prozor sistema)
 * i pri kucanju. Odbrojavanje je zaustavljeno (test dispatcher), a granica se mijenja između
 * otključavanja i dodira: ako se vrati na novu granicu, dodir je stigao do sesije.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class DodirTest {
    @get:Rule val compose = createComposeRule()

    private var granica = 120
    private val sesija = PodesavanjaSesija(TestScope(StandardTestDispatcher()), { granica })

    private fun prikazi(sadrzaj: @androidx.compose.runtime.Composable () -> Unit) {
        sesija.otkljucaj()
        granica = 300
        compose.setContent {
            CafeBarTema { Ljuska(Sekcija.ARTIKLI, {}, {}, sesija, BrojaciMenija(0, 0, 0, 0), probniPin = false) { sadrzaj() } }
        }
        compose.waitForIdle()
        assertEquals(120, sesija.preostaloS.value)
    }

    @Test fun dodirUProzoruVracaOdbrojavanje() {
        prikazi {
            Prozor("Probni prozor", onZatvori = {}, sirina = 400.dp, sadrzaj = {}, dugmad = { SporednoDugme("Unutra", {}) })
        }
        compose.onNodeWithText("Unutra").performClick()
        compose.waitForIdle()
        assertEquals(300, sesija.preostaloS.value)
    }

    @Test fun kucanjeUPoljuVracaOdbrojavanje() {
        prikazi {
            var t by mutableStateOf("")
            Polje("Naziv", t, { t = it })
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("a")
        compose.waitForIdle()
        assertEquals(300, sesija.preostaloS.value)
    }
}
