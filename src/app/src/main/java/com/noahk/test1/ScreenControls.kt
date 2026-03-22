package com.noahk.NowPlayingCarView

import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenControls(
    modifier: Modifier = Modifier
) {
    var keepAwake by remember { mutableStateOf(false) }
    var dimScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(keepAwake) {
        val window = (context as? android.app.Activity)?.window
        if (keepAwake) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    DisposableEffect(dimScreen) {
        val window = (context as? android.app.Activity)?.window
        val params = window?.attributes
        if (params != null) {
            params.screenBrightness = if (dimScreen) 0.05f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = params
        }
        onDispose {
            val p = window?.attributes
            if (p != null) {
                p.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = p
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ToggleChip(
            label = "KEEP AWAKE",
            active = keepAwake,
            activeColor = MaterialTheme.colorScheme.tertiary,
            inactiveColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.weight(1f)
        ) { keepAwake = !keepAwake }

        ToggleChip(
            label = "DIM",
            active = dimScreen,
            activeColor = MaterialTheme.colorScheme.secondary,
            inactiveColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.weight(1f)
        ) { dimScreen = !dimScreen }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleChip(
    label: String,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val effectColor: FiniteAnimationSpec<Color> = MaterialTheme.motionScheme.fastEffectsSpec()

    val onActive = MaterialTheme.colorScheme.onTertiary
    val onInactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val bgColor by animateColorAsState(
        targetValue = if (active) activeColor else inactiveColor,
        animationSpec = effectColor,
        label = "chip_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (active) onActive else onInactive,
        animationSpec = effectColor,
        label = "text_color"
    )

    MorphButton(
        onClick = onToggle,
        modifier = modifier.height(44.dp),
        containerColor = bgColor,
        contentColor = textColor,
        isFocused = active
    ) { _ ->
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = textColor
        )
    }
}