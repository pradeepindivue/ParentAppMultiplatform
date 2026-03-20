package com.edmik.parentapp.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ─── Spring presets matching the video ───────────────────────────────────────
//
// Video: one upward overshoot then settles — medium bounce.
// dampingRatio = 0.65f → slightly underdamped → visible single overshoot.
// StiffnessMediumLow → weighty arrival, not too snappy.

/** Spring used for the card slide (full-screen height travel). */
val CardEntrySpring: SpringSpec<Float> = spring(
    dampingRatio = 0.65f,
    stiffness = Spring.StiffnessMedium
)

/** Spring used for per-element bounce inside the card (Dp for animateDpAsState). */
private val ElementBounceSpring: SpringSpec<Dp> = spring(
    dampingRatio = 0.7f,
    stiffness = Spring.StiffnessMediumLow
)

// ─── Staggered Fade + Spring Bounce slide ────────────────────────────────────
/**
 * Applies a fade-in + spring-bounce slide-up to [content], delayed by [delayMillis].
 *
 * **Why graphicsLayer instead of AnimatedVisibility + slideInVertically?**
 * Content inside a `verticalScroll` Column is clipped to the scroll container's
 * bounds. `slideInVertically` starts the element outside those bounds and the
 * frame is clipped — only the fade shows. Using `graphicsLayer { translationY }`
 * is a *visual-only* transform that bypasses layout clipping, so both the slide
 * and the spring overshoot are fully visible regardless of the scroll container.
 *
 * The element always occupies its layout space (no layout jump); it just renders
 * translated + faded until the spring settles.
 */
@Composable
fun StaggeredFadeSlide(
    masterVisible: Boolean,
    delayMillis: Int,
    slideDistanceDp: Dp = 30.dp,
    content: @Composable () -> Unit,
) {
    // Per-element gate, fired after the stagger delay
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(masterVisible) {
        if (masterVisible) {
            delay(delayMillis.toLong())
            triggered = true
        } else {
            triggered = false
        }
    }

    val density = LocalDensity.current

    // Fade: simple tween (spring looks bad for alpha)
    val alpha by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "elementAlpha"
    )

    // Slide: spring from +slideDistance → 0, with natural bounce overshoot
    val translationYDp by animateDpAsState(
        targetValue = if (triggered) 0.dp else slideDistanceDp,
        animationSpec = ElementBounceSpring,
        label = "elementSlide"
    )

    val translationYPx = with(density) { translationYDp.toPx() }

    // graphicsLayer: visual-only transform, NOT clipped by parent layout
    val modifier = Modifier.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationYPx
    }

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

// ─── Press-scale Button Effect ───────────────────────────────────────────────
/**
 * Modifier that slightly scales down a composable while pressed,
 * giving a tactile "click" feel without bounce.
 */
@Composable
fun Modifier.pressClickEffect(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "pressScale"
    )
    return this.scale(scale)
}

// ─── Focus Scale Effect ──────────────────────────────────────────────────────
/**
 * Modifier that subtly enlarges a TextField when it has focus.
 */
@Composable
fun Modifier.focusScaleEffect(
    isFocused: Boolean,
    focusedScale: Float = 1.01f,
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "focusScale"
    )
    return this.scale(scale)
}

// ─── Error Shake Effect ──────────────────────────────────────────────────────
/**
 * Modifier that applies a short horizontal shake when [trigger] changes
 * to a non-null value — draws attention to an inline error message.
 */
@Composable
fun Modifier.errorShake(
    trigger: Any?,
    shakeOffset: Dp = 6.dp,
): Modifier {
    val density = LocalDensity.current
    val shakeOffsetPx = with(density) { shakeOffset.toPx() }
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger != null) {
            repeat(3) {
                translationX.animateTo(shakeOffsetPx, tween(50, easing = FastOutSlowInEasing))
                translationX.animateTo(-shakeOffsetPx, tween(50, easing = FastOutSlowInEasing))
            }
            translationX.animateTo(0f, tween(50, easing = FastOutSlowInEasing))
        }
    }

    return this.graphicsLayer { this.translationX = translationX.value }
}
