package com.noahk.NowPlayingCarView

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ControlsPanel(
    isPlaying: Boolean,
    hasTrack: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focusedButton by remember { mutableIntStateOf(0) }

    val spatialSpec: FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.defaultSpatialSpec()

    val prevWeight by animateFloatAsState(
        targetValue = when (focusedButton) {
            2 -> 3f
            3 -> 0.5f
            else -> 1f
        },
        animationSpec = spatialSpec,
        label = "prevWeight"
    )

    val nextWeight by animateFloatAsState(
        targetValue = when (focusedButton) {
            3 -> 4f
            2 -> 0.8f
            else -> 2f
        },
        animationSpec = spatialSpec,
        label = "nextWeight"
    )

    // Material You colors
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Play / Pause — top button
        MorphButton(
            onClick = { focusedButton = 1; onPlayPause() },
            isFocused = focusedButton == 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            containerColor = if (isPlaying) primary else surfaceVariant,
            contentColor = if (isPlaying) onPrimary else onSurfaceVariant
        ) { isPressed ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PopIcon(isPressed) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
                ExpressiveLabel(
                    text = when {
                        !hasTrack -> "NOTHING QUEUED"
                        isPlaying -> "PAUSE"
                        else      -> "PLAY"
                    },
                    color = if (isPlaying) onPrimary else onSurfaceVariant
                )
            }
        }

        // Prev / Next row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MorphButton(
                onClick = { focusedButton = 2; onPrev() },
                isFocused = focusedButton == 2,
                modifier = Modifier
                    .weight(prevWeight)
                    .fillMaxHeight(),
                containerColor = surfaceContainerHigh,
                contentColor = onSurface
            ) { isPressed ->
                PopIcon(isPressed) {
                    Icon(Icons.Default.SkipPrevious, null, Modifier.size(28.dp))
                }
            }

            MorphButton(
                onClick = { focusedButton = 3; onNext() },
                isFocused = focusedButton == 3,
                modifier = Modifier
                    .weight(nextWeight)
                    .fillMaxHeight(),
                containerColor = surfaceContainerHigh,
                contentColor = onSurface
            ) { isPressed ->
                PopIcon(isPressed) {
                    Icon(Icons.Default.SkipNext, null, Modifier.size(28.dp))
                }
            }
        }
    }

    LaunchedEffect(focusedButton) {
        if (focusedButton != 0) {
            delay(1000)
            focusedButton = 0
        }
    }
}