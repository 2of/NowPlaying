package com.noahk.NowPlayingCarView

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowPlayingPanel(track: TrackInfo?, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = track,
            transitionSpec = {
                (slideInVertically { it / 4 } + fadeIn(tween(400)))
                    .togetherWith(slideOutVertically { -it / 4 } + fadeOut(tween(250)))
            },
            contentKey = { it?.title.orEmpty() + (it?.artist.orEmpty()) },
            label = "track_transition"
        ) { currentTrack ->
            if (currentTrack != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    // ── 1. Top Context Tags ──
                    Column(
                        horizontalAlignment = Alignment.End, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (currentTrack.sourceApp.isNotBlank()) {
                            Text(
                                text = "PLAYING FROM ${currentTrack.sourceApp.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── 2. Flexible Hero Art ──
                    // Takes up all available space between the top tags and bottom controls
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 24.dp), // Breathing room around the art
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (currentTrack.albumArt != null) {
                            Image(
                                bitmap = currentTrack.albumArt.asImageBitmap(),
                                contentDescription = "Album art for ${currentTrack.album}",
                                modifier = Modifier
                                    .fillMaxHeight() // Maximize vertical space first
                                    .aspectRatio(1f) // Constrain to a perfect square
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // ── 3. Bottom Track Info & Controls ──
                    // Tightly grouped together for a cohesive control cluster
                    Column(
                        horizontalAlignment = Alignment.End, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        
                        // Artist & Album
                        Text(
                            text = buildString {
                                append(currentTrack.artist.uppercase())
                                if (currentTrack.album.isNotBlank()) append(" • ${currentTrack.album.uppercase()}")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Progress Section
                        if (currentTrack.duration > 0L) {
                            val progress = (currentTrack.position.toFloat() / currentTrack.duration.toFloat())
                                .coerceIn(0f, 1f)

                            val animatedProgress by animateFloatAsState(
                                targetValue = progress,
                                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                                label = "progress"
                            )

                            LinearWavyProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(currentTrack.position),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTime(currentTrack.duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Up Next Section (only add spacing if it exists)
                        if (currentTrack.nextTrack.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "UP NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentTrack.nextTrack,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // ── Nothing playing state ──
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Nothing\nplaying.",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Play something\nto get started",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Format milliseconds as m:ss */
private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}