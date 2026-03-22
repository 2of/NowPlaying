package com.noahk.NowPlayingCarView

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    isFocused: Boolean = false,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    // Snappy spring for immediate press feedback, bouncier for release
    val pressSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    val pressDpSpec: FiniteAnimationSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    val releaseSpec: FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec()
    val releaseDpSpec: FiniteAnimationSpec<Dp> = MaterialTheme.motionScheme.fastSpatialSpec()
    val effectColor: FiniteAnimationSpec<Color> = MaterialTheme.motionScheme.fastEffectsSpec()

    val floatSpec = if (pressed) pressSpec else releaseSpec
    val dpSpec = if (pressed) pressDpSpec else releaseDpSpec

    // Corner: sharp when focused, round when idle, mid when pressed
    val animatedCorner by animateDpAsState(
        targetValue = when {
            pressed   -> 16.dp
            isFocused -> 8.dp
            else      -> 32.dp
        },
        animationSpec = dpSpec,
        label = "corner"
    )

    // Elevation breathes up on press
    val animatedElevation by animateDpAsState(
        targetValue = if (pressed) 12.dp else 0.dp,
        animationSpec = dpSpec,
        label = "elevation"
    )

    // Slight lift on press
    val translateY by animateFloatAsState(
        targetValue = if (pressed) -8f else 0f,
        animationSpec = floatSpec,
        label = "translateY"
    )

    // Scale punch
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = floatSpec,
        label = "scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = effectColor,
        label = "color"
    )

    Surface(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationY = translateY,
                shadowElevation = animatedElevation.value
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onClick()
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            },
        shape = RoundedCornerShape(animatedCorner),
        color = animatedColor,
        contentColor = contentColor,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content(pressed)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PopIcon — Snappy spring bounce on the icon inside a button
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PopIcon(
    isPressed: Boolean,
    peakScale: Float = 1.35f,
    content: @Composable () -> Unit
) {
    // Snap down fast, bounce back slow
    val pressSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    val releaseSpec: FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec()
    val spec = if (isPressed) pressSpec else releaseSpec

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = spec,
        label = "pop_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -15f else 0f,
        animationSpec = spec,
        label = "pop_rotation"
    )

    Box(
        modifier = Modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            rotationZ = rotation
        )
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpressiveLabel — M3 Expressive uppercase tracking label
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLabel(
    text: String,
    color: Color = Color.Unspecified,
    isPressed: Boolean = false
) {
    val spatialFloat: FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec()
    val effectFloat: FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastEffectsSpec()

    val offsetY by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = spatialFloat,
        label = "label_offset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.5f else 1f,
        animationSpec = effectFloat,
        label = "label_alpha"
    )

    val finalColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp,
        color = finalColor.copy(alpha = alpha),
        modifier = Modifier.graphicsLayer(translationY = offsetY)
    )
}