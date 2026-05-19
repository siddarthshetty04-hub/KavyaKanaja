package com.kavyakanaja.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.data.Poem
import kotlinx.coroutines.launch

@Composable
fun ZenModeOverlay(
    poem: Poem,
    isVisible: Boolean,
    isPlaying: Boolean,
    currentLineIndex: Int?,
    onClose: () -> Unit,
    onPlayPause: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500))
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // Auto-scroll to current line
        LaunchedEffect(currentLineIndex) {
            if (currentLineIndex != null && currentLineIndex >= 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(maxOf(0, currentLineIndex - 2))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    )
                )
                .clickable(enabled = false) {} // block touches
        ) {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 24.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close Zen Mode", tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                
                Text(
                    text = poem.title,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "— ${poem.poet}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Lyrics list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(poem.lines.size) { index ->
                        val isCurrent = currentLineIndex == index
                        val alpha by animateFloatAsState(
                            targetValue = if (isCurrent) 1f else if (currentLineIndex == null) 0.5f else 0.2f,
                            animationSpec = tween(400), label = "alpha"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isCurrent) 1.1f else 1f,
                            animationSpec = tween(400), label = "scale"
                        )

                        Text(
                            text = poem.lines[index],
                            fontSize = if (isCurrent) 28.sp else 22.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .alpha(alpha)
                        )
                    }
                }

                // Playback Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF0F2027))
                            )
                        )
                        .padding(bottom = 40.dp, top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AudioVisualizer(isPlaying = isPlaying)
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onPlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color(0xFF0F2027),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioVisualizer(isPlaying: Boolean) {
    val durations = listOf(400, 550, 300, 650, 450)
    
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(40.dp)
    ) {
        for (i in 0..4) {
            // Always run the infinite transition — never call composables conditionally
            val infiniteTransition = rememberInfiniteTransition(label = "bar_$i")
            val rawHeight by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durations[i], easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "h_$i"
            )
            // When paused, clamp the height to a small flat bar
            val heightFraction by animateFloatAsState(
                targetValue = if (isPlaying) rawHeight else 0.15f,
                animationSpec = tween(500),
                label = "clamp_$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(6.dp)
                    .height(40.dp * heightFraction)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }
    }
}
