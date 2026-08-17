package com.pikpay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pikpay.app.R
import com.pikpay.app.model.Order
import com.pikpay.app.model.OrderStatus
import com.pikpay.app.ui.theme.ErrorColor
import com.pikpay.app.ui.theme.ErrorTint
import com.pikpay.app.ui.theme.HintDivider
import com.pikpay.app.ui.theme.Pending
import com.pikpay.app.ui.theme.PendingTint
import com.pikpay.app.ui.theme.AccentGradient
import com.pikpay.app.ui.theme.Success
import com.pikpay.app.ui.theme.SuccessTint
import com.pikpay.app.ui.theme.Surface
import com.pikpay.app.ui.theme.TextPrimary
import com.pikpay.app.ui.theme.TextSecondary
import com.pikpay.app.ui.theme.TextTertiary
import java.text.NumberFormat
import java.util.Locale

private val ruFormat = NumberFormat.getInstance(Locale("ru", "RU"))

@Composable
fun OrderCard(
    order: Order,
    onConfirm: (Int) -> Unit,
    onReject: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            val amountColor = when (order.status) {
                OrderStatus.SUCCESS -> Success
                OrderStatus.ERROR -> TextTertiary
                OrderStatus.PENDING -> TextPrimary
            }
            Text(
                text = "+${ruFormat.format(order.amount)} ₽",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                textDecoration = if (order.status == OrderStatus.ERROR) TextDecoration.LineThrough else null
            )
            StatusPill(order.status)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text("Заказ №${order.id}", fontSize = 13.sp, color = TextSecondary)
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(3.dp)
                    .background(TextTertiary, CircleShape)
            )
            Text(order.time, fontSize = 13.sp, color = TextSecondary)
        }

        OrderHint(order)

        if (order.status == OrderStatus.PENDING) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                Button(
                    onClick = { onConfirm(order.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AccentGradient),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_check_circle), null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text("Подтвердить", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 5.dp))
                }
                Button(
                    onClick = { onReject(order.id) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorTint),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_close_circle), null, tint = ErrorColor, modifier = Modifier.size(15.dp))
                    Text("Отменить", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ErrorColor, modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderHint(order: Order) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(1.dp)
            .background(HintDivider)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        when {
            order.status == OrderStatus.SUCCESS && order.auto -> {
                Icon(painterResource(R.drawable.ic_magic_wand), null, tint = Success, modifier = Modifier.size(15.dp))
                Text("Найдено автоматически", fontSize = 12.5.sp, color = TextSecondary, modifier = Modifier.padding(start = 7.dp))
            }
            order.status == OrderStatus.ERROR -> {
                Icon(painterResource(R.drawable.ic_danger_triangle), null, tint = ErrorColor, modifier = Modifier.size(15.dp))
                Text("Перевод не обнаружен за 15 минут", fontSize = 12.5.sp, color = TextSecondary, modifier = Modifier.padding(start = 7.dp))
            }
            order.status == OrderStatus.PENDING -> {
                RingSpinner(color = TextSecondary)
                Text("Ищем перевод", fontSize = 12.5.sp, color = TextSecondary, modifier = Modifier.padding(start = 7.dp))
            }
            order.status == OrderStatus.SUCCESS -> {
                Icon(painterResource(R.drawable.ic_check), null, tint = Success, modifier = Modifier.size(15.dp))
                Text("Подтверждено вручную", fontSize = 12.5.sp, color = TextSecondary, modifier = Modifier.padding(start = 7.dp))
            }
        }
    }
}

@Composable
private fun StatusPill(status: OrderStatus) {
    val (bg, fg, label) = when (status) {
        OrderStatus.SUCCESS -> Triple(SuccessTint, Success, "Подтверждена")
        OrderStatus.PENDING -> Triple(PendingTint, Pending, "Не подтверждена")
        OrderStatus.ERROR -> Triple(ErrorTint, ErrorColor, "Не найдена")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        when (status) {
            OrderStatus.PENDING -> RingSpinner(color = fg)
            OrderStatus.SUCCESS -> Icon(painterResource(R.drawable.ic_check_circle), null, tint = fg, modifier = Modifier.size(14.dp))
            OrderStatus.ERROR -> Icon(painterResource(R.drawable.ic_close_circle), null, tint = fg, modifier = Modifier.size(14.dp))
        }
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(start = 6.dp))
    }
}
