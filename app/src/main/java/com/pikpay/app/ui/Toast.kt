package com.pikpay.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pikpay.app.R
import com.pikpay.app.ui.theme.Success
import com.pikpay.app.ui.theme.TextPrimary

@Composable
fun Toast(visible: Boolean, text: String) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(350)) { it * 2 },
        exit = slideOutVertically(tween(350)) { it * 2 }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(TextPrimary, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(painterResource(R.drawable.ic_check_circle), null, tint = Success, modifier = Modifier.size(19.dp))
            Text(text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(start = 10.dp))
        }
    }
}
