package dev.nextpixel.cafebar919.ui.podesavanja.pravila

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.PostavkeE
import dev.nextpixel.cafebar919.data.RazlogE
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.Cip
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prekidac
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.components.Stepper
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** V6 · Pravila rada (Paper): zaglavlje, pet redova (3 stepera, prekidač, razlozi kao čipovi). */
@Composable
fun PravilaEkran(vm: PravilaViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }
    val postavke by vm.postavke.collectAsState()
    val razlozi by vm.razlozi.collectAsState()
    val potvrda by vm.potvrda.collectAsState()
    val razlog by vm.razlog.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Pravila rada", color = Boje.ink, style = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold))
            Text(
                "Važe od sljedeće radnje. Ture koje su već unesene se ne mijenjaju.",
                color = Boje.muted,
                style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
            )
        }
        postavke?.let { p -> Redovi(p, vm, razlozi) }
    }

    potvrda?.let { t ->
        Prozor(
            naslov = "Obrisati stare noći?",
            onZatvori = vm::nastaviUnos,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    "${Format.brojac(t.noci, "noć", "noći", "noći")} starijih od ${t.dana} dana biće obrisano pri sljedećem pokretanju.",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                )
            },
            dugmad = {
                SporednoDugme("Nastavi unos", vm::nastaviUnos)
                OpasnoDugme("Smanji istoriju", vm::smanjiIstoriju)
            },
        )
    }
    razlog?.let { RazlogProzor(it, vm) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Redovi(p: PostavkeE, vm: PravilaViewModel, razlozi: List<RazlogE>?) {
    Column(Modifier.fillMaxWidth()) {
        Red("Poništavanje bez PIN-a", "Koliko dugo poslije potvrde radnik može sam poništiti turu.") {
            Stepper(
                p.rokBezPinaS.toString(), "s",
                onMinus = { vm.rokBezPina(-1) }, onPlus = { vm.rokBezPina(1) },
                minusOmogucen = p.rokBezPinaS > ROK_MIN_S, plusOmogucen = p.rokBezPinaS < ROK_MAX_S,
            )
        }
        Red("Istorija noći", "Starije zatvorene noći se brišu same.") {
            Stepper(
                p.istorijaDana.toString(), Format.mnozina(p.istorijaDana, "dan", "dana", "dana"),
                onMinus = { vm.istorija(-1) }, onPlus = { vm.istorija(1) },
                minusOmogucen = p.istorijaDana > ISTORIJA_MIN_DANA, plusOmogucen = p.istorijaDana < ISTORIJA_MAX_DANA,
            )
        }
        Red("Zaključavanje podešavanja", "Poslije koliko vremena bez dodira se podešavanja zaključaju.") {
            Stepper(
                (p.zakljucavanjeS / 60).toString(), "min",
                onMinus = { vm.zakljucavanje(-1) }, onPlus = { vm.zakljucavanje(1) },
                minusOmogucen = p.zakljucavanjeS > ZAKLJUCAVANJE_MIN_S, plusOmogucen = p.zakljucavanjeS < ZAKLJUCAVANJE_MAX_S,
            )
        }
        Red("Ispomoć", "Radnik na šanku može upisati ime ispomoći pri otvaranju noći.") {
            Prekidac(p.ispomocDozvoljena, vm::postaviIspomoc, opis = "Ispomoć")
        }
        Red("Razlozi poništavanja", "Nude se kao dugmad kad vlasnik poništava turu.") {
            FlowRow(Modifier.widthIn(max = 520.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                razlozi?.forEach { r ->
                    Cip(r.tekst, izabran = false, onClick = {}, ukloni = { vm.ukloniRazlog(r.id) }, opisUklanjanja = "Ukloni ${r.tekst}")
                }
                DodajRazlog(vm::otvoriRazlog)
            }
        }
    }
}

/** Red postavke: naslov 17/700, opis 14/20 sivo, kontrola desno; min visina 84, razmak 24 (Paper V6). */
@Composable
private fun Red(naslov: String, opis: String, kontrola: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 84.dp).drawBehind {
            drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx())
        }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(naslov, color = Boje.ink, style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold))
            Text(opis, color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp))
        }
        kontrola()
    }
}

/** Isprekidani zlatni čip „+ Razlog“ (Paper): okvir 1,5 neutralna linija, tekst primary 14/600. */
@Composable
private fun DodajRazlog(onClick: () -> Unit) {
    val boja = Boje.neutralLine
    Box(
        Modifier
            .heightIn(min = 56.dp)
            .clip(Oblici.md)
            .drawBehind {
                val debljina = 1.5.dp.toPx()
                drawRoundRect(
                    boja,
                    topLeft = Offset(debljina / 2, debljina / 2),
                    size = Size(size.width - debljina, size.height - debljina),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(debljina, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("+ Razlog", color = Boje.primary, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RazlogProzor(forma: RazlogForma, vm: PravilaViewModel) {
    var imaoFokus by remember { mutableStateOf(false) }
    Prozor(
        naslov = "Novi razlog",
        onZatvori = vm::odustaniOdRazloga,
        sirina = 520.dp,
        sadrzaj = {
            Polje(
                labela = "Razlog",
                vrijednost = forma.tekst,
                onPromjena = vm::promijeniRazlog,
                greska = forma.greska,
                max = Validacija.RAZLOG_MAX,
                brojacOd = Validacija.RAZLOG_MAX - 4,
                modifier = Modifier.onFocusChanged {
                    if (it.hasFocus) imaoFokus = true else if (imaoFokus) { imaoFokus = false; vm.razlogNapusten() }
                },
            )
        },
        dugmad = {
            SporednoDugme("Odustani", vm::odustaniOdRazloga)
            GlavnoDugme("Dodaj razlog", vm::dodajRazlog, ikona = Icons.Rounded.Check, omoguceno = !forma.upis)
        },
    )
}
