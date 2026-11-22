package dev.nextpixel.cafebar919.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SemaBoja = darkColorScheme(
    primary = Boje.primary,
    onPrimary = Boje.onPrimary,
    primaryContainer = Boje.primaryTint,
    onPrimaryContainer = Boje.ink,
    background = Boje.bg,
    onBackground = Boje.ink,
    surface = Boje.surface,
    onSurface = Boje.ink,
    surfaceVariant = Boje.tile,
    onSurfaceVariant = Boje.muted,
    outline = Boje.neutralLine,
    outlineVariant = Boje.line,
    error = Boje.danger,
    errorContainer = Boje.dangerTint,
)

@Composable
fun CafeBarTema(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SemaBoja, content = content)
}
