package com.kavyakanaja.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kavyakanaja.app.data.Poem
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Duolingo-style Poem Memorization Mode.
 * Lines are shown one by one with blanks; user taps words to fill them in.
 */
@Composable
fun MemorizationMode(poem: Poem, onDismiss: () -> Unit) {
    var lineIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var totalLines by remember { mutableStateOf(poem.lines.size) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)))
                )
        ) {
            if (showResult) {
                MemorizationResult(score = score, total = totalLines, onDismiss = onDismiss)
            } else {
                val currentLine = poem.lines.getOrElse(lineIndex) { "" }
                MemorizationLinePuzzle(
                    poem = poem,
                    line = currentLine,
                    lineNumber = lineIndex + 1,
                    totalLines = poem.lines.size,
                    score = score,
                    onCorrect = {
                        score++
                        if (lineIndex < poem.lines.size - 1) lineIndex++
                        else showResult = true
                    },
                    onSkip = {
                        if (lineIndex < poem.lines.size - 1) lineIndex++
                        else showResult = true
                    },
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
fun MemorizationLinePuzzle(
    poem: Poem,
    line: String,
    lineNumber: Int,
    totalLines: Int,
    score: Int,
    onCorrect: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit
) {
    val words = line.split(" ").filter { it.isNotBlank() }
    // Pick 1-2 random words to blank out (skip very short words)
    val blankIndices = remember(line) {
        words.indices.filter { words[it].length > 2 }.shuffled().take(max(1, words.size / 3))
    }
    val allWords = remember(line) {
        val correctWords = blankIndices.map { words[it] }
        val decoys = poem.lines
            .flatMap { it.split(" ") }
            .filter { it.length > 2 && !correctWords.contains(it) }
            .shuffled()
            .take(6 - correctWords.size)
        (correctWords + decoys).shuffled()
    }
    
    val filledBlanks = remember(line) { mutableStateMapOf<Int, String>() }
    var feedback by remember(line) { mutableStateOf<Boolean?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun checkAnswer(): Boolean {
        return blankIndices.all { idx ->
            filledBlanks[idx] == words[idx]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧠 Memorize", color = Color.White.copy(0.8f), fontSize = 13.sp, letterSpacing = 2.sp)
                Text(poem.title, color = Color.White.copy(0.6f), fontSize = 11.sp)
            }
            Text("⭐ $score", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = lineNumber.toFloat() / totalLines,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = Color(0xFFA5D6A7),
            trackColor = Color.White.copy(0.2f)
        )
        Text("Line $lineNumber of $totalLines", color = Color.White.copy(0.5f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(40.dp))

        Text("Fill in the missing words:", color = Color.White.copy(0.7f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        // The line with blanks
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(0.15f))
                .padding(20.dp)
        ) {
            val annotated = buildAnnotatedString {
                words.forEachIndexed { idx, word ->
                    if (blankIndices.contains(idx)) {
                        val filled = filledBlanks[idx]
                        if (filled != null) {
                            withStyle(SpanStyle(color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)) {
                                append("[$filled]")
                            }
                        } else {
                            withStyle(SpanStyle(color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)) {
                                append("[   ?   ]")
                            }
                        }
                    } else {
                        withStyle(SpanStyle(color = Color.White)) {
                            append(word)
                        }
                    }
                    if (idx < words.size - 1) append(" ")
                }
            }
            Text(text = annotated, fontSize = 22.sp, lineHeight = 34.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Word choices
        Text("Choose words:", color = Color.White.copy(0.6f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        val chunked = allWords.chunked(3)
        chunked.forEach { rowWords ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                rowWords.forEach { choice ->
                    val isUsed = filledBlanks.values.contains(choice)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isUsed) Color.White.copy(0.1f) else Color.White.copy(0.2f))
                            .border(1.dp, if (isUsed) Color.Transparent else Color.White.copy(0.4f), RoundedCornerShape(50))
                            .clickable(enabled = !isUsed) {
                                // Fill next empty blank
                                val nextBlank = blankIndices.firstOrNull { !filledBlanks.containsKey(it) }
                                if (nextBlank != null) filledBlanks[nextBlank] = choice
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(choice, color = if (isUsed) Color.White.copy(0.3f) else Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback
        AnimatedVisibility(visible = feedback != null) {
            val msg = if (feedback == true) "✅ Correct! Well done!" else "❌ Not quite. Try again!"
            Text(msg, color = if (feedback == true) Color(0xFFA5D6A7) else Color(0xFFEF9A9A),
                fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                filledBlanks.clear()
                feedback = null
            }, shape = RoundedCornerShape(50)) {
                Text("↺ Clear", color = Color.White)
            }
            OutlinedButton(onClick = onSkip, shape = RoundedCornerShape(50)) {
                Text("Skip →", color = Color.White.copy(0.7f))
            }
            Button(
                onClick = {
                    val correct = checkAnswer()
                    feedback = correct
                    if (correct) {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(800)
                            onCorrect()
                        }
                    }
                },
                enabled = blankIndices.all { filledBlanks.containsKey(it) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Check ✓", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MemorizationResult(score: Int, total: Int, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val percent = if (total > 0) (score.toFloat() / total * 100).toInt() else 0
    val grade = when {
        percent >= 90 -> "🏆 Master"
        percent >= 70 -> "🥇 Expert"
        percent >= 50 -> "🥈 Learner"
        else -> "🥉 Beginner"
    }

    val xpEarned = score * 15
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (xpEarned > 0) {
            com.kavyakanaja.app.util.FavoritesManager.addXP(context, xpEarned)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Memory Test Complete!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(grade, fontSize = 40.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(100)).background(Color.White.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$percent%", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("$score / $total correct", color = Color.White.copy(0.7f), fontSize = 13.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Done", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
