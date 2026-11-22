package dev.nextpixel.cafebar919.data

import androidx.room.Embedded
import androidx.room.Relation

data class TuraSaStavkama(
    @Embedded val tura: TuraE,
    @Relation(parentColumn = "id", entityColumn = "tura_id") val stavke: List<StavkaTureE>,
    @Relation(parentColumn = "primalac_id", entityColumn = "id") val primalac: NocPrimalacE,
)

data class StavkaPrimaoca(val primalacId: Long, val uZbiru: String, val mjeraRedoslijed: Int, val kolicina: Int)
