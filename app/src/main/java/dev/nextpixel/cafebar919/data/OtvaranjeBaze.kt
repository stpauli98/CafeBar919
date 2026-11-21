package dev.nextpixel.cafebar919.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface StanjeBaze {
    data object Ucitavanje : StanjeBaze
    data class Spremna(val db: AppDatabase) : StanjeBaze
    data class Greska(val e: Exception) : StanjeBaze
}

/** Otvara bazu i hvata grešku (BAZA-01). Nikad ne pravi novu bazu preko stare. */
class OtvaranjeBaze(private val otvori: suspend () -> AppDatabase) {
    private val _stanje = MutableStateFlow<StanjeBaze>(StanjeBaze.Ucitavanje)
    val stanje: StateFlow<StanjeBaze> = _stanje

    private val brava = Mutex()

    suspend fun pokusaj() = brava.withLock {
        if (_stanje.value is StanjeBaze.Spremna) return@withLock
        _stanje.value = StanjeBaze.Ucitavanje
        var db: AppDatabase? = null
        try {
            db = otvori()
            // Prisiljava stvarno otvaranje (i pravljenje/migraciju) odmah, ne tek pri prvom upitu.
            withContext(Dispatchers.IO) { db.openHelper.writableDatabase }
            _stanje.value = StanjeBaze.Spremna(db)
        } catch (e: CancellationException) {
            db?.close()
            throw e
        } catch (e: Exception) {
            Log.e("CafeBar919", "Baza se nije otvorila", e)
            db?.close()
            _stanje.value = StanjeBaze.Greska(e)
        }
    }
}
