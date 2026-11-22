package dev.nextpixel.cafebar919.ui.podesavanja.artikli

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.KategorijaE
import dev.nextpixel.cafebar919.data.MjeraE
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.Cip
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

private val NASLOV_SEKCIJE = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)

/**
 * V1.1 / V1.2 (Paper): prozor 820, forma lijevo i živi pregled pločice desno. Izlazak sa izmjenama zamjenjuje
 * sadržaj potvrdom O6 (ne prozor preko prozora, O5).
 */
@Composable
fun ArtikalProzor(forma: ArtikalForma, kategorije: List<KategorijaE>, mjere: List<MjeraE>, vm: ArtikliViewModel) {
    val novi = forma.id == null
    if (forma.potvrdaOdbacivanja) {
        Prozor(
            naslov = if (novi) "Odbaciti novi artikal?" else "Odbaciti izmjene?",
            onZatvori = vm::nastaviUnos,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    if (novi) "Uneseni naziv i mjera se ne čuvaju." else "Izmjene se ne čuvaju.",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                )
            },
            dugmad = {
                SporednoDugme("Nastavi unos", vm::nastaviUnos)
                OpasnoDugme("Odbaci", vm::odbaci)
            },
        )
        return
    }
    // Isključene stavke se ne nude, osim onih koje je artikal već imao (da se vide i mogu skinuti).
    val ponudeneKategorije = kategorije.filter { it.aktivna || it.id == forma.pocetna.kategorijaId }
    val ponudeneMjere = mjere.filter { it.aktivna || it.id in forma.pocetna.mjereIds }
    val izabraneMjere = mjere.filter { it.id in forma.mjereIds }

    Prozor(
        naslov = if (novi) "Novi artikal" else "Izmjena · ${forma.pocetna.naziv}",
        onZatvori = vm::odustani,
        sirina = 820.dp,
        sadrzaj = {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    var imaoFokus by remember { mutableStateOf(false) }
                    Polje(
                        labela = "Naziv",
                        vrijednost = forma.naziv,
                        onPromjena = vm::promijeniNaziv,
                        greska = forma.greskaNaziv,
                        placeholder = "npr. Jägermeister 0,7",
                        max = Validacija.NAZIV_ARTIKLA_MAX,
                        brojacOd = Validacija.BROJAC_OD,
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) imaoFokus = true else if (imaoFokus) { imaoFokus = false; vm.nazivNapusten() }
                        },
                    )
                    Sekcija("Kategorija") {
                        ponudeneKategorije.forEach { k ->
                            Cip(
                                k.naziv,
                                izabran = forma.kategorijaId == k.id,
                                onClick = { vm.izaberiKategoriju(k.id) },
                                modifier = Modifier.testTag("kategorija:${k.naziv}"),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Sekcija("Dugmad na pločici · najviše 3") {
                            ponudeneMjere.forEach { m ->
                                val izabrana = m.id in forma.mjereIds
                                Cip(
                                    Format.oznakaMjere(m.naziv, m.litara, saJedinicom = true),
                                    izabran = izabrana,
                                    onClick = { vm.prebaciMjeru(m.id) },
                                    modifier = Modifier.testTag("mjera:${m.naziv}"),
                                    omogucen = izabrana || forma.mjereIds.size < 3,
                                )
                            }
                        }
                        Text(
                            "Mjere se uređuju u meniju Mjere. Litre u pregledu se računaju po mjeri.",
                            color = Boje.muted,
                            style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        )
                    }
                }
                Column(Modifier.width(212.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Na šanku izgleda ovako", color = Boje.ink, style = NASLOV_SEKCIJE)
                    PregledPlocice(Validacija.ocisti(forma.naziv), izabraneMjere)
                }
            }
        },
        dugmad = {
            if (forma.greska != null) {
                Text(
                    forma.greska,
                    Modifier.weight(1f),
                    color = Boje.danger,
                    style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
                )
            } else if (forma.napomena != null) {
                Text(
                    forma.napomena,
                    Modifier.weight(1f),
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
                )
            }
            SporednoDugme("Odustani", vm::odustani)
            GlavnoDugme(
                if (novi) "Sačuvaj artikal" else "Sačuvaj izmjenu",
                vm::sacuvaj,
                ikona = Icons.Rounded.Check,
                omoguceno = forma.moguceSacuvati && !forma.upis,
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Sekcija(naslov: String, cipovi: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(naslov, color = Boje.ink, style = NASLOV_SEKCIJE)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { cipovi() }
    }
}

/**
 * Pločica kao na šanku (Paper V1.1/V1.2): 212×120, tile podloga, radius 16. Sa 2–3 mjere dugmad su unutra
 * (natpis mjere), sa jednom je pločica kao na S1 sa natpisom malim slovom dole.
 */
@Composable
private fun PregledPlocice(naziv: String, mjere: List<MjeraE>) {
    val jedna = mjere.size == 1
    Column(
        Modifier
            .width(212.dp)
            .height(120.dp)
            .background(Boje.tile, Oblici.lg)
            .then(
                if (jedna) Modifier.padding(14.dp) else Modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 4.dp),
            ),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            naziv,
            Modifier.padding(horizontal = if (jedna) 0.dp else 10.dp),
            color = Boje.ink,
            maxLines = if (jedna) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        )
        if (jedna) {
            Text(
                mjere.single().natpis.lowercase(),
                color = Boje.muted,
                style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                mjere.forEach { m ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(Boje.bg, Oblici.md)
                            .border(1.5.dp, Boje.line, Oblici.md),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            m.natpis,
                            color = Boje.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                        )
                    }
                }
            }
        }
    }
}
