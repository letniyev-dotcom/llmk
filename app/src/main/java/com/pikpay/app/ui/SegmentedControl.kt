package com.pikpay.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pikpay.app.model.OrderTab
import com.pikpay.app.ui.theme.Accent
import com.pikpay.app.ui.theme.Surface
import com.pikpay.app.ui.theme.TextSecondary

private val IndicatorSpring = spring<Dp>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

private data class TabBounds(val offsetX: Dp, val width: Dp, val height: Dp)

@Composable
fun SegmentedControl(
    selected: OrderTab,
    counts: Map<OrderTab, Int>,
    onSelect: (OrderTab) -> Unit
) {
    val density = LocalDensity.current
    val tabBounds = remember { mutableStateMapOf<OrderTab, TabBounds>() }
    val selectedBounds = tabBounds[selected]

    val indicatorOffset by animateDpAsState(
        targetValue = selectedBounds?.offsetX ?: 0.dp,
        animationSpec = IndicatorSpring,
        label = "indicatorOffset"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = selectedBounds?.width ?: 0.dp,
        animationSpec = IndicatorSpring,
        label = "indicatorWidth"
    )
    val indicatorHeight by animateDpAsState(
        targetValue = selectedBounds?.height ?: 0.dp,
        animationSpec = IndicatorSpring,
        label = "indicatorHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        if (selectedBounds != null) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .background(Accent, RoundedCornerShape(11.dp))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OrderTab.entries.forEach { tab ->
                val active = tab == selected
                val textColor by animateColorAsState(
                    targetValue = if (active) Color.White else TextSecondary,
                    label = "tabTextColor"
                )
                val countColor by animateColorAsState(
                    targetValue = if (active) Color.White.copy(alpha = 0.55f) else TextSecondary.copy(alpha = 0.6f),
                    label = "tabCountColor"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInParent()
                            with(density) {
                                tabBounds[tab] = TabBounds(
                                    offsetX = position.x.toDp(),
                                    width = coordinates.size.width.toDp(),
                                    height = coordinates.size.height.toDp()
                                )
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(tab) }
                        .padding(vertical = 9.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = " ${counts[tab] ?: 0}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = countColor
                    )
                }
            }
        }
    }
}
