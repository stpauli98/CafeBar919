package dev.nextpixel.cafebar919.ui.components

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KomponenteTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pinUnos_sestCifaraIBrisanje() {
        val unos = mutableStateListOf<Char>()
        compose.setContent { CafeBarTema { PinUnos(unos.size, null, { unos.add(it) }, { unos.removeLastOrNull() }) } }
        "123456".forEach { compose.onNodeWithText(it.toString()).performClick() }
        assertEquals("123456".toList(), unos.toList())
        compose.onNodeWithContentDescription("Obriši cifru").performClick()
        assertEquals(5, unos.size)
    }

    @Test fun pinUnos_onemogucenNePrimaCifre() {
        val unos = mutableStateListOf<Char>()
        compose.setContent {
            CafeBarTema { PinUnos(unos.size, null, { unos.add(it) }, { unos.removeLastOrNull() }, omogucen = false) }
        }
        compose.onNodeWithText("1").performClick()
        assertTrue(unos.isEmpty())
    }

    @Test fun pinUnos_greskaSeVidi() {
        compose.setContent { CafeBarTema { PinUnos(0, "PIN nije tačan. Unesi ponovo. · PIN-01", {}, {}) } }
        compose.onNodeWithText("PIN nije tačan. Unesi ponovo. · PIN-01").assertIsDisplayed()
    }

    @Test fun glavnoDugme_upisujeIgnoriseDrugiKlik() {
        var klikova = 0
        compose.setContent { CafeBarTema { GlavnoDugme("Potvrdi", { klikova++ }, upisuje = true) } }
        compose.onNodeWithText("Upisuje se…").assertIsDisplayed()
        compose.onNodeWithText("Upisuje se…").performClick()
        assertEquals(0, klikova)
    }

    @Test fun glavnoDugme_klikRadi() {
        var klikova = 0
        compose.setContent { CafeBarTema { GlavnoDugme("Potvrdi", { klikova++ }) } }
        compose.onNodeWithText("Potvrdi").performClick()
        assertEquals(1, klikova)
    }

    @Test fun polje_prikazujeBrojacOd20() {
        val vrijednost = mutableStateOf("a".repeat(21))
        compose.setContent {
            CafeBarTema { Polje("Naziv", vrijednost.value, { vrijednost.value = it }, max = 24, brojacOd = 20) }
        }
        compose.onNodeWithText("21/24").assertIsDisplayed()
        vrijednost.value = "a".repeat(19)
        compose.waitForIdle()
        compose.onNodeWithText("19/24").assertDoesNotExist()
    }

    @Test fun polje_neprimaViseOdMax() {
        val vrijednost = mutableStateOf("a".repeat(23))
        compose.setContent {
            CafeBarTema { Polje("Naziv", vrijednost.value, { vrijednost.value = it }, max = 24, placeholder = "npr.") }
        }
        compose.onNodeWithText("Naziv").assertIsDisplayed()
        compose.onNodeWithText("a".repeat(23)).performTextInput("bbb")
        assertEquals(23, vrijednost.value.length)
    }

    @Test fun polje_prikazujeGresku() {
        compose.setContent {
            CafeBarTema { Polje("Naziv", "Jack", {}, greska = "Taj naziv već postoji · ARTIKAL-01") }
        }
        compose.onNodeWithText("Taj naziv već postoji · ARTIKAL-01").assertIsDisplayed()
    }

    @Test fun stepper_onemogucenPlusNaMaksimumu() {
        compose.setContent {
            CafeBarTema { Stepper("3", "doza", {}, {}, minusOmogucen = true, plusOmogucen = false) }
        }
        compose.onNodeWithContentDescription("Plus").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Minus").assertIsEnabled()
    }

    @Test fun stepper_prikazujeVrijednostSaJedinicom() {
        compose.setContent { CafeBarTema { Stepper("60", "s", {}, {}, minusOmogucen = true, plusOmogucen = true) } }
        compose.onNodeWithText("60 s").assertIsDisplayed()
    }

    @Test fun segmentiraniIzbor_javljaIndeks() {
        var izbor = -1
        compose.setContent { CafeBarTema { SegmentiraniIzbor(listOf("Konobar", "Šank"), 0, { izbor = it }) } }
        compose.onNodeWithText("Šank").performClick()
        assertEquals(1, izbor)
    }

    @Test fun baner_dugmeZoveAkciju() {
        var kliknuto = false
        compose.setContent {
            CafeBarTema { Baner(VrstaBanera.UPOZORENJE, "Otključano za vlasnika.", "Zaključava se za 2 min.", "Zaključaj", { kliknuto = true }) }
        }
        compose.onNodeWithText("Otključano za vlasnika.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Zaključaj").performClick()
        assertTrue(kliknuto)
    }

    @Test fun prozor_prikazujeSadrzajIDugmad() {
        var zatvoreno = false
        compose.setContent {
            CafeBarTema {
                Prozor("Završiti noć?", { zatvoreno = true }, 480.dp, sadrzaj = { androidx.compose.material3.Text("Tekst prozora") }, dugmad = { SporednoDugme("Nastavi unos", {}) })
            }
        }
        compose.onNodeWithText("Završiti noć?").assertIsDisplayed()
        compose.onNodeWithText("Tekst prozora").assertIsDisplayed()
        compose.onNodeWithText("Nastavi unos").assertIsDisplayed()
    }

    @Test fun toast_prikazujeSeINestajeNakon4s() {
        val stanje = ToastStanje()
        compose.setContent { CafeBarTema { ToastHost(stanje) } }
        stanje.prikazi("Upisano: Vuleta · 3 stavke", "Poništi")
        compose.waitForIdle()
        compose.onNodeWithText("Upisano: Vuleta · 3 stavke").assertIsDisplayed()
        compose.onNodeWithText("Poništi").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(3_000)
        compose.onNodeWithText("Upisano: Vuleta · 3 stavke").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(1_500)
        compose.onNodeWithText("Upisano: Vuleta · 3 stavke").assertDoesNotExist()
    }

    @Test fun toast_noviZamjenjujeStari() {
        val stanje = ToastStanje()
        compose.setContent { CafeBarTema { ToastHost(stanje) } }
        stanje.prikazi("Prvi")
        compose.waitForIdle()
        stanje.prikazi("Drugi")
        compose.waitForIdle()
        compose.onNodeWithText("Prvi").assertDoesNotExist()
        compose.onNodeWithText("Drugi").assertIsDisplayed()
    }

    @Test fun cip_pilula_prekidac_rade() {
        var cip = 0; var pilula = 0; var prekidac: Boolean? = null
        compose.setContent {
            CafeBarTema {
                androidx.compose.foundation.layout.Column {
                    Cip("Piće", true, { cip++ })
                    Pilula("Sve · 15", false, { pilula++ })
                    Prekidac(false, { prekidac = it })
                    OpasnoDugme("Odbaci", {})
                }
            }
        }
        compose.onNodeWithText("Piće").performClick()
        compose.onNodeWithText("Sve · 15").performClick()
        compose.onNodeWithContentDescription("Prekidač").performClick()
        assertEquals(1, cip); assertEquals(1, pilula); assertEquals(true, prekidac)
    }

    @Test fun prekidac_koristiProsljedjeniOpis() {
        var stanje: Boolean? = null
        compose.setContent { CafeBarTema { Prekidac(false, { stanje = it }, opis = "Aktivan: Jelen pivo") } }
        compose.onNodeWithContentDescription("Aktivan: Jelen pivo").performClick()
        assertEquals(true, stanje)
    }
}
