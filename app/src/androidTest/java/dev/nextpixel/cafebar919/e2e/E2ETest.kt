package dev.nextpixel.cafebar919.e2e

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.CafeBarApp
import dev.nextpixel.cafebar919.MainActivity
import dev.nextpixel.cafebar919.data.StanjeBaze
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule

/**
 * Osnova E2E testova na emulatoru/tabletu: prava aplikacija (MainActivity, pravi sat, prava baza na uređaju).
 * Orchestrator sa clearPackageData briše podatke prije svakog testa, pa svaki test počinje kao prva instalacija:
 * početne mjere, radnici Vuleta, Srđan, Miloš, Smitran i probni PIN 123456, bez kategorija i pića.
 */
abstract class E2ETest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    protected val kontejner: AppContainer
        get() = (compose.activity.application as CafeBarApp).container

    protected fun postoji(tekst: String, dio: Boolean = false) =
        compose.onAllNodesWithText(tekst, substring = dio).fetchSemanticsNodes().isNotEmpty()

    protected fun cekajUslov(opis: String, ms: Long = 10_000, uslov: () -> Boolean) = try {
        compose.waitUntil(ms) { uslov() }
    } catch (e: ComposeTimeoutException) {
        throw AssertionError("$opis\n--- ekran ---\n" + ekran(), e)
    }

    /** Svi tekstovi i opisi na ekranu, za poruku greške. */
    protected fun ekran(): String = runCatching {
        compose.onAllNodes(androidx.compose.ui.test.isRoot()).fetchSemanticsNodes().flatMap { root ->
            fun obidji(n: androidx.compose.ui.semantics.SemanticsNode): List<String> {
                val c = n.config
                val t = buildList {
                    if (c.contains(androidx.compose.ui.semantics.SemanticsProperties.Text)) addAll(c[androidx.compose.ui.semantics.SemanticsProperties.Text].map { it.text })
                    if (c.contains(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription)) addAll(c[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription].map { "[$it]" })
                }
                return t + n.children.flatMap { obidji(it) }
            }
            obidji(root)
        }.joinToString(" | ")
    }.getOrElse { "nije pročitan: $it" }

    protected fun cekaj(tekst: String, dio: Boolean = false, ms: Long = 10_000) =
        cekajUslov("Nema teksta „$tekst“", ms) { postoji(tekst, dio) }

    protected fun cekajNestanak(tekst: String, dio: Boolean = false, ms: Long = 10_000) =
        cekajUslov("Tekst „$tekst“ nije nestao", ms) { !postoji(tekst, dio) }

    /** Dodir na element koji je dugme i nosi tekst (kao korisnik koji pritiska ono što vidi). */
    protected fun dodirni(tekst: String, indeks: Int = 0) {
        cekajUslov("Nema dugmeta „$tekst“") { compose.onAllNodes(hasText(tekst) and hasClickAction()).fetchSemanticsNodes().size > indeks }
        compose.onAllNodes(hasText(tekst) and hasClickAction())[indeks].performClick()
        compose.waitForIdle()
    }

    protected fun dodirniOpis(opis: String, indeks: Int = 0) {
        cekajUslov("Nema elementa „$opis“") { compose.onAllNodesWithContentDescription(opis).fetchSemanticsNodes().size > indeks }
        compose.onAllNodesWithContentDescription(opis)[indeks].performClick()
        compose.waitForIdle()
    }

    protected fun dodirniOznaku(tag: String) {
        cekajUslov("Nema oznake „$tag“") { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).performClick()
        compose.waitForIdle()
    }

    /** Upisuje tekst u n-to polje za unos na ekranu (0 = prvo). */
    protected fun upisi(tekst: String, polje: Int = 0) {
        cekajUslov("Nema polja za unos #$polje") { compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size > polje }
        compose.onAllNodes(hasSetTextAction())[polje].performClick()
        compose.onAllNodes(hasSetTextAction())[polje].performTextInput(tekst)
        compose.waitForIdle()
    }

    protected fun pinTastatura(pin: String) = pin.forEach { c ->
        compose.onNode(hasTextExactly(c.toString()) and hasClickAction()).performClick()
        compose.waitForIdle()
    }

    /** Sa šanka (S0 ili S1) otvara podešavanja PIN-om. */
    protected fun otkljucajPodesavanja(pin: String = "123456") {
        dodirni("Podešavanja")
        cekaj("Odustani")
        pinTastatura(pin)
        cekaj("Otključano za vlasnika.")
    }

    protected fun baza(): SupportSQLiteDatabase {
        cekajUslov("Baza se nije otvorila") { kontejner.otvaranjeBaze.stanje.value is StanjeBaze.Spremna }
        return (kontejner.otvaranjeBaze.stanje.value as StanjeBaze.Spremna).db.openHelper.writableDatabase
    }

    protected fun broj(sql: String): Long = baza().query(sql).use { c -> c.moveToFirst(); c.getLong(0) }

    // ---- priprema podataka kroz repozitorij aplikacije (isti put kao podešavanja, bez klikanja) ----

    protected fun meni(vararg kategorije: Pair<String, List<Pair<String, List<String>>>>) = runBlocking {
        baza()
        val repo = kontejner.repo()
        kategorije.forEach { (kat, pica) ->
            repo.sacuvajKategoriju(null, kat)
            val katId = repo.kategorije.first().first { it.k.naziv == kat }.k.id
            val mjere = repo.mjere.first()
            pica.forEach { (naziv, mjereNazivi) ->
                repo.sacuvajArtikal(null, naziv, katId, mjereNazivi.map { m -> mjere.first { it.m.naziv == m }.m.id })
            }
        }
    }

    /** Uobičajen meni sa pravim nazivima sa karte pića (Mac Tire Pub, Banja Luka). */
    protected fun standardniMeni() = meni(
        "Kafa" to listOf("Espresso" to listOf("Komad")),
        "Pivo" to listOf("Jelen 0,5" to listOf("Komad"), "Nektar 0,5" to listOf("Komad")),
        "Rakija" to listOf("Brkina šljiva" to listOf("Doza", "Bokal")),
    )

    /** Sa S0 otvara noć sa izabranim radnicima (po imenu na pločici). */
    protected fun otvoriNoc(vararg radnici: String) {
        cekaj("Ko radi večeras?")
        radnici.forEach { dodirni(it) }
        val n = radnici.size
        val tekst = "Otvori noć · $n " + when {
            n % 10 == 1 && n % 100 != 11 -> "radnik"
            n % 10 in 2..4 && n % 100 !in 12..14 -> "radnika"
            else -> "radnika"
        }
        dodirni(tekst)
        cekaj("Noć otvorena od", dio = true)
    }

    protected fun matcherTekstIDugme(tekst: String): SemanticsMatcher = hasText(tekst) and hasClickAction()
}
