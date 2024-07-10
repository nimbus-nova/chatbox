package com.chatgptlite.wanted.ui.conversations.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin


@Composable
fun RecordingDialog(
    onDismissRequest: () -> Unit,
    isRecording: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("recording") },
        text = {
            Column {
                WaveformAnimation(isRecording)
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun WaveformAnimation(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val waveAmplitude = canvasHeight / 4f
        val waveFrequency = 2f

        for (x in 0..canvasWidth.toInt() step 5) {
            val y = if (isRecording) {
                sin(x * waveFrequency / canvasWidth + wavePhase) * waveAmplitude + canvasHeight / 2
            } else {
                canvasHeight / 2
            }
            drawCircle(
                color = Color.Blue,
                radius = 2f,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}