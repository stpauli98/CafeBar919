package dev.nextpixel.cafebar919.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1280dp-h800dp")
class SankKomponenteTest {
    @get:Rule val compose = createComposeRule()

    @Test fun plocicaPica_jednaMjera_dodirDodajeIZnackaBroj() {
        var dodano = -1L
        compose.setContent {
            CafeBarTema {
                PlocicaPica("Jelen pivo", listOf(MjeraNaPlocici(7, "Komad", "komad", 2)), { dodano = it })
            }
        }
        compose.onNodeWithText("2").assertIsDisplayed()
        compose.onNodeWithText("komad").assertIsDisplayed()
        compose.onNodeWithText("Jelen pivo").performClick()
        assertEquals(7L, dodano)
    }

    @Test fun plocicaPica_dvijeMjere_svakoDugmeSvojaMjera() {
        val dodano = mutableListOf<Long>()
        compose.setContent {
            CafeBarTema {
                PlocicaPica(
                    "Rakija šljiva",
                    listOf(MjeraNaPlocici(1, "0,03", "doza 0,03 l", 2), MjeraNaPlocici(2, "Bokal", "bokal 0,25 l", 1)),
                    { dodano.add(it) },
                )
            }
        }
        compose.onNodeWithText("0,03 · 2").performClick()
        compose.onNodeWithText("Bokal · 1").performClick()
        assertEquals(listOf(1L, 2L), dodano)
    }

    @Test fun plocicaOsobe_izabranaImaSelected() {
        compose.setContent {
            CafeBarTema {
                PlocicaOsobe("Vuleta", "6 kom", izabrana = true, sank = false, onClick = {})
            }
        }
        compose.onNodeWithText("Vuleta").assertIsSelected()
    }

    @Test fun plocicaOsobe_neizabranaNemaSelected() {
        compose.setContent { CafeBarTema { PlocicaOsobe("Srđan", "još ništa", izabrana = false, sank = false, onClick = {}) } }
        compose.onNodeWithText("Srđan").assertIsNotSelected()
    }

    @Test fun stepperTure_plusOnemogucenNa99() {
        compose.setContent { CafeBarTema { StepperTure(99, {}, {}, plusOmogucen = false) } }
        compose.onNodeWithContentDescription("Više").assertIsNotEnabled()
        compose.onNodeWithText("99").assertIsDisplayed()
    }

    @Test fun poljePretrage_placeholderIUpis() {
        var upit = ""
        val stanje = mutableStateOf("")
        compose.setContent { CafeBarTema { PoljePretrage(stanje.value, { stanje.value = it; upit = it }) } }
        compose.onNodeWithText("Traži piće").assertIsDisplayed()
        compose.onNodeWithText("Traži piće").performTextInput("jack")
        assertEquals("jack", upit)
    }

    @Test fun dodirneZone_najmanje56() {
        compose.setContent {
            CafeBarTema {
                PlocicaPica(
                    "Rakija šljiva",
                    listOf(MjeraNaPlocici(1, "0,03", "doza", 0), MjeraNaPlocici(2, "Bokal", "bokal", 0)),
                    {},
                )
                StepperTure(2, {}, {}, true)
                PoljePretrage("", {})
            }
        }
        compose.onNodeWithText("0,03").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithContentDescription("Manje").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithContentDescription("Više").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithText("Traži piće").assertHeightIsAtLeast(56.dp)
    }
}
