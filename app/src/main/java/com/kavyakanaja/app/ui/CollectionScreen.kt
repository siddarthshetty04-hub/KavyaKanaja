package com.kavyakanaja.app.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.R
import com.kavyakanaja.app.data.Poem
import com.kavyakanaja.app.util.FavoritesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    allPoems: List<Poem>,
    isPlaying: Boolean,
    currentLineIndex: Int?,
    ttsReady: Boolean,
    onPlay: (List<String>) -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showFavOnly by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val favIds = remember { mutableStateOf(FavoritesManager.getFavoriteIds(context)) }
    
    var showZenMode by remember { mutableStateOf(false) }
    var zenModePoem by remember { mutableStateOf<Poem?>(null) }
    
    // Track which poem is currently expanded/playing so we can pass correct highlighting
    var expandedPoemId by remember { mutableStateOf<Int?>(null) }

    val filtered = allPoems.filter { p ->
        val matchSearch = searchQuery.isBlank() || p.title.contains(searchQuery, true) || p.poet.contains(searchQuery, true)
        val matchFav = if (showFavOnly) favIds.value.contains(p.id) else true
        matchSearch && matchFav
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poem Collection", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.ivory)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.deep_teal)),
                actions = {
                    IconButton(onClick = { showFavOnly = !showFavOnly }) {
                        Icon(
                            if (showFavOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (showFavOnly) Color.Red else colorResource(id = R.color.ivory)
                        )
                    }
                }
            )
        },
        containerColor = colorResource(id = R.color.ivory)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search poems or poets...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colorResource(id = R.color.deep_teal)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Clear, contentDescription = null) }
                },
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.deep_teal),
                    unfocusedBorderColor = colorResource(id = R.color.parchment),
                    focusedContainerColor = colorResource(id = R.color.warm_cream),
                    unfocusedContainerColor = colorResource(id = R.color.warm_cream)
                )
            )
            Text(
                text = if (showFavOnly) "❤️ Favorites (${filtered.size})" else "📚 All Poems (${filtered.size})",
                fontSize = 13.sp, color = colorResource(id = R.color.muted_sage),
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filtered, key = { it.id }) { poem ->
                    val isThisExpanded = expandedPoemId == poem.id
                    CollectionPoemCard(
                        poem = poem,
                        isExpanded = isThisExpanded,
                        isPlaying = isThisExpanded && isPlaying,
                        currentLineIndex = if (isThisExpanded) currentLineIndex else null,
                        ttsReady = ttsReady,
                        onExpand = { expandedPoemId = if (isThisExpanded) null else poem.id },
                        onPlay = { onPlay(poem.lines) },
                        onStop = onStop,
                        onFavChanged = { favIds.value = FavoritesManager.getFavoriteIds(context) },
                        onZenModeClick = { zenModePoem = poem; showZenMode = true }
                    )
                }
            }
        }
    }
    
    if (showZenMode && zenModePoem != null) {
        ZenModeOverlay(
            poem = zenModePoem!!,
            isVisible = showZenMode,
            isPlaying = isPlaying,
            currentLineIndex = currentLineIndex,
            onClose = { showZenMode = false },
            onPlayPause = { if (isPlaying) onStop() else onPlay(zenModePoem!!.lines) }
        )
    }
}

@Composable
fun CollectionPoemCard(
    poem: Poem,
    isExpanded: Boolean,
    isPlaying: Boolean,
    currentLineIndex: Int?,
    ttsReady: Boolean,
    onExpand: () -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onFavChanged: () -> Unit,
    onZenModeClick: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember(poem.id) { mutableStateOf(FavoritesManager.isFavorite(context, poem.id)) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { onExpand() },
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(poem.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
                    Text("— ${poem.poet}", fontSize = 13.sp, color = colorResource(id = R.color.rich_brown))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onZenModeClick) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = "Zen Mode", tint = colorResource(id = R.color.saffron))
                    }
                    IconButton(onClick = { isFavorite = FavoritesManager.toggleFavorite(context, poem.id); onFavChanged() }) {
                        Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else colorResource(id = R.color.muted_sage))
                    }
                    Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null, tint = colorResource(id = R.color.muted_sage))
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                // Lines with highlight
                poem.lines.forEachIndexed { i, line ->
                    val hl = currentLineIndex == i
                    Text(
                        text = line, fontSize = 17.sp,
                        fontWeight = if (hl) FontWeight.Bold else FontWeight.Normal,
                        color = if (hl) colorResource(id = R.color.saffron) else colorResource(id = R.color.dark_walnut),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            .background(if (hl) colorResource(id = R.color.saffron).copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                // Play button
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                    .background(colorResource(id = R.color.deep_teal))
                    .clickable { if (isPlaying) onStop() else onPlay() }
                    .padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!ttsReady) {
                            Text("⏳", color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading audio...", color = Color.White)
                        } else {
                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPlaying) "Pause" else "Play Recitation", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("📖 English Meaning", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deep_teal))
                Text(poem.englishTranslation, fontSize = 14.sp, color = colorResource(id = R.color.charcoal), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
