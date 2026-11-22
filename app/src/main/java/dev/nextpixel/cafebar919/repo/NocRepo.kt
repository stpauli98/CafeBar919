package dev.nextpixel.cafebar919.repo

import androidx.room.withTransaction
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.data.VrstaPrimaoca
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Validacija
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

/** Noć: otvaranje sa snimkom primalaca i praznim nacrtom ture. */
class NocRepo(private val db: AppDatabase, private val clock: Clock) {
    private val dao = db.nocDao()

    val aktivna: Flow<NocE?> = dao.aktivna()

    fun primaoci(nocId: Long): Flow<List<NocPrimalacE>> = dao.primaoci(nocId)

    /** Otvara noć; ako je već jedna aktivna, vraća njen id i ništa ne mijenja. */
    suspend fun otvoriNoc(radniciIds: List<Long>, ispomoc: List<String>): Long = db.withTransaction {
        dao.aktivnaJednom()?.let { return@withTransaction it.id }
        val sada = clock.millis()
        val nocId = dao.dodajNoc(NocE(datum = LocalDate.now(clock).toEpochDay(), otvorenaU = sada, zatvorenaU = null))

        val izabrani = db.radnikDao().jednom().filter { it.aktivan && it.id in radniciIds }
        val redom = izabrani.filter { it.uloga == Uloga.KONOBAR } + izabrani.filter { it.uloga == Uloga.ZA_SANKOM }
        val primaoci = mutableListOf<NocPrimalacE>()
        fun dodaj(vrsta: VrstaPrimaoca, radnikId: Long?, ime: String) {
            primaoci += NocPrimalacE(nocId = nocId, vrsta = vrsta, radnikId = radnikId, ime = ime, redoslijed = primaoci.size)
        }
        redom.forEach { dodaj(if (it.uloga == Uloga.KONOBAR) VrstaPrimaoca.KONOBAR else VrstaPrimaoca.ZA_SANKOM, it.id, it.ime) }

        val zauzeta = mutableListOf<String>()
        for (sirovo in ispomoc) {
            if (Validacija.provjeriIspomoc(sirovo, zauzeta) != Ishod.Ok) continue
            val ime = Validacija.ocisti(sirovo)
            zauzeta += ime
            dodaj(VrstaPrimaoca.ISPOMOC, null, ime)
        }
        dodaj(VrstaPrimaoca.SANK, null, "Šank")
        dao.dodajPrimaoce(primaoci)

        dao.upisiNacrt(NacrtTureE(id = 1, primalacId = null, kljuc = UUID.randomUUID().toString(), stavke = "[]", izmijenjenU = sada))
        nocId
    }
}
