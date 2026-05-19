package com.kavyakanaja.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kavyakanaja.app.data.Poem
import com.kavyakanaja.app.util.FavoritesManager
import kotlinx.coroutines.delay

/**
 * Split-screen Local Multiplayer Game
 * Two players race to unscramble the poem line.
 */
@Composable
fun BattleModeDialog(poem: Poem, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var round by remember { mutableStateOf(0) }
    var p1Score by remember { mutableStateOf(0) }
    var p2Score by remember { mutableStateOf(0) }
    var winner by remember { mutableStateOf<Int?>(null) } // 1 for P1, 2 for P2
    
    // Pick 5 random lines with at least 3 words for the game
    val gameLines = remember(poem) {
        poem.lines.filter { it.split(" ").size > 2 }.shuffled().take(5).ifEmpty { poem.lines }
    }

    if (winner != null || round >= gameLines.size) {
        val finalWinner = when {
            p1Score > p2Score -> 1
            p2Score > p1Score -> 2
            else -> 0 // Tie
        }
        
        LaunchedEffect(Unit) {
            FavoritesManager.addXP(context, 50) // Give XP for playing
        }

        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (finalWinner == 0) "🤝 It's a Tie!" else "🏆 Player $finalWinner Wins!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Player 1: $p1Score", color = Color(0xFF64B5F6), fontSize = 24.sp)
                    Text("Player 2: $p2Score", color = Color(0xFFE57373), fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                        Text("Finish (+50 XP)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentLine = gameLines[round]
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Player 2 Side (Top, Rotated 180)
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF2C1C1C)).rotate(180f)) {
                    PlayerBattleArea(
                        playerId = 2,
                        color = Color(0xFFE57373),
                        line = currentLine,
                        score = p2Score,
                        onWinRound = { 
                            p2Score++
                            round++
                        }
                    )
                }

                // Divider
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White)) {
                    Box(modifier = Modifier.align(Alignment.Center).background(Color.White, CircleShape).padding(8.dp)) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                }

                // Player 1 Side (Bottom, Normal)
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1C252C))) {
                    PlayerBattleArea(
                        playerId = 1,
                        color = Color(0xFF64B5F6),
                        line = currentLine,
                        score = p1Score,
                        onWinRound = { 
                            p1Score++
                            round++
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerBattleArea(
    playerId: Int,
    color: Color,
    line: String,
    score: Int,
    onWinRound: () -> Unit
) {
    val correctWords = remember(line) { line.split(" ").filter { it.isNotBlank() } }
    val scrambledWords = remember(line) { correctWords.shuffled() }
    
    var selectedWords by remember(line) { mutableStateOf(emptyList<String>()) }
    var isWrong by remember { mutableStateOf(false) }

    LaunchedEffect(isWrong) {
        if (isWrong) {
            delay(400)
            selectedWords = emptyList()
            isWrong = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Player $playerId", color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Score: $score", color = Color.White.copy(0.7f), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Answer Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isWrong) Color.Red.copy(0.2f) else Color.White.copy(0.1f))
                .border(2.dp, if (isWrong) Color.Red else color.copy(0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                selectedWords.forEach { word ->
                    Text(word, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Word Choices
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            scrambledWords.forEachIndexed { index, word ->
                // Because words can be duplicated, we use the combination of word+index to track if it's selected
                val wordId = "$word-$index"
                val isSelected = selectedWords.contains(wordId)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color.White.copy(0.1f) else color.copy(0.8f))
                        .clickable(enabled = !isSelected && !isWrong) {
                            val newSelection = selectedWords + wordId
                            selectedWords = newSelection
                            
                            // Check if current sequence is correct so far
                            val strippedSelection = newSelection.map { it.split("-")[0] }
                            val isCorrectSoFar = strippedSelection.mapIndexed { i, w -> w == correctWords[i] }.all { it }
                            
                            if (!isCorrectSoFar) {
                                isWrong = true
                            } else if (newSelection.size == correctWords.size) {
                                onWinRound()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(word, color = if (isSelected) Color.Transparent else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
