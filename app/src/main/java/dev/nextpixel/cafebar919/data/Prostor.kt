package dev.nextpixel.cafebar919.data

import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Izvor podatka o slobodnom prostoru; razdvojen radi testiranja. */
fun interface Prostor {
    fun slobodnoBajtova(): Long
}

/** Slobodan prostor na particiji u kojoj je [dir]. Poziva se van glavne niti (StatFs može da blokira). */
class ProstorTableta(private val dir: File) : Prostor {
    override fun slobodnoBajtova(): Long = StatFs(dir.path).availableBytes
}

/** PROSTOR-01: „nizak“ je tačno kad je slobodno manje od 200 MB. Izuzetak pri čitanju znači „nije nizak“. */
class ProstorStanje(private val prostor: Prostor) {
    private val _nizak = MutableStateFlow(false)
    val nizak: StateFlow<Boolean> = _nizak

    fun provjeri() {
        _nizak.value = try {
            prostor.slobodnoBajtova() < PRAG
        } catch (e: Exception) {
            false
        }
    }

    /** Skriva baner do sljedeće [provjeri]. */
    fun razumijem() {
        _nizak.value = false
    }

    private companion object {
        const val PRAG = 200L * 1024 * 1024
    }
}
