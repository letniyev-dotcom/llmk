package com.pikpay.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pikpay.app.R
import com.pikpay.app.ui.theme.TextSecondary
import com.pikpay.app.ui.theme.TextTertiary

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 40.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(R.drawable.ic_inbox),
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Text("Здесь пока пусто", fontSize = 14.sp, color = TextSecondary)
    }
}
