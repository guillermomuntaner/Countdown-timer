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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.activeDigitTextSize
import com.example.androiddevchallenge.ui.theme.activeFontWeight
import com.example.androiddevchallenge.ui.theme.digitHeight

@Composable
fun AdjustableTimer(
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
            val secondDigit = minutes.value % 10
            minutes.value = it * 10 + secondDigit
        }
        NumberColumn(9, minutes.value % 10, interactionSourceMinutesLastDigit) {
            val firstDigit = minutes.value / 10
            minutes.value = firstDigit * 10 + it
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
            val secondDigit = seconds.value % 10
            seconds.value = it * 10 + secondDigit
        }
        NumberColumn(9, seconds.value % 10, interactionSourceSecondsLastDigit) {
            val firstDigit = seconds.value / 10
            seconds.value = firstDigit * 10 + it
        }
    }
}
