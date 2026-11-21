package dev.nextpixel.cafebar919.repo

import androidx.room.withTransaction
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.PostavkeE
import dev.nextpixel.cafebar919.domain.Pin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant

sealed interface ProvjeraPina {
    data object Tacan : ProvjeraPina
    data class Pogresan(val preostalo: Int) : ProvjeraPina
    data class Blokiran(val do_: Instant) : ProvjeraPina
}

sealed interface PromjenaPina {
    data object Uspjeh : PromjenaPina
    data object PogresanTrenutni : PromjenaPina
    data class Blokiran(val do_: Instant) : PromjenaPina
    data object Zabranjen : PromjenaPina
    data object NeispravanFormat : PromjenaPina
}

/** PIN vlasnika: provjera, blokada poslije 10 pogrešnih (čuva se u bazi) i promjena. */
class PinServis(private val db: AppDatabase, private val clock: Clock) {
    private val dao = db.postavkeDao()

    val probni: Flow<Boolean> = dao.postavke().map { it.pinProbni }

    suspend fun provjeri(pin: String): ProvjeraPina = db.withTransaction { pokusaj(pin) }

    suspend fun blokiranDo(): Instant? {
        val zapisano = dao.jedne().pinBlokiranDo ?: return null
        val sada = clock.instant()
        val do_ = ogranici(zapisano, sada)
        // Samo kad je sat vraćen unazad treba pisati: skraćena blokada se čuva.
        if (do_.toEpochMilli() != zapisano) {
            db.withTransaction { dao.jedne().let { dao.izmijeni(it.copy(pinBlokiranDo = do_.toEpochMilli())) } }
        }
        return do_.takeIf { it.isAfter(sada) }
    }

    /** Sat vraćen unazad ne smije produžiti blokadu: najviše `Pin.BLOKADA` od sada. */
    private fun ogranici(blokiranDoMs: Long, sada: Instant): Instant =
        Instant.ofEpochMilli(blokiranDoMs).coerceAtMost(sada.plus(Pin.BLOKADA))

    suspend fun promijeni(trenutni: String, novi: String): PromjenaPina = db.withTransaction {
        when (val r = pokusaj(trenutni)) {
            is ProvjeraPina.Blokiran -> PromjenaPina.Blokiran(r.do_)
            is ProvjeraPina.Pogresan -> PromjenaPina.PogresanTrenutni
            ProvjeraPina.Tacan -> when {
                !FORMAT.matches(novi) -> PromjenaPina.NeispravanFormat
                Pin.zabranjen(novi) -> PromjenaPina.Zabranjen
                else -> {
                    val (hes, so) = Pin.hesiraj(novi)
                    dao.izmijeni(dao.jedne().copy(pinHes = hes, pinSo = so, pinProbni = false))
                    PromjenaPina.Uspjeh
                }
            }
        }
    }

    /** Čita-mijenja-piše brojač; mora se zvati unutar transakcije. */
    private suspend fun pokusaj(pin: String): ProvjeraPina {
        val sada = clock.instant()
        var p: PostavkeE = dao.jedne()
        val blokiranDo = p.pinBlokiranDo
        if (blokiranDo != null) {
            val do_ = ogranici(blokiranDo, sada)
            if (do_.toEpochMilli() != blokiranDo) {
                p = p.copy(pinBlokiranDo = do_.toEpochMilli())
                dao.izmijeni(p)
            }
            if (do_.isAfter(sada)) return ProvjeraPina.Blokiran(do_)
            p = p.copy(pinPogresnihZaredom = 0, pinBlokiranDo = null)
        }
        if (FORMAT.matches(pin) && Pin.provjeri(pin, p.pinHes, p.pinSo)) {
            dao.izmijeni(p.copy(pinPogresnihZaredom = 0))
            return ProvjeraPina.Tacan
        }
        val brojac = p.pinPogresnihZaredom + 1
        if (brojac >= Pin.MAX_POGRESNIH) {
            val do_ = sada.plus(Pin.BLOKADA)
            dao.izmijeni(p.copy(pinPogresnihZaredom = brojac, pinBlokiranDo = do_.toEpochMilli()))
            return ProvjeraPina.Blokiran(do_)
        }
        dao.izmijeni(p.copy(pinPogresnihZaredom = brojac))
        return ProvjeraPina.Pogresan(Pin.MAX_POGRESNIH - brojac)
    }

    private companion object {
        val FORMAT = Regex("[0-9]{${Pin.DUZINA}}")
    }
}
