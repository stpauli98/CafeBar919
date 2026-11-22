package dev.nextpixel.cafebar919.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.sank.NovaNocEkran
import dev.nextpixel.cafebar919.ui.sank.NovaNocViewModel
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(qualifiers = "w1280dp-h800dp")
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

    @Test fun novaNocPrikazujePodesavanjaIDatum() {
        val zona = ZoneId.systemDefault()
        val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(21, 0).atZone(zona).toInstant(), zona)
        val db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        try {
            val vm = NovaNocViewModel(PodesavanjaRepo(db, sat), NocRepo(db, sat), sat, poslijeOtvaranja = {})
            compose.setContent { CafeBarTema { NovaNocEkran(vm, onPodesavanja = {}) } }
            compose.onNodeWithText("Podešavanja").assertIsDisplayed()
            compose.onNodeWithText("Subota 21. 11.").assertIsDisplayed()
        } finally {
            db.close()
        }
    }
}
