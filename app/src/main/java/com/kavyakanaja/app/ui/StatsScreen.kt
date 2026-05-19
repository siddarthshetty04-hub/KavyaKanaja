package com.kavyakanaja.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.R
import com.kavyakanaja.app.data.Poem
import com.kavyakanaja.app.util.FavoritesManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(allPoems: List<Poem>) {
    val context = LocalContext.current
    val streak = remember { FavoritesManager.getStreak(context) }
    val activeDates = remember { FavoritesManager.getActiveDates(context) }
    val favoriteIds = remember { FavoritesManager.getFavoriteIds(context) }
    val totalPoems = allPoems.size
    val uniquePoets = allPoems.map { it.poet }.distinct().size
    val wordsLearned = allPoems.sumOf { it.wordMeanings.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Insights", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 22.sp)
                        Text("Your learning journey", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A2E))
            )
        },
        containerColor = Color(0xFF1A1A2E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Streak Ring ──────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            StreakRing(streak = streak)

            Spacer(modifier = Modifier.height(28.dp))

            // ── 4-stat Grid ──────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), icon = "📚", label = "Poems", value = "$totalPoems", color = Color(0xFF7E57C2))
                StatCard(modifier = Modifier.weight(1f), icon = "❤️", label = "Favorites", value = "${favoriteIds.size}", color = Color(0xFFE91E63))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), icon = "🖊️", label = "Poets", value = "$uniquePoets", color = Color(0xFFFF7043))
                StatCard(modifier = Modifier.weight(1f), icon = "💡", label = "Words Learned", value = "$wordsLearned", color = Color(0xFF26A69A))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 30-day Activity Heatmap ───────────────────────────
            ActivityHeatmap(activeDates = activeDates)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Poets Breakdown ───────────────────────────────────
            PoetBreakdown(allPoems = allPoems)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Motivational Quote ────────────────────────────────
            val quotes = listOf(
                "Reading poetry is like drinking water—necessary, pure, and life-giving.",
                "Every poem you read is a window to a thousand minds.",
                "The more you read, the more you become."
            )
            val quote = remember { quotes.random() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✨", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"$quote\"",
                        color = Color.White.copy(0.8f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StreakRing(streak: Int) {
    val animProgress by animateFloatAsState(
        targetValue = (streak % 30).toFloat() / 30f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "ring"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        val ringColor = Color(0xFFFF6F00)
        val trackColor = Color(0xFF37474F)

        Canvas(modifier = Modifier.size(180.dp)) {
            val stroke = 16.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            // Track
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round))
            // Progress
            drawArc(
                brush = Brush.sweepGradient(colors = listOf(Color(0xFFFF6F00), Color(0xFFFFCA28), Color(0xFFFF6F00))),
                startAngle = -90f,
                sweepAngle = 360f * animProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥", fontSize = 32.sp)
            Text("$streak", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("day streak", color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: String, label: String, value: String, color: Color) {
    val animValue by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "stat"
    )
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 22.sp) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 13.sp, color = Color.White.copy(0.6f))
        }
    }
}

@Composable
fun ActivityHeatmap(activeDates: Set<String>) {
    val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val cal = Calendar.getInstance()
    
    // Build last 35 days (5 weeks x 7 days)
    cal.add(Calendar.DAY_OF_YEAR, -34)
    val days = (0..34).map {
        val d = format.format(cal.time)
        val isActive = activeDates.contains(d)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        isActive
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("📅 Activity (Last 5 Weeks)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            // 5 weeks x 7 days grid
            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                    week.forEach { active ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) Color(0xFF66BB6A) else Color(0xFF263238))
                        ) {
                            if (active) {
                                Text("✓", color = Color.White, fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF263238)))
                Text("  Missed  ", color = Color.White.copy(0.5f), fontSize = 11.sp)
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF66BB6A)))
                Text("  Active", color = Color.White.copy(0.5f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PoetBreakdown(allPoems: List<Poem>) {
    val poetCounts = allPoems.groupBy { it.poet }.mapValues { it.value.size }
        .entries.sortedByDescending { it.value }.take(5)
    val max = poetCounts.maxOfOrNull { it.value } ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("🖊️ Top Poets in Library", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            poetCounts.forEachIndexed { idx, (poet, count) ->
                val barProgress by animateFloatAsState(
                    targetValue = count.toFloat() / max,
                    animationSpec = tween(800 + idx * 100, easing = FastOutSlowInEasing),
                    label = "bar_$idx"
                )
                val barColor = listOf(
                    Color(0xFF7E57C2), Color(0xFF42A5F5), Color(0xFF26A69A),
                    Color(0xFFFF7043), Color(0xFFEF5350)
                )[idx % 5]

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${idx + 1}.", color = Color.White.copy(0.4f), fontSize = 12.sp, modifier = Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(poet, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("$count poems", color = barColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                                .background(Color.White.copy(0.1f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(barProgress).fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(Brush.horizontalGradient(listOf(barColor, barColor.copy(0.6f))))
                            )
                        }
                    }
                }
            }
        }
    }
}
