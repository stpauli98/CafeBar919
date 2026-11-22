package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.CafeBarApp
import dev.nextpixel.cafebar919.MainActivity
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class PodesavanjaNavTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()

    @Test fun tacanPinOtvaraArtikle_zakljucavanjeVracaNaPocetni() {
        val sesija = ApplicationProvider.getApplicationContext<CafeBarApp>().container.sesija
        compose.waitUntil(10_000) { postoji("Podešavanja") }
        compose.onNodeWithText("Podešavanja").performClick()
        compose.waitUntil(5_000) { postoji("Odustani") }
        "123456".forEach { compose.onNodeWithText(it.toString()).performClick() }
        compose.waitUntil(10_000) { postoji("Nazad na unos") }
        assertTrue(sesija.otkljucano.value)
        compose.onNodeWithText("Otključano za vlasnika.").assertExists()

        compose.runOnIdle { sesija.zakljucaj() }
        compose.waitUntil(5_000) { !postoji("Nazad na unos") }
        compose.onNodeWithText("Unos tura dolazi u sljedećoj fazi izrade.").assertExists()
    }
}
