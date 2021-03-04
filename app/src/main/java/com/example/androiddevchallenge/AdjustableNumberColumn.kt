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

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.androiddevchallenge.ui.theme.activeDigitTextSize
import com.example.androiddevchallenge.ui.theme.activeFontWeight
import com.example.androiddevchallenge.ui.theme.digitHeight
import com.example.androiddevchallenge.ui.theme.digitTextSize
import com.example.androiddevchallenge.ui.theme.fontWeight
import kotlin.math.roundToInt

@Composable
fun NumberColumn(maxDigit: Int, digit: Int, interactionSource: MutableInteractionSource, onAdjust: (Int) -> Unit) {

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
