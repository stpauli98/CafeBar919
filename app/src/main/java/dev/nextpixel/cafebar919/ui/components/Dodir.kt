package dev.nextpixel.cafebar919.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Javlja svaki dodir u podešavanjima (spec 6.3 „Svaki dodir vraća odbrojavanje“). Ljuska ga daje,
 * a prozori i polja ga zovu jer su prozori posebni prozori sistema pa dodiri ne prolaze kroz ljusku.
 */
val LocalDodir = staticCompositionLocalOf<() -> Unit> { {} }

/** Hvata svaki dodir (pass Initial, bez trošenja) i javlja ga. */
fun Modifier.hvataDodir(onDodir: () -> Unit): Modifier = pointerInput(onDodir) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial)
            onDodir()
        }
    }
}
