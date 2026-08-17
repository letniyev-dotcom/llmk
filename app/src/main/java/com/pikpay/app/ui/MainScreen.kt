package com.pikpay.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pikpay.app.model.Order
import com.pikpay.app.model.OrderStatus
import com.pikpay.app.model.OrderTab
import com.pikpay.app.model.sampleOrders
import com.pikpay.app.ui.components.rememberElasticOverscroll
import com.pikpay.app.ui.theme.Bg
import com.pikpay.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Reasonable starting guess for the header's height (status bar + title +
// tabs) so the list already has correct top padding on the very first frame,
// before the real measurement below refines it. Prevents any overlap flash.
private val HeaderHeightFallback: Dp = 150.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    var orders by remember { mutableStateOf(sampleOrders()) }
    var activeTab by remember { mutableStateOf(OrderTab.ALL) }
    var toastText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun updateOrder(id: Int, transform: (Order) -> Order) {
        orders = orders.map { if (it.id == id) transform(it) else it }
    }

    fun showToast(text: String) {
        toastText = text
        scope.launch {
            delay(2200)
            toastText = null
        }
    }

    val counts = mapOf(
        OrderTab.ALL to orders.size,
        OrderTab.PENDING to orders.count { it.status == OrderStatus.PENDING },
        OrderTab.SUCCESS to orders.count { it.status == OrderStatus.SUCCESS },
        OrderTab.ERROR to orders.count { it.status == OrderStatus.ERROR }
    )
    val filtered = when (activeTab) {
        OrderTab.ALL -> orders
        OrderTab.PENDING -> orders.filter { it.status == OrderStatus.PENDING }
        OrderTab.SUCCESS -> orders.filter { it.status == OrderStatus.SUCCESS }
        OrderTab.ERROR -> orders.filter { it.status == OrderStatus.ERROR }
    }

    val density = LocalDensity.current
    var headerHeightDp by remember { mutableStateOf(HeaderHeightFallback) }

    // Translucent panel behind the header (matches the html reference's
    // rgba(bg, .55) + mask-gradient) — needs real content sliding underneath
    // it to actually read as a fade, which is why the header overlays the
    // list below instead of sitting above it in the normal flow.
    val headerFadeBrush = Brush.verticalGradient(
        0f to Bg.copy(alpha = 0.55f),
        0.42f to Bg.copy(alpha = 0.55f),
        0.78f to Bg.copy(alpha = 0f),
        1f to Bg.copy(alpha = 0f)
    )

    // Custom iOS-style elastic overscroll (translation + rubber-band spring
    // back) replaces the stock Android stretch/glow effect everywhere the
    // order list scrolls.
    val elastic = rememberElasticOverscroll()

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        // ---- scrollable content, sits underneath the sticky header ----
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = headerHeightDp)) {
                EmptyState()
            }
        } else {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(elastic.connection)
                        .graphicsLayer {
                            translationY = elastic.verticalOverscroll.floatValue
                        },
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = headerHeightDp + 4.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onConfirm = { id ->
                                updateOrder(id) { it.copy(status = OrderStatus.SUCCESS, auto = false) }
                                showToast("Заказ №$id подтверждён")
                            },
                            onReject = { id ->
                                updateOrder(id) { it.copy(status = OrderStatus.ERROR) }
                            }
                        )
                    }
                }
            }
        }

        // ---- sticky fading header, drawn on top of the list ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                // Measured OUTERMOST (before background/insets/padding) so the
                // reported size includes the full header — status bar inset,
                // title, tabs and bottom padding — not just its inner content.
                .onGloballyPositioned { coordinates ->
                    val measuredDp = with(density) { coordinates.size.height.toDp() }
                    if (measuredDp != headerHeightDp) headerHeightDp = measuredDp
                }
                .background(headerFadeBrush)
                .statusBarsPadding()
                .padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 14.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("ПикПэй", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                SegmentedControl(selected = activeTab, counts = counts, onSelect = { activeTab = it })
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .align(Alignment.BottomCenter)
        ) {
            Toast(visible = toastText != null, text = toastText ?: "")
        }
    }
}
