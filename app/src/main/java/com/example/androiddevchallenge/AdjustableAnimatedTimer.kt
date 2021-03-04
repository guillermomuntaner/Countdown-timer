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
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.isActive

@Composable
fun AnimatedTimer() {
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

    AdjustableTimer(
        seconds,
        minutes,
        interactionSourceMinutesFirstDigit,
        interactionSourceMinutesLastDigit,
        interactionSourceSecondsFirstDigit,
        interactionSourceSecondsLastDigit
    )
}
