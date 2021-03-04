/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@Composable
fun AnimatedClockDemo() {
    val seconds = remember { mutableStateOf(0) }
    val minutes = remember { mutableStateOf(0) }

    val timer: MutableState<CountDownTimer?> = mutableStateOf(null)

    val interactionSourceMinutesFirstDigit = remember { MutableInteractionSource() }
    val interactionSourceMinutesLastDigit = remember { MutableInteractionSource() }
    val interactionSourceSecondsFirstDigit = remember { MutableInteractionSource() }
    val interactionSourceSecondsLastDigit = remember { MutableInteractionSource() }

    val isPressed1 by interactionSourceMinutesFirstDigit.collectIsPressedAsState()
    val isPressed2 by interactionSourceMinutesLastDigit.collectIsPressedAsState()
    val isPressed3 by interactionSourceSecondsFirstDigit.collectIsPressedAsState()
    val isPressed4 by interactionSourceSecondsLastDigit.collectIsPressedAsState()
    val isDragged1 by interactionSourceMinutesFirstDigit.collectIsDraggedAsState()
    val isDragged2 by interactionSourceMinutesLastDigit.collectIsDraggedAsState()
    val isDragged3 by interactionSourceSecondsFirstDigit.collectIsDraggedAsState()
    val isDragged4 by interactionSourceSecondsLastDigit.collectIsDraggedAsState()

    LaunchedEffect(key1 = Unit) {
        while (isActive) {
            withInfiniteAnimationFrameMillis {
                // Don't mutate while dragging
                val isPressed = isPressed1 || isPressed2 || isPressed3 || isPressed4
                val isDragged = isDragged1 || isDragged2 || isDragged3 || isDragged4
                if (isDragged) {
                    // If dragging & there is a timer, cancel & nullify it
                    if (timer.value != null) {
                        timer.value?.cancel()
                        timer.value = null
                    }
                    return@withInfiniteAnimationFrameMillis
                }
                // If not dragging & there is no timer, set one
                if (timer.value != null) return@withInfiniteAnimationFrameMillis
                val totalTimeSeconds = minutes.value * 60 + seconds.value
                // Note: 1st tick is trigger immediately, so add 1s to millis in future
                val newTimer = object : CountDownTimer(totalTimeSeconds.toLong() * 1000 + 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val totalSeconds = (millisUntilFinished / 1000).toInt()
                        seconds.value = totalSeconds % 60
                        minutes.value = totalSeconds / 60
                    }

                    override fun onFinish() {
                        // NA
                    }
                }
                newTimer.start()
                timer.value = newTimer
            }
        }
    }

    FancyClock(
        seconds,
        minutes,
        interactionSourceMinutesFirstDigit,
        interactionSourceMinutesLastDigit,
        interactionSourceSecondsFirstDigit,
        interactionSourceSecondsLastDigit
    )
}

private const val digitHeight = 56
private const val digitTextSize = 34
private const val activeDigitTextSize = 38
private val activeFontWeight = FontWeight.ExtraBold
private val fontWeight = FontWeight.Medium

@Composable
private fun FancyClock(
    seconds: MutableState<Int>,
    minutes: MutableState<Int>,
    interactionSourceMinutesFirstDigit: MutableInteractionSource,
    interactionSourceMinutesLastDigit: MutableInteractionSource,
    interactionSourceSecondsFirstDigit: MutableInteractionSource,
    interactionSourceSecondsLastDigit: MutableInteractionSource
) {

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberColumn(5, minutes.value / 10, interactionSourceMinutesFirstDigit) {
            minutes.value = it * 10 + minutes.value % 10
        }
        NumberColumn(9, minutes.value % 10, interactionSourceMinutesLastDigit) {
            minutes.value = minutes.value / 10 + it
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            modifier = Modifier.height(digitHeight.dp),
            color = MaterialTheme.colors.onSurface,
            fontSize = activeDigitTextSize.sp,
            text = ":",
            textAlign = TextAlign.Center,
            fontWeight = activeFontWeight
        )
        Spacer(modifier = Modifier.size(4.dp))
        NumberColumn(5, seconds.value / 10, interactionSourceSecondsFirstDigit) {
            seconds.value = it * 10 + seconds.value % 10
        }
        NumberColumn(9, seconds.value % 10, interactionSourceSecondsLastDigit) {
            seconds.value = seconds.value / 10 + it
        }
    }
}

