package dev.nextpixel.cafebar919

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.nextpixel.cafebar919.ui.pocetni.PocetniEkran
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CafeBarApp
        setContent {
            CafeBarTema {
                PocetniEkran(danas = LocalDate.now(app.clock), onPodesavanja = {})
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) sakrijSistemskeTrake()
    }

    private fun sakrijSistemskeTrake() {
        val kontroler = WindowCompat.getInsetsController(window, window.decorView)
        kontroler.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        kontroler.hide(WindowInsetsCompat.Type.systemBars())
    }
}
