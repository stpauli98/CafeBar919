package dev.nextpixel.cafebar919.repo

import dev.nextpixel.cafebar919.data.ArtikalE
import dev.nextpixel.cafebar919.data.KategorijaE
import dev.nextpixel.cafebar919.data.MjeraE

data class KategorijaPrikaz(val k: KategorijaE, val brojAktivnihPica: Int)

/** `aktivnaPica`: imena aktivnih pića koja koriste ovu mjeru. */
data class MjeraPrikaz(val m: MjeraE, val aktivnaPica: List<String>)

data class ArtikalPrikaz(val a: ArtikalE, val kategorija: KategorijaE, val mjere: List<MjeraE>)

/** Samo aktivne stavke (spec 7 #15). */
data class BrojaciMenija(val pica: Int, val kategorija: Int, val mjera: Int, val stalnihRadnika: Int)
