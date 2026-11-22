package dev.nextpixel.cafebar919

import android.app.Application
import java.time.Clock

class CafeBarApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
    val clock: Clock get() = container.clock
}
