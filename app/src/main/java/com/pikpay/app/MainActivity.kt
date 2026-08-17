package com.pikpay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.pikpay.app.ui.MainScreen
import com.pikpay.app.ui.OnboardingScreen
import com.pikpay.app.ui.theme.Bg
import com.pikpay.app.ui.theme.PikPayTheme

// iOS-style ease-out: the incoming screen decelerates into place while the
// outgoing one drifts left with a touch of parallax.
private val ScreenPushEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)
private const val ScreenPushMs = 320

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PikPayTheme {
                PikPayApp()
            }
        }
    }
}

private enum class AppScreen { ONBOARDING, MAIN }

@Composable
private fun PikPayApp() {
    var screen by remember { mutableStateOf(AppScreen.ONBOARDING) }

    // progress 0f = fully on ONBOARDING, 1f = fully settled on MAIN.
    val progress = remember { Animatable(0f) }

    LaunchedEffect(screen) {
        if (screen == AppScreen.MAIN) {
            progress.animateTo(1f, animationSpec = tween(ScreenPushMs, easing = ScreenPushEasing))
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        val w = constraints.maxWidth.toFloat()
        // Outgoing screen drifts left with a subtle parallax (iOS push feel)
        // instead of just vanishing.
        val parallax = 0.28f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .graphicsLayer { translationX = -progress.value * parallax * w }
        ) {
            OnboardingScreen(onFinished = { screen = AppScreen.MAIN })
        }

        // Composed unconditionally from the start, parked off-screen at
        // progress = 0, so its first layout/draw happens up front instead
        // of bursting onto the same frame the push animation begins on.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .graphicsLayer { translationX = (1f - progress.value) * w }
        ) {
            MainScreen()
        }
    }
}
