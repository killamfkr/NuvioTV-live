package com.nuvio.tv.ui.screens.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class DragMode {
    BRIGHTNESS,
    VOLUME,
}

private sealed class GestureFeedback {
    data class Seek(val forward: Boolean) : GestureFeedback()
    data class BrightnessLevel(val fraction: Float) : GestureFeedback()
    data class VolumeLevel(val fraction: Float) : GestureFeedback()
}

/**
 * Fullscreen phone/tablet overlays: double-tap left/right to skip 15s, vertical drag on the
 * left/right edges for brightness and media volume.
 */
@Composable
fun HandheldPlayerVideoGestureLayer(
    onSingleTap: () -> Unit,
    onSeekByMs: (Long) -> Unit,
    skipEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    var feedback by remember { mutableStateOf<GestureFeedback?>(null) }
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(750)
            feedback = null
        }
    }

    BoxWithConstraints(modifier = modifier) {
        GestureFeedbackOverlay(feedback = feedback, modifier = Modifier.fillMaxSize())

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(skipEnabled) {
                    detectTapGestures(
                        onTap = { onSingleTap() },
                        onDoubleTap = { offset ->
                            if (!skipEnabled) return@detectTapGestures
                            val forward = offset.x >= size.width / 2f
                            val delta = if (forward) {
                                PlayerScrubRates.STEP_HANDHELD_DOUBLE_TAP_MS
                            } else {
                                -PlayerScrubRates.STEP_HANDHELD_DOUBLE_TAP_MS
                            }
                            onSeekByMs(delta)
                            feedback = GestureFeedback.Seek(forward = forward)
                        },
                    )
                }
                .pointerInput(activity, audioManager) {
                    var dragMode: DragMode? = null
                    var dragAccumulatedPx = 0f
                    var brightnessStart = 0.5f
                    var volumeStart = 0

                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            dragAccumulatedPx = 0f
                            val leftEdgeMaxX = size.width * 0.38f
                            val rightEdgeMinX = size.width * 0.62f
                            dragMode = when {
                                offset.x <= leftEdgeMaxX -> DragMode.BRIGHTNESS
                                offset.x >= rightEdgeMinX -> DragMode.VOLUME
                                else -> null
                            }
                            when (dragMode) {
                                DragMode.BRIGHTNESS -> {
                                    val current = activity?.window?.attributes?.screenBrightness ?: -1f
                                    brightnessStart = if (current in 0f..1f) current else 0.5f
                                }
                                DragMode.VOLUME -> {
                                    volumeStart = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                }
                                null -> Unit
                            }
                        },
                        onDragEnd = { dragMode = null },
                        onDragCancel = { dragMode = null },
                        onVerticalDrag = { change, dragAmount ->
                            val mode = dragMode ?: return@detectVerticalDragGestures
                            change.consume()
                            dragAccumulatedPx += dragAmount
                            val travel = dragAccumulatedPx / size.height
                            when (mode) {
                                DragMode.BRIGHTNESS -> {
                                    val act = activity ?: return@detectVerticalDragGestures
                                    val newBrightness = (brightnessStart - travel * 1.25f)
                                        .coerceIn(0.05f, 1f)
                                    val params = act.window.attributes
                                    params.screenBrightness = newBrightness
                                    act.window.attributes = params
                                    feedback = GestureFeedback.BrightnessLevel(newBrightness)
                                }
                                DragMode.VOLUME -> {
                                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        .coerceAtLeast(1)
                                    val deltaSteps = (-travel * max * 1.1f).roundToInt()
                                    val newVol = (volumeStart + deltaSteps).coerceIn(0, max)
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        newVol,
                                        0,
                                    )
                                    feedback = GestureFeedback.VolumeLevel(newVol.toFloat() / max)
                                }
                            }
                        },
                    )
                },
        )
    }
}

@Composable
private fun GestureFeedbackOverlay(
    feedback: GestureFeedback?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        when (val f = feedback) {
            is GestureFeedback.Seek -> {
                val align = if (f.forward) Alignment.CenterEnd else Alignment.CenterStart
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = align,
                ) {
                    SeekHintBubble(
                        forward = f.forward,
                        modifier = Modifier.padding(horizontal = 48.dp),
                    )
                }
            }
            is GestureFeedback.BrightnessLevel -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    LevelHintBubble(
                        icon = { Icon(Icons.Default.Brightness6, null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                        label = "${(f.fraction * 100).roundToInt()}%",
                        modifier = Modifier.padding(start = 28.dp),
                    )
                }
            }
            is GestureFeedback.VolumeLevel -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    LevelHintBubble(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp),
                            )
                        },
                        label = "${(f.fraction * 100).roundToInt()}%",
                        modifier = Modifier.padding(end = 28.dp),
                    )
                }
            }
            null -> Unit
        }
    }
}

@Composable
private fun SeekHintBubble(
    forward: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = if (forward) Icons.Default.FastForward else Icons.Default.FastRewind,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "15",
            color = Color.White,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun LevelHintBubble(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        icon()
        Text(text = label, color = Color.White, fontSize = 14.sp)
    }
}
