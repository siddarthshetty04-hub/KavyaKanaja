package com.kavyakanaja.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kavyakanaja.app.data.Poem
import kotlinx.coroutines.delay

/**
 * Spritz-style Speed Reader — flashes one word at a time, 
 * centered with the "Optimal Recognition Point" (ORP) highlighted in red.
 */
@Composable
fun SpeedReaderDialog(poem: Poem, onDismiss: () -> Unit) {
    val words = remember { poem.lines.joinToString(" ").split(" ").filter { it.isNotBlank() } }
    var currentIndex by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var wpm by remember { mutableStateOf(180) } // words per minute

    // Auto-advance words
    LaunchedEffect(isRunning, wpm) {
        if (isRunning) {
            while (isRunning && currentIndex < words.size) {
                delay((60_000L / wpm))
                if (currentIndex < words.size - 1) currentIndex++ else { isRunning = false }
            }
        }
    }

    val progress = if (words.isEmpty()) 0f else (currentIndex + 1).toFloat() / words.size
    val word = words.getOrElse(currentIndex) { "" }
    // Find the Optimal Recognition Point (ORP) — ~30% into the word
    val orpIndex = ((word.length - 1) * 0.3f).toInt().coerceAtLeast(0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color(0xFF01579B)))
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Text(
                    "⚡ Speed Reader",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    poem.title,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                // ORP Word Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    // ORP marker line
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color.Red.copy(alpha = 0.7f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color.Red.copy(alpha = 0.7f))
                    )

                    AnimatedContent(
                        targetState = word,
                        transitionSpec = {
                            fadeIn(tween(60)) togetherWith fadeOut(tween(60))
                        },
                        label = "word"
                    ) { displayWord ->
                        // Split word at ORP for red-highlighted center letter
                        val before = displayWord.take(orpIndex)
                        val center = displayWord.getOrElse(orpIndex) { ' ' }.toString()
                        val after = if (displayWord.length > orpIndex + 1) displayWord.substring(orpIndex + 1) else ""

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(before, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(center, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                            Text(after, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF64B5F6),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    "${currentIndex + 1} / ${words.size} words",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // WPM Slider
                Text("Speed: $wpm WPM", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = wpm.toFloat(),
                    onValueChange = { wpm = it.toInt() },
                    valueRange = 100f..500f,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(vertical = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF64B5F6),
                        activeTrackColor = Color(0xFF64B5F6),
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Controls
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Reset
                    OutlinedButton(
                        onClick = { currentIndex = 0; isRunning = false },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White.copy(0.5f), Color.White.copy(0.5f)))
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("↺ Reset", color = Color.White)
                    }

                    // Play/Pause
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(if (isRunning) "⏸ Pause" else "▶ Start", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }

                    // Close
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White.copy(0.5f), Color.White.copy(0.5f)))
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}
