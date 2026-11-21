package dev.nextpixel.cafebar919

import android.content.Context
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.OtvaranjeBaze
import dev.nextpixel.cafebar919.data.StanjeBaze
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Clock

/** Drži sat i otvaranje baze; repozitoriji i servisi se dodaju kad ih taskovi naprave. */
class AppContainer(context: Context, val clock: Clock = Clock.systemDefaultZone()) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val otvaranjeBaze = OtvaranjeBaze { AppDatabase.napravi(appContext) }

    private var podesavanjaRepo: PodesavanjaRepo? = null

    /** Pravi repozitorij jednom, tek kad je baza spremna. */
    @Synchronized
    fun repo(): PodesavanjaRepo {
        podesavanjaRepo?.let { return it }
        val stanje = otvaranjeBaze.stanje.value
        check(stanje is StanjeBaze.Spremna) { "Baza još nije spremna" }
        return PodesavanjaRepo(stanje.db, clock).also { podesavanjaRepo = it }
    }

    private var pinServis: PinServis? = null

    /** Pravi PIN servis jednom, tek kad je baza spremna. */
    @Synchronized
    fun pin(): PinServis {
        pinServis?.let { return it }
        val stanje = otvaranjeBaze.stanje.value
        check(stanje is StanjeBaze.Spremna) { "Baza još nije spremna" }
        return PinServis(stanje.db, clock).also { pinServis = it }
    }

    init {
        pokusajPonovo()
    }

    /** Pokreće (ponovo) otvaranje baze; poziva se pri startu i iz dugmeta „Pokušaj ponovo“. */
    fun pokusajPonovo() {
        scope.launch { otvaranjeBaze.pokusaj() }
    }
}
