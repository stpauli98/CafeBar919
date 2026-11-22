package dev.nextpixel.cafebar919

import android.content.Context
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.OtvaranjeBaze
import dev.nextpixel.cafebar919.data.StanjeBaze
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.repo.TuraRepo
import dev.nextpixel.cafebar919.ui.podesavanja.PodesavanjaSesija
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
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

    private var nocRepo: NocRepo? = null

    /** Pravi repozitorij noći jednom, tek kad je baza spremna. */
    @Synchronized
    fun noc(): NocRepo {
        nocRepo?.let { return it }
        val stanje = otvaranjeBaze.stanje.value
        check(stanje is StanjeBaze.Spremna) { "Baza još nije spremna" }
        return NocRepo(stanje.db, clock).also { nocRepo = it }
    }

    private var turaRepo: TuraRepo? = null

    /** Pravi repozitorij ture jednom, tek kad je baza spremna. */
    @Synchronized
    fun ture(): TuraRepo {
        turaRepo?.let { return it }
        val stanje = otvaranjeBaze.stanje.value
        check(stanje is StanjeBaze.Spremna) { "Baza još nije spremna" }
        return TuraRepo(stanje.db, clock).also { turaRepo = it }
    }

    /** Posljednja vrijednost „zaključavanje_s“ iz postavki; 120 s dok baza nije spremna. */
    private val zakljucavanjeS = MutableStateFlow(120)

    /** Sesija podešavanja; živi koliko i aplikacija, odbrojava na glavnoj niti. */
    val sesija = PodesavanjaSesija(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) { zakljucavanjeS.value }

    init {
        pokusajPonovo()
        scope.launch {
            otvaranjeBaze.stanje.filterIsInstance<StanjeBaze.Spremna>().first()
            repo().postavke.collect { zakljucavanjeS.value = it.zakljucavanjeS }
        }
    }

    /** Pokreće (ponovo) otvaranje baze; poziva se pri startu i iz dugmeta „Pokušaj ponovo“. */
    fun pokusajPonovo() {
        scope.launch { otvaranjeBaze.pokusaj() }
    }
}
