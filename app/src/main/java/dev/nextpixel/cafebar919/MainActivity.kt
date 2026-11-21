package dev.nextpixel.cafebar919

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.nextpixel.cafebar919.data.StanjeBaze
import dev.nextpixel.cafebar919.ui.greska.BazaGreskaEkran
import dev.nextpixel.cafebar919.ui.pocetni.PocetniEkran
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CafeBarApp
        setContent {
            CafeBarTema {
                val stanje by app.container.otvaranjeBaze.stanje.collectAsState()
                when (stanje) {
                    is StanjeBaze.Spremna -> PocetniEkran(danas = LocalDate.now(app.clock), onPodesavanja = {})
                    is StanjeBaze.Greska -> BazaGreskaEkran(onPonovo = app.container::pokusajPonovo)
                    StanjeBaze.Ucitavanje -> Box(Modifier.fillMaxSize().background(Boje.bg))
                }
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
