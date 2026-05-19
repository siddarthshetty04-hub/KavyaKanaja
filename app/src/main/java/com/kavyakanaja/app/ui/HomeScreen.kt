package com.kavyakanaja.app.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.R
import com.kavyakanaja.app.data.Poem
import com.kavyakanaja.app.util.FavoritesManager
import kotlinx.coroutines.launch

// ─── Home Screen ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    poem: Poem?,
    isPlaying: Boolean,
    currentLineIndex: Int?,
    ttsReady: Boolean,
    onPlay: (List<String>) -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    var showMeaningDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf("") }
    var selectedMeaning by remember { mutableStateOf("") }
    val streak = remember { FavoritesManager.updateStreak(context) }
    val xp = remember { FavoritesManager.getXP(context) }
    val (level, xpProgress) = remember { FavoritesManager.getLevel(xp) }

    var showStreakDialog by remember { mutableStateOf(false) }
    var showZenMode by remember { mutableStateOf(false) }
    var showSpeedReader by remember { mutableStateOf(false) }
    var showMemorize by remember { mutableStateOf(false) }
    var showBattleMode by remember { mutableStateOf(false) }
    var showWordMatch by remember { mutableStateOf(false) }
    var showLevelDetails by remember { mutableStateOf(false) }

    if (poem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = colorResource(id = R.color.saffron), fontWeight = FontWeight.Bold)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🌠 Firefly particle layer behind everything
        FireflyCanvas(modifier = Modifier.fillMaxSize())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kavya Kanaja", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.ivory), letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.deep_teal)),
                actions = {
                    // Level / XP Bar (Duolingo Style)
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(0.2f))
                            .clickable { showLevelDetails = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lvl $level", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            progress = xpProgress,
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF64B5F6),
                            strokeWidth = 2.dp,
                            trackColor = Color.White.copy(0.2f)
                        )
                    }

                    Surface(
                        color = colorResource(id = R.color.deep_amber).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { showStreakDialog = true }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$streak", fontWeight = FontWeight.ExtraBold, color = colorResource(id = R.color.ivory), fontSize = 16.sp)
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main card
            PoemCard(
                poem = poem,
                isPlaying = isPlaying,
                currentLineIndex = currentLineIndex,
                ttsReady = ttsReady,
                onPlay = onPlay,
                onStop = onStop,
                onWordClick = { w, m -> selectedWord = w; selectedMeaning = m; showMeaningDialog = true },
                onZenModeClick = { showZenMode = true }
            )

            Spacer(modifier = Modifier.height(20.dp))
            BhavarthaSection(poem)
            Spacer(modifier = Modifier.height(20.dp))

            // ─── Quick Feature Buttons ───────────────────────────
            Text("✨ More Ways to Learn", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = colorResource(id = R.color.ivory), modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    emoji = "⚡", label = "Speed\nReader",
                    gradient = listOf(Color(0xFF1A237E), Color(0xFF283593)),
                    onClick = { showSpeedReader = true }
                )
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🧠", label = "Memorize\nMode",
                    gradient = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)),
                    onClick = { showMemorize = true }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    emoji = "⚔️", label = "Poem\nBattle",
                    gradient = listOf(Color(0xFFB71C1C), Color(0xFFD32F2F)),
                    onClick = { showBattleMode = true }
                )
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🔗", label = "Word\nMatch",
                    gradient = listOf(Color(0xFF4A148C), Color(0xFF311B92)),
                    onClick = { showWordMatch = true }
                )
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🌌", label = "Zen\nMode",
                    gradient = listOf(Color(0xFF0F2027), Color(0xFF203A43)),
                    onClick = { showZenMode = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            WordGlossarySection(poem)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showMeaningDialog) {
        AlertDialog(
            onDismissRequest = { showMeaningDialog = false },
            title = { Text(selectedWord, color = colorResource(id = R.color.deep_teal), fontWeight = FontWeight.Bold) },
            text = { Text(selectedMeaning, fontSize = 16.sp, color = colorResource(id = R.color.charcoal)) },
            confirmButton = {
                TextButton(onClick = { showMeaningDialog = false }) {
                    Text("Got it!", color = colorResource(id = R.color.saffron), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colorResource(id = R.color.parchment)
        )
    }

    if (showStreakDialog) {
        StreakHistoryDialog(onDismiss = { showStreakDialog = false }, currentStreak = streak)
    }

    if (showSpeedReader) {
        SpeedReaderDialog(poem = poem, onDismiss = { showSpeedReader = false })
    }

    if (showMemorize) {
        MemorizationMode(poem = poem, onDismiss = { showMemorize = false })
    }

    if (showBattleMode) {
        BattleModeDialog(poem = poem, onDismiss = { showBattleMode = false })
    }

    if (showWordMatch) {
        WordMatchDialog(poem = poem, onDismiss = { showWordMatch = false })
    }

    if (showLevelDetails) {
        LevelDetailsDialog(level = level, currentXp = xp, onDismiss = { showLevelDetails = false })
    }

    ZenModeOverlay(
        poem = poem,
        isVisible = showZenMode,
        isPlaying = isPlaying,
        currentLineIndex = currentLineIndex,
        onClose = { showZenMode = false },
        onPlayPause = { if (isPlaying) onStop() else onPlay(poem.lines) }
    )
    } // close Box wrapper
}

// ─── Streak History Dialog ──────────────────────────────────────
@Composable
fun StreakHistoryDialog(onDismiss: () -> Unit, currentStreak: Int) {
    val context = LocalContext.current
    val activeDates = remember { FavoritesManager.getActiveDates(context) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your Streak", color = colorResource(id = R.color.deep_teal), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("You're on a", color = colorResource(id = R.color.muted_sage), fontSize = 16.sp)
                Text("$currentStreak Day", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = colorResource(id = R.color.saffron))
                Text("learning streak!", color = colorResource(id = R.color.dark_walnut), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Past 7 days visualization
                Text("Past 7 Days", fontWeight = FontWeight.SemiBold, color = colorResource(id = R.color.charcoal), modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val cal = java.util.Calendar.getInstance()
                    val format = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                    val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    
                    // Go back 6 days + today
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
                    for (i in 0..6) {
                        val dateStr = format.format(cal.time)
                        val dayStr = dayFormat.format(cal.time).take(1)
                        val isActive = activeDates.contains(dateStr)
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) colorResource(id = R.color.saffron) else colorResource(id = R.color.parchment)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = colorResource(id = R.color.muted_sage).copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dayStr, fontSize = 12.sp, color = colorResource(id = R.color.muted_sage))
                        }
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, null, tint = colorResource(id = R.color.deep_teal))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Daily Reminder", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
                            Text("Keep your streak alive by returning tomorrow!", fontSize = 12.sp, color = colorResource(id = R.color.muted_sage))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deep_teal)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Awesome!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colorResource(id = R.color.ivory)
    )
}

