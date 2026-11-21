package dev.nextpixel.cafebar919

import android.content.Context
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.OtvaranjeBaze
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

    init {
        pokusajPonovo()
    }

    /** Pokreće (ponovo) otvaranje baze; poziva se pri startu i iz dugmeta „Pokušaj ponovo“. */
    fun pokusajPonovo() {
        scope.launch { otvaranjeBaze.pokusaj() }
    }
}
