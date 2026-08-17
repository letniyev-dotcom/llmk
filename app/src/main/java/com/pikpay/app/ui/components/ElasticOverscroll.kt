package com.pikpay.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.math.tanh
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// iOS rubber-band coefficient: the first pixel of drag past the edge
// produces 0.55px of overscroll, then damping increases quadratically.
private const val RubberBandC = 0.55f

/**
 * iOS-style elastic translation overscroll for Compose. When a scrollable
 * hits its edge and the user keeps dragging, the leftover delta accumulates
 * into a per-axis [MutableFloatState] with rubber-band damping, then springs
 * back to 0 on release. Vertical and horizontal axes are independent.
 */
class ElasticOverscrollState internal constructor(
    val verticalOverscroll: MutableFloatState,
    val horizontalOverscroll: MutableFloatState,
    val connection: NestedScrollConnection,
    val lastFlingVelocityY: MutableFloatState,
    val lastFlingVelocityX: MutableFloatState,
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun rememberElasticOverscroll(
    maxVertical: Dp = 64.dp,
    maxHorizontal: Dp = 48.dp,
): ElasticOverscrollState {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val maxV = with(density) { maxVertical.toPx() }
    val maxH = with(density) { maxHorizontal.toPx() }
    val verticalOverscrollY = remember { mutableFloatStateOf(0f) }
    val horizontalOverscrollX = remember { mutableFloatStateOf(0f) }
    val flingVelocityY = remember { mutableFloatStateOf(0f) }
    val flingVelocityX = remember { mutableFloatStateOf(0f) }
    val connection = remember(maxV, maxH) {
        ElasticOverscrollConnection(
            verticalOverscroll = verticalOverscrollY,
            horizontalOverscroll = horizontalOverscrollX,
            lastFlingVelocityY = flingVelocityY,
            lastFlingVelocityX = flingVelocityX,
            maxVerticalPx = maxV,
            maxHorizontalPx = maxH,
            scope = scope,
        )
    }
    return remember(connection) {
        ElasticOverscrollState(
            verticalOverscrollY,
            horizontalOverscrollX,
            connection,
            flingVelocityY,
            flingVelocityX,
        )
    }
}

/**
 * Wraps [content] in a [Box] with the null system overscroll, the elastic
 * [NestedScrollConnection], and the resulting translation applied to the
 * whole subtree. Use this when the ENTIRE screen content should slide
 * together; for screens with pinned UI, use [rememberElasticOverscroll]
 * directly and apply the translation selectively.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ElasticOverscroll(
    modifier: Modifier = Modifier,
    maxVertical: Dp = 64.dp,
    maxHorizontal: Dp = 48.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val elastic = rememberElasticOverscroll(maxVertical, maxHorizontal)
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(
            modifier = modifier
                .nestedScroll(elastic.connection)
                .graphicsLayer {
                    translationY = elastic.verticalOverscroll.floatValue
                    translationX = elastic.horizontalOverscroll.floatValue
                },
            content = content,
        )
    }
}

internal class ElasticOverscrollConnection(
    private val verticalOverscroll: MutableFloatState,
    private val horizontalOverscroll: MutableFloatState,
    private val lastFlingVelocityY: MutableFloatState,
    private val lastFlingVelocityX: MutableFloatState,
    private val maxVerticalPx: Float,
    private val maxHorizontalPx: Float,
    private val scope: CoroutineScope,
) : NestedScrollConnection {

    private var verticalJob: Job? = null
    private var horizontalJob: Job? = null

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.Drag) return Offset.Zero
        if (available.y != 0f && verticalOverscroll.floatValue != 0f) cancelVertical()
        if (available.x != 0f && horizontalOverscroll.floatValue != 0f) cancelHorizontal()
        // The scrollable always gets first look at the full delta so content
        // keeps tracking the finger 1:1 whenever it has room to scroll. The
        // overscroll register only shrinks reactively in onPostScroll, based
        // on what the scrollable actually consumed.
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source != NestedScrollSource.Drag) return Offset.Zero
        shrinkAxis(horizontalOverscroll, consumed.x)
        shrinkAxis(verticalOverscroll, consumed.y)
        val ax = applyAxis(horizontalOverscroll, available.x, maxHorizontalPx)
        val ay = applyAxis(verticalOverscroll, available.y, maxVerticalPx)
        return Offset(ax, ay)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        lastFlingVelocityY.floatValue = available.y
        lastFlingVelocityX.floatValue = available.x
        val springY = verticalOverscroll.floatValue != 0f
        val springX = horizontalOverscroll.floatValue != 0f
        if (springY) {
            verticalJob = scope.launch { springBack(verticalOverscroll, available.y, maxVerticalPx) }
        }
        if (springX) {
            horizontalJob = scope.launch { springBack(horizontalOverscroll, available.x, maxHorizontalPx) }
        }
        return Velocity(
            x = if (springX) available.x else 0f,
            y = if (springY) available.y else 0f,
        )
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity {
        // [available] is leftover fling velocity the scrollable couldn't
        // consume at its edge — convert it into the same elastic bounce a
        // slow drag past the edge produces.
        var consumedY = 0f
        var consumedX = 0f
        if (available.y != 0f) {
            cancelVertical()
            verticalJob = scope.launch { flingBounce(verticalOverscroll, available.y, maxVerticalPx) }
            consumedY = available.y
        } else if (verticalOverscroll.floatValue != 0f && verticalJob?.isActive != true) {
            verticalJob = scope.launch { springBack(verticalOverscroll, 0f, maxVerticalPx) }
        }
        if (available.x != 0f) {
            cancelHorizontal()
            horizontalJob = scope.launch { flingBounce(horizontalOverscroll, available.x, maxHorizontalPx) }
            consumedX = available.x
        } else if (horizontalOverscroll.floatValue != 0f && horizontalJob?.isActive != true) {
            horizontalJob = scope.launch { springBack(horizontalOverscroll, 0f, maxHorizontalPx) }
        }
        return Velocity(consumedX, consumedY)
    }

    /**
     * Elastic edge bounce driven by leftover fling velocity (px/s): a
     * critically-damped spring released from rest with that velocity
     * travels out to a single peak and glides back with no oscillation.
     * Velocity is pre-saturated with tanh so the peak smoothly approaches
     * the cap instead of overshooting and getting hard-clamped mid-motion.
     */
    private suspend fun flingBounce(
        state: MutableFloatState,
        velocity: Float,
        maxPx: Float,
    ) {
        if (velocity == 0f || maxPx <= 0f) return
        val stiffness = 220f
        val vCap = maxPx * sqrt(stiffness) * 2.718281828f
        val v = vCap * tanh(0.4f * velocity / vCap)
        val animatable = Animatable(state.floatValue)
        try {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    stiffness = stiffness,
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    visibilityThreshold = 0.5f,
                ),
                initialVelocity = v,
            ) {
                state.floatValue = value.coerceIn(-maxPx, maxPx)
            }
            state.floatValue = 0f
        } catch (_: CancellationException) {
            // Re-grabbed mid-bounce — leave the value where it is.
        }
    }

    private fun cancelVertical() {
        verticalJob?.cancel()
    }

    private fun cancelHorizontal() {
        horizontalJob?.cancel()
    }

    private fun shrinkAxis(state: MutableFloatState, consumedDelta: Float) {
        if (consumedDelta == 0f) return
        val current = state.floatValue
        if (current == 0f) return
        if (sign(consumedDelta) == sign(current)) return
        val newVal = (current + consumedDelta).let { if (sign(it) != sign(current)) 0f else it }
        state.floatValue = newVal
    }

    /**
     * Rolls a delta the scrollable couldn't consume (at an edge) into the
     * overscroll with rubber-band damping:
     *   d = c · (1 − |current|/maxPx)^2,   c ≈ 0.55
     */
    private fun applyAxis(state: MutableFloatState, available: Float, maxPx: Float): Float {
        if (available == 0f || maxPx <= 0f) return 0f
        val current = state.floatValue
        val ratio = (abs(current) / maxPx).coerceIn(0f, 1f)
        val damping = RubberBandC * (1f - ratio) * (1f - ratio)
        val delta = available * damping
        val newVal = (current + delta).coerceIn(-maxPx, maxPx)
        state.floatValue = newVal
        return available
    }

    private suspend fun springBack(
        state: MutableFloatState,
        initialVelocity: Float,
        maxPx: Float,
    ) {
        val startValue = state.floatValue
        if (startValue == 0f) return
        // Damp the lift-off velocity through the same rubber-band curve used
        // for displacement, scaled by how far the content is pulled — keeps
        // a small pull's release gentle and a near-cap pull's release
        // confident, without feeding a raw multi-thousand-px/s finger
        // velocity straight into the spring solver.
        val ratio = (abs(startValue) / maxPx).coerceIn(0f, 1f)
        val velocityScale = RubberBandC * (1f - ratio) * (1f - ratio)
        val dampedVelocity = initialVelocity * velocityScale * ratio
        val animatable = Animatable(startValue)
        try {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    stiffness = 230f,
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    visibilityThreshold = 0.25f,
                ),
                initialVelocity = dampedVelocity,
            ) {
                state.floatValue = value.coerceIn(-maxPx, maxPx)
            }
            state.floatValue = 0f
        } catch (_: CancellationException) {
            // Re-grabbed mid-spring — leave the value where it is.
        }
    }
}