@Composable
private fun NumberColumn(maxDigit: Int, digit: Int, interactionSource: MutableInteractionSource, onAdjust: (Int) -> Unit) {

    val isDragged by interactionSource.collectIsDraggedAsState()

    var dragDelta by remember { mutableStateOf(0f.dp) }
    if (!isDragged) {
        dragDelta = 0.dp
    }

    // Local values
    val density = LocalDensity.current
    val borderStroke = LocalBorderStroke.current
    val hapticFeedback = LocalHapticFeedback.current

    val cardHeight = ((maxDigit + 1) * digitHeight).dp

    val springDampingRatio = 0.6f
    val springStiffness: Float = Spring.StiffnessMedium

    var offsetY by remember { mutableStateOf(0f.dp) }
    val destinationY = (digit * digitHeight).dp - cardHeight / 2 + digitHeight.dp / 2
    LaunchedEffect(digit) {
        animate(
            initialValue = offsetY.value,
            targetValue = destinationY.value,
            animationSpec = spring(dampingRatio = springDampingRatio, stiffness = springStiffness)
        ) { animationValue, _ -> offsetY = animationValue.dp }
    }

    val alphas: List<State<Float>> = (0..maxDigit).map {
        animateFloatAsState(
            targetValue = if (it == digit) 1f else 0.5f
        )
    }
    val textSizes: List<State<Float>> = (0..maxDigit).map {
        animateFloatAsState(
            targetValue = if (it == digit) activeDigitTextSize.sp.value else digitTextSize.sp.value
        )
    }
    val fontWeights: List<State<Int>> = (0..maxDigit).map {
        animateIntAsState(
            targetValue = if (it == digit) activeFontWeight.weight else fontWeight.weight
        )
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw an elevation shadow for the rounded column
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .offset(y = offsetY)
                .size(digitHeight.dp, cardHeight),
            elevation = 4.dp
        ) {}
        // Draw all the digits up to count
        Column(
            modifier = Modifier
                .offset(y = offsetY)
                .draggable(
                    orientation = Orientation.Vertical,
                    interactionSource = interactionSource,
                    startDragImmediately = true,
                    state = rememberDraggableState { deltaPx ->
                        // Accumulated dragging (without any visual offsetting)
                        dragDelta += with(density) { deltaPx.toDp() }
                        val acc = destinationY + dragDelta
                        // Calculate new time
                        val newValue =
                            (((acc + cardHeight / 2 - digitHeight.dp / 2) / digitHeight.dp).roundToInt()).coerceIn(
                                0..maxDigit
                            )
                        Log.d(
                            "COMPOSE",
                            "deltapx: $deltaPx dragDelta: $dragDelta acc: $acc newValue: $newValue digit: $digit"
                        )
                        if (newValue != digit) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Reduce acc delta by the exact displacement
                            val newValueDestinationY =
                                (newValue * digitHeight).dp - cardHeight / 2 + digitHeight.dp / 2
                            dragDelta = acc - newValueDestinationY
                            // Report new value
                            onAdjust(newValue)
                        }
                    }
                )
        ) {
            for (i in (0..maxDigit).reversed()) {
                Text(
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .size(digitHeight.dp)
                        .alpha(alphas[i].value),
                    fontSize = textSizes[i].value.sp,
                    text = "$i",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight(fontWeights[i].value)
                )
            }
        }
        // Draw circle that follows focused digit
        Surface(
            modifier = Modifier.size(digitHeight.dp + borderStroke.width * 2, digitHeight.dp + borderStroke.width * 2),
            color = Color.Transparent,
            shape = MaterialTheme.shapes.medium,
            border = borderStroke,
            elevation = 0.dp // Be aware of ugly inner shadows
        ) {}
    }
}
