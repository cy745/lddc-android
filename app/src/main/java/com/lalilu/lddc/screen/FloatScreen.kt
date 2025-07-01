package com.lalilu.lddc.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed class FloatScreenState {
    object Idle : FloatScreenState()
    data class Searching(val keywords: String) : FloatScreenState()
    object Success : FloatScreenState()
    data class Error(val message: String) : FloatScreenState()
}

sealed interface FloatScreenAction {
    data class Cancel(val forReason: String) : FloatScreenAction
}

sealed interface FloatScreenEvent {

}

@Composable
fun FloatScreen(
    modifier: Modifier = Modifier,
    state: FloatScreenState = FloatScreenState.Idle,
    event: Flow<FloatScreenEvent> = emptyFlow(),
    onAction: (FloatScreenAction) -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 32.dp)
        ) {
            AnimatedContent(
                targetState = state,
//                transitionSpec = { fadeIn() togetherWith fadeOut() },
                contentAlignment = Alignment.Center
            ) { stateValue ->
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .widthIn(min = 250.dp)
                        .padding(
                            vertical = 16.dp,
                            horizontal = 24.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (stateValue) {
                        FloatScreenState.Idle -> {
                            CircularProgressIndicator()
                        }

                        is FloatScreenState.Error -> {
                            Text(
                                text = "异常：${stateValue.message}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        is FloatScreenState.Searching -> {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Text(
                                text = stateValue.keywords,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Button(
                                onClick = { onAction(FloatScreenAction.Cancel("用户取消")) },
                            ) {
                                Text(text = "取消")
                            }
                        }

                        FloatScreenState.Success -> {
                            val activity = LocalActivity.current
                            val progress = remember { mutableFloatStateOf(0f) }
                            LaunchedEffect(state) {
                                if (state is FloatScreenState.Success) {
                                    progress.floatValue = 0f
                                    while (progress.floatValue < 1f) {
                                        progress.floatValue += 0.01f
                                        delay(500 / 100)
                                    }
                                    activity?.finish()
                                }
                            }

                            Text(
                                modifier = Modifier.padding(bottom = 12.dp),
                                text = "处理完成，自动关闭",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            LinearProgressIndicator(
                                progress = { progress.floatValue }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun FloatScreenPreview(modifier: Modifier = Modifier) {
    FloatScreen(
        modifier = Modifier,
        state = FloatScreenState.Success
    )
}