// ─── Poem Card ─────────────────────────────────────────────────
@Composable
fun PoemCard(
    poem: Poem,
    isPlaying: Boolean,
    currentLineIndex: Int?,
    ttsReady: Boolean,
    onPlay: (List<String>) -> Unit,
    onStop: () -> Unit,
    onWordClick: (String, String) -> Unit,
    onZenModeClick: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember(poem.id) { mutableStateOf(FavoritesManager.isFavorite(context, poem.id)) }

    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    
    val animatedRotationX by animateFloatAsState(targetValue = rotationX, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "rotX")
    val animatedRotationY by animateFloatAsState(targetValue = rotationY, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "rotY")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.rotationX = animatedRotationX
                this.rotationY = animatedRotationY
                cameraDistance = 8 * density
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        rotationX = 0f
                        rotationY = 0f
                    },
                    onDragCancel = {
                        rotationX = 0f
                        rotationY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    rotationY += dragAmount.x * 0.15f
                    rotationX -= dragAmount.y * 0.15f
                    rotationY = rotationY.coerceIn(-15f, 15f)
                    rotationX = rotationX.coerceIn(-15f, 15f)
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // Header row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(poem.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
                    Text("— ${poem.poet}", fontSize = 14.sp, color = colorResource(id = R.color.rich_brown), modifier = Modifier.padding(top = 4.dp))
                }
                // Favorite
                val scale by animateFloatAsState(targetValue = if (isFavorite) 1.3f else 1f, animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "fav")
                IconButton(onClick = { isFavorite = FavoritesManager.toggleFavorite(context, poem.id) }) {
                    Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite", tint = if (isFavorite) Color.Red else colorResource(id = R.color.muted_sage),
                        modifier = Modifier.scale(scale))
                }
                // Share
                val shareText = "${poem.title}\n— ${poem.poet}\n\n${poem.lines.joinToString("\n")}\n\nShared via Kavya Kanaja"
                IconButton(onClick = {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }, "Share Poem"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = colorResource(id = R.color.muted_sage))
                }
                
                // Zen Mode
                IconButton(onClick = onZenModeClick) {
                    Icon(Icons.Filled.Fullscreen, contentDescription = "Zen Mode", tint = colorResource(id = R.color.saffron), modifier = Modifier.size(28.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = colorResource(id = R.color.parchment))

            // ─── Poem Lines with Karaoke Highlight ───
            poem.lines.forEachIndexed { index, line ->
                val isHighlighted = currentLineIndex == index
                val bgAlpha by animateFloatAsState(if (isHighlighted) 0.25f else 0f, tween(300), label = "bg_$index")
                val textColor by animateColorAsState(
                    if (isHighlighted) colorResource(id = R.color.saffron) else colorResource(id = R.color.dark_walnut),
                    tween(300), label = "tc_$index"
                )
                val fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal

                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorResource(id = R.color.saffron).copy(alpha = bgAlpha))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inline word clickability
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        line.split(" ").forEach { word ->
                            val clean = word.replace(Regex("[.,!?;:]"), "")
                            val isDifficult = poem.wordMeanings.containsKey(clean)
                            Text(
                                text = "$word ",
                                fontSize = 20.sp,
                                fontWeight = if (isDifficult) FontWeight.ExtraBold else fontWeight,
                                color = if (isDifficult) colorResource(id = R.color.deep_amber) else textColor,
                                modifier = Modifier.clickable(enabled = isDifficult) {
                                    onWordClick(clean, poem.wordMeanings[clean] ?: "")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Play Button ───
            val btnBrush = Brush.horizontalGradient(
                listOf(colorResource(id = R.color.deep_teal), colorResource(id = R.color.muted_sage))
            )
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                    .background(btnBrush)
                    .clickable {
                        if (isPlaying) onStop() else onPlay(poem.lines)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!ttsReady) {
                        Text("⏳", color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Initializing audio...", color = Color.White, fontWeight = FontWeight.Medium)
                    } else {
                        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (isPlaying) "⏸  Pause Recitation" else "▶  Play Recitation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ─── Bhavartha ─────────────────────────────────────────────────
@Composable
fun BhavarthaSection(poem: Poem) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.parchment))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📖", fontSize = 20.sp); Spacer(modifier = Modifier.width(8.dp))
                Text("Meaning in English", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deep_teal))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(poem.englishTranslation, fontSize = 15.sp, lineHeight = 24.sp, color = colorResource(id = R.color.charcoal))
            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = colorResource(id = R.color.warm_cream))
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🇮🇳", fontSize = 18.sp); Spacer(modifier = Modifier.width(8.dp))
                Text("Kannada Bhavartha", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colorResource(id = R.color.muted_sage))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(poem.bhavarthaKannada, fontSize = 14.sp, lineHeight = 22.sp, color = colorResource(id = R.color.charcoal))
        }
    }
}

