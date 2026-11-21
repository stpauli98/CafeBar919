package dev.nextpixel.cafebar919

import android.app.Application
import java.time.Clock

class CafeBarApp : Application() {
    val clock: Clock = Clock.systemDefaultZone()
}
