package dev.nextpixel.cafebar919.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.pocetni.PocetniEkran
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TemaTest {
    @get:Rule val compose = createComposeRule()

    @Test fun tokeniOdgovarajuPaperu() {
        assertEquals(Color(0xFF191919), Boje.bg)
        assertEquals(Color(0xFFD3B574), Boje.primary)
        assertEquals(Color(0xFFF5F1E6), Boje.ink)
        assertEquals(56.dp, Razmaci.dodir)
        assertEquals(16.sp, Tekst.body.fontSize)
    }

    @Test fun pocetniEkranPrikazujePodesavanjaIDatum() {
        compose.setContent {
            CafeBarTema { PocetniEkran(danas = LocalDate.of(2026, 11, 21), onPodesavanja = {}) }
        }
        compose.onNodeWithText("Podešavanja").assertIsDisplayed()
        compose.onNodeWithText("Subota 21. 11.").assertIsDisplayed()
    }
}
