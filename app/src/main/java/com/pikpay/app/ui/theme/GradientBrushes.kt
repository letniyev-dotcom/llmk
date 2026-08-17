package com.pikpay.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

// Coral → pink, the brand accent gradient. Used anywhere the flat Accent
// color used to sit — CTA buttons, the segmented control indicator, the
// logo — so the whole app reads as one consistent gradient identity.
val AccentGradient = Brush.linearGradient(
    colors = listOf(AccentGradientStart, AccentGradientEnd),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)
