package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.TuraSaStavkama
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Rok
import dev.nextpixel.cafebar919.ui.components.Znacka
import dev.nextpixel.cafebar919.ui.components.VrstaZnacke
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst
import java.time.ZoneId

/**
 * Donja traka S1 (Paper: visina 72, gornja linija 1, padding 24, razmak 12): „POSLJEDNJE“ i vodoravni red čipova
 * najnovijih tura. Dok traje rok bez PIN-a, čip ima dugme „Poništi · {odbrojavanje}“. Čipovi nisu dodirljivi (S1.3 nije u fazi 2).
 */
@Composable
internal fun PosljednjeTraka(
    posljednje: List<TuraSaStavkama>,
    rokS: Int,
    sadaMs: Long,
    zona: ZoneId,
    onPonisti: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind { drawLine(Boje.line, Offset(0f, 0.5.dp.toPx()), Offset(size.width, 0.5.dp.toPx()), 1.dp.toPx()) }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Posljednje".uppercase(), color = Boje.muted, style = NatpisGrupe)
        if (posljednje.isEmpty()) {
            Text("Još nema tura ove noći.", color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp))
        } else {
            LazyRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(posljednje, key = { it.tura.id }) { t -> Cip(t, rokS, sadaMs, zona, onPonisti) }
            }
        }
    }
}

@Composable
private fun Cip(t: TuraSaStavkama, rokS: Int, sadaMs: Long, zona: ZoneId, onPonisti: (Long) -> Unit) {
    val ponistena = t.tura.ponistenaU != null
    val preostalo = if (ponistena) 0 else Rok.preostaloSekundi(t.tura.potvrdjenaU, sadaMs, rokS)
    val sazetak = Format.sazetakTure(t.stavke.map { Format.StavkaSazetka(it.artikalNaziv, it.mjeraUZbiru, it.litara, it.kolicina) })
    Row(
        Modifier
            .clip(Oblici.md)
            .background(Boje.surface)
            .padding(top = 4.dp, bottom = 4.dp, start = 14.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            Format.vrijeme(t.tura.potvrdjenaU, zona),
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
        )
        Text(t.primalac.ime, color = Boje.ink, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        Text(
            sazetak,
            Modifier.widthIn(max = 320.dp),
            color = if (ponistena) Boje.muted else Boje.ink,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, textDecoration = if (ponistena) TextDecoration.LineThrough else TextDecoration.None),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (t.tura.ponistenaBezPina) Znacka("Poništeno bez PIN-a", VrstaZnacke.PONISTENO_BEZ_PINA)
        if (preostalo > 0) PonistiDugme(Format.odbrojavanje(preostalo)) { onPonisti(t.tura.id) }
    }
}

/** Dugme u čipu (Paper: okvir 1,5 `neutralLine`, radius 10, padding 14, ikona 18, tekst 14/600); visina 56 zbog dodira. */
@Composable
private fun PonistiDugme(odbrojavanje: String, onClick: () -> Unit) {
    val oblik = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
    Row(
        Modifier
            .heightIn(min = 56.dp)
            .clip(oblik)
            .border(BorderStroke(1.5.dp, Boje.neutralLine), oblik)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.AutoMirrored.Rounded.Undo, null, Modifier.size(18.dp), tint = Boje.ink)
        Text(
            "Poništi · $odbrojavanje",
            color = Boje.ink,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
            maxLines = 1,
        )
    }
}
