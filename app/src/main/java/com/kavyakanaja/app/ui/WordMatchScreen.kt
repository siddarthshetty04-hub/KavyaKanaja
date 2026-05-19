package com.kavyakanaja.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun WordMatchDialog(poem: Poem, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    // Select 5 random words from the glossary
    val gamePairs = remember(poem) {
        poem.wordMeanings.entries.toList().shuffled().take(5)
    }

    if (gamePairs.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Glossary Available") },
            text = { Text("This poem does not have enough words in its glossary to play Word Match.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    val kannadaWords = remember { gamePairs.map { it.key }.shuffled() }
    val englishMeanings = remember { gamePairs.map { it.value }.shuffled() }

    var selectedKannada by remember { mutableStateOf<String?>(null) }
    var selectedEnglish by remember { mutableStateOf<String?>(null) }
    var matchedPairs by remember { mutableStateOf(setOf<String>()) }
    var errorPair by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showResult by remember { mutableStateOf(false) }

    LaunchedEffect(selectedKannada, selectedEnglish) {
        if (selectedKannada != null && selectedEnglish != null) {
            val correctMeaning = gamePairs.find { it.key == selectedKannada }?.value
            if (correctMeaning == selectedEnglish) {
                // Match!
                matchedPairs = matchedPairs + selectedKannada!!
                selectedKannada = null
                selectedEnglish = null
                
                if (matchedPairs.size == gamePairs.size) {
                    delay(500)
                    showResult = true
                    FavoritesManager.addXP(context, 20)
                }
            } else {
                // Incorrect
                errorPair = Pair(selectedKannada!!, selectedEnglish!!)
                delay(600)
                selectedKannada = null
                selectedEnglish = null
                errorPair = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF311B92))))
        ) {
            if (showResult) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🎉", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Perfect Match!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+20 XP", color = Color(0xFF69F0AE), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Awesome!", color = Color(0xFF311B92), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White) }
                        Text("Word Match", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    Text("Match the Kannada words to their English meanings", color = Color.White.copy(0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(40.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Kannada Column
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            kannadaWords.forEach { word ->
                                val isMatched = matchedPairs.contains(word)
                                val isSelected = selectedKannada == word
                                val isError = errorPair?.first == word
                                
                                val bgColor = when {
                                    isMatched -> Color.Transparent
                                    isError -> Color(0xFFE53935)
                                    isSelected -> Color(0xFF00ACC1)
                                    else -> Color.White.copy(0.15f)
                                }
                                val borderColor = if (isSelected) Color.White else Color.Transparent
                                val textColor = if (isMatched) Color.Transparent else Color.White

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isMatched && errorPair == null) { selectedKannada = if (isSelected) null else word }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(word, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // English Column
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            englishMeanings.forEach { meaning ->
                                val correspondingKannada = gamePairs.find { it.value == meaning }?.key
                                val isMatched = matchedPairs.contains(correspondingKannada)
                                val isSelected = selectedEnglish == meaning
                                val isError = errorPair?.second == meaning

                                val bgColor = when {
                                    isMatched -> Color.Transparent
                                    isError -> Color(0xFFE53935)
                                    isSelected -> Color(0xFF00ACC1)
                                    else -> Color.White.copy(0.15f)
                                }
                                val borderColor = if (isSelected) Color.White else Color.Transparent
                                val textColor = if (isMatched) Color.Transparent else Color.White

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isMatched && errorPair == null) { selectedEnglish = if (isSelected) null else meaning }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(meaning, color = textColor, fontWeight = FontWeight.Medium, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
