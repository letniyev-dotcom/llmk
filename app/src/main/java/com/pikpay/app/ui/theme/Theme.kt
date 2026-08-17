package com.pikpay.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PikPayColorScheme = lightColorScheme(
    primary = Accent,
    background = Bg,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorColor,
)

@Composable
fun PikPayTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as android.app.Activity).window
        // Edge-to-edge is enabled in MainActivity — don't also paint an opaque
        // status bar color here, or it shows as a solid strip on top of the
        // app's own translucent header background. Just set icon contrast.
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }
    MaterialTheme(
        colorScheme = PikPayColorScheme,
        typography = PikPayTypography,
        content = content
    )
}