// ─── Word Glossary ─────────────────────────────────────────────
@Composable
fun WordGlossarySection(poem: Poem) {
    if (poem.wordMeanings.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 20.sp); Spacer(modifier = Modifier.width(8.dp))
                Text("Word Glossary", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
            }
            Text("Tap amber words in the poem to see meanings.", fontSize = 12.sp, color = colorResource(id = R.color.muted_sage), modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
            poem.wordMeanings.entries.forEachIndexed { i, entry ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Surface(shape = RoundedCornerShape(8.dp), color = colorResource(id = R.color.deep_amber).copy(alpha = 0.15f), modifier = Modifier.padding(end = 10.dp)) {
                        Text(entry.key, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deep_amber), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Text(entry.value, fontSize = 14.sp, color = colorResource(id = R.color.charcoal), lineHeight = 20.sp, modifier = Modifier.weight(1f))
                }
                if (i < poem.wordMeanings.size - 1) Divider(color = colorResource(id = R.color.parchment), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

// ─── Level Details Dialog ──────────────────────────────────────
@Composable
fun LevelDetailsDialog(level: Int, currentXp: Int, onDismiss: () -> Unit) {
    // Re-calculate the specific requirements for current level to display
    var tempLevel = 1
    var xpNeeded = 100
    var xpInCurrentLevel = currentXp
    while (xpInCurrentLevel >= xpNeeded) {
        xpInCurrentLevel -= xpNeeded
        tempLevel++
        xpNeeded = (xpNeeded * 1.5).toInt()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌟", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Level $level", color = colorResource(id = R.color.deep_teal), fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column {
                Text("You are an aspiring Kannada Scholar! Keep reading, playing, and learning to level up.", fontSize = 14.sp, color = colorResource(id = R.color.charcoal), lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                
                // XP Progress Bar
                Text("Progress to Level ${level + 1}", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(50)).background(colorResource(id = R.color.parchment))) {
                    Box(modifier = Modifier.fillMaxWidth(xpInCurrentLevel.toFloat() / xpNeeded).fillMaxHeight().background(Color(0xFF64B5F6)))
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$currentXp total XP", fontSize = 12.sp, color = colorResource(id = R.color.muted_sage))
                    Text("${xpNeeded - xpInCurrentLevel} XP to go", fontSize = 12.sp, color = colorResource(id = R.color.muted_sage), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("How to earn XP:", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Take a Quiz (+10 XP per correct answer)\n• Complete Memorize Mode (+15 XP per line)\n• Play a Poem Battle (+50 XP per match)", fontSize = 13.sp, color = colorResource(id = R.color.charcoal), lineHeight = 22.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deep_teal))) {
                Text("Keep Learning", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.ivory))
            }
        },
        containerColor = colorResource(id = R.color.ivory)
    )
}

// ─── Feature Launch Button ─────────────────────────────────────
@Composable
fun FeatureButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable {
                scope.launch {
                    scale.animateTo(0.9f, tween(80))
                    scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                }
                onClick()
            }
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 15.sp)
        }
    }
}
