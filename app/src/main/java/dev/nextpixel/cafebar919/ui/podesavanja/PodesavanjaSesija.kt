package dev.nextpixel.cafebar919.ui.podesavanja

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Otključana sesija podešavanja: odbrojava svake sekunde i zaključava se na 0.
 * `dodir()` vraća brojač na `zakljucavanjeS()` (samo dok je otključano).
 */
class PodesavanjaSesija(private val scope: CoroutineScope, private val zakljucavanjeS: () -> Int) {
    private val _otkljucano = MutableStateFlow(false)
    private val _preostaloS = MutableStateFlow(0)
    private var odbrojavanje: Job? = null

    val otkljucano: StateFlow<Boolean> = _otkljucano.asStateFlow()
    val preostaloS: StateFlow<Int> = _preostaloS.asStateFlow()

    fun otkljucaj() {
        odbrojavanje?.cancel()
        _preostaloS.value = zakljucavanjeS()
        _otkljucano.value = true
        odbrojavanje = scope.launch {
            while (true) {
                delay(1000)
                val ostalo = _preostaloS.value - 1
                if (ostalo <= 0) {
                    zakljucaj()
                    return@launch
                }
                _preostaloS.value = ostalo
            }
        }
    }

    fun dodir() {
        if (_otkljucano.value) _preostaloS.value = zakljucavanjeS()
    }

    fun zakljucaj() {
        odbrojavanje?.cancel()
        odbrojavanje = null
        _otkljucano.value = false
        _preostaloS.value = 0
    }
}
