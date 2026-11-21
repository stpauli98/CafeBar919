package dev.nextpixel.cafebar919.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Vremena su `Long` (ms od epohe), datumi `Long` (epochDay), enumi se čuvaju kao tekst. */
enum class Uloga { KONOBAR, ZA_SANKOM }

enum class VrstaPrimaoca { KONOBAR, ZA_SANKOM, ISPOMOC, SANK }

@Entity(tableName = "kategorija")
data class KategorijaE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val naziv: String,
    val redoslijed: Int,
    val aktivna: Boolean = true,
)

@Entity(tableName = "mjera")
data class MjeraE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val naziv: String,
    val natpis: String,
    @ColumnInfo(name = "u_zbiru") val uZbiru: String,
    val litara: Double?,
    val redoslijed: Int,
    val aktivna: Boolean = true,
)

@Entity(
    tableName = "artikal",
    foreignKeys = [ForeignKey(KategorijaE::class, ["id"], ["kategorija_id"])],
    indices = [Index("kategorija_id")],
)
data class ArtikalE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val naziv: String,
    @ColumnInfo(name = "kategorija_id") val kategorijaId: Long,
    val redoslijed: Int,
    val aktivan: Boolean = true,
)

@Entity(
    tableName = "artikal_mjera",
    primaryKeys = ["artikal_id", "mjera_id"],
    foreignKeys = [
        ForeignKey(ArtikalE::class, ["id"], ["artikal_id"]),
        ForeignKey(MjeraE::class, ["id"], ["mjera_id"]),
    ],
    indices = [Index("mjera_id")],
)
data class ArtikalMjeraE(
    @ColumnInfo(name = "artikal_id") val artikalId: Long,
    @ColumnInfo(name = "mjera_id") val mjeraId: Long,
    val redoslijed: Int,
)

@Entity(tableName = "radnik")
data class RadnikE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ime: String,
    val uloga: Uloga,
    val aktivan: Boolean = true,
    val redoslijed: Int,
)

@Entity(tableName = "razlog_ponistavanja")
data class RazlogE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tekst: String,
    val redoslijed: Int,
    val aktivan: Boolean = true,
)

/** Jedan red, `id` je uvijek 1. */
@Entity(tableName = "postavke")
data class PostavkeE(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "rok_bez_pina_s") val rokBezPinaS: Int,
    @ColumnInfo(name = "istorija_dana") val istorijaDana: Int,
    @ColumnInfo(name = "zakljucavanje_s") val zakljucavanjeS: Int,
    @ColumnInfo(name = "ispomoc_dozvoljena") val ispomocDozvoljena: Boolean,
    @ColumnInfo(name = "pin_hes") val pinHes: String,
    @ColumnInfo(name = "pin_so") val pinSo: String,
    @ColumnInfo(name = "pin_probni") val pinProbni: Boolean,
    @ColumnInfo(name = "pin_pogresnih_zaredom") val pinPogresnihZaredom: Int = 0,
    @ColumnInfo(name = "pin_blokiran_do") val pinBlokiranDo: Long? = null,
)

@Entity(tableName = "noc")
data class NocE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Datum otvaranja, epochDay. */
    val datum: Long,
    @ColumnInfo(name = "otvorena_u") val otvorenaU: Long,
    @ColumnInfo(name = "zatvorena_u") val zatvorenaU: Long? = null,
)

@Entity(
    tableName = "noc_primalac",
    foreignKeys = [ForeignKey(NocE::class, ["id"], ["noc_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("noc_id")],
)
data class NocPrimalacE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "noc_id") val nocId: Long,
    val vrsta: VrstaPrimaoca,
    /** Snimak radnika; null za ispomoć i Šank. */
    @ColumnInfo(name = "radnik_id") val radnikId: Long?,
    val ime: String,
    val redoslijed: Int,
)

@Entity(
    tableName = "tura",
    foreignKeys = [
        ForeignKey(NocE::class, ["id"], ["noc_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(NocPrimalacE::class, ["id"], ["primalac_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("noc_id"), Index("primalac_id"), Index("kljuc", unique = true)],
)
data class TuraE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "noc_id") val nocId: Long,
    @ColumnInfo(name = "primalac_id") val primalacId: Long,
    @ColumnInfo(name = "potvrdjena_u") val potvrdjenaU: Long,
    /** UUID iz nacrta; `UNIQUE` pa se dvostruki upis ignoriše. */
    val kljuc: String,
    @ColumnInfo(name = "ponistena_u") val ponistenaU: Long? = null,
    @ColumnInfo(name = "ponistena_bez_pina") val ponistenaBezPina: Boolean = false,
    @ColumnInfo(name = "razlog_tekst") val razlogTekst: String? = null,
)

/** Snimak naziva, mjere i litara u trenutku unosa; pregledi čitaju samo ovo. */
@Entity(
    tableName = "stavka_ture",
    foreignKeys = [ForeignKey(TuraE::class, ["id"], ["tura_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tura_id")],
)
data class StavkaTureE(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tura_id") val turaId: Long,
    @ColumnInfo(name = "artikal_id") val artikalId: Long,
    @ColumnInfo(name = "artikal_naziv") val artikalNaziv: String,
    @ColumnInfo(name = "kategorija_id") val kategorijaId: Long,
    @ColumnInfo(name = "kategorija_naziv") val kategorijaNaziv: String,
    @ColumnInfo(name = "mjera_id") val mjeraId: Long,
    @ColumnInfo(name = "mjera_naziv") val mjeraNaziv: String,
    @ColumnInfo(name = "mjera_natpis") val mjeraNatpis: String,
    @ColumnInfo(name = "mjera_u_zbiru") val mjeraUZbiru: String,
    @ColumnInfo(name = "mjera_redoslijed") val mjeraRedoslijed: Int,
    val litara: Double?,
    val kolicina: Int,
)

/** Jedan red (`id` = 1): nepotvrđena tura koja preživljava restart. */
@Entity(tableName = "nacrt_ture")
data class NacrtTureE(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "primalac_id") val primalacId: Long?,
    /** JSON lista stavki. */
    val stavke: String = "[]",
    @ColumnInfo(name = "izmijenjen_u") val izmijenjenU: Long,
)
