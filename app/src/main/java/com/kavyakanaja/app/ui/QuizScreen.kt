package com.kavyakanaja.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.R
import com.kavyakanaja.app.data.Poem

data class QuizQuestion(
    val word: String,
    val correctAnswer: String,
    val options: List<String>
)

fun buildQuizQuestions(poems: List<Poem>): List<QuizQuestion> {
    val allEntries = poems.flatMap { it.wordMeanings.entries }
        .distinctBy { it.key }
        .shuffled()
        .take(10)
    val allAnswers = allEntries.map { it.value }
    return allEntries.map { entry ->
        val wrong = allAnswers.filter { it != entry.value }.shuffled().take(3)
        QuizQuestion(entry.key, entry.value, (wrong + entry.value).shuffled())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(allPoems: List<Poem>) {
    val questions = remember(allPoems) { buildQuizQuestions(allPoems) }
    var idx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Quiz", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.ivory)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.deep_teal))
            )
        },
        containerColor = colorResource(id = R.color.ivory)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Add more poems to unlock the quiz!", textAlign = TextAlign.Center, color = colorResource(id = R.color.muted_sage))
                }; return@Scaffold
            }
            if (finished) {
                QuizResultScreen(score, questions.size) { score = 0; idx = 0; selected = null; finished = false }
                return@Scaffold
            }

            val q = questions[idx]
            val progress by animateFloatAsState((idx + 1f) / questions.size, tween(400), label = "progress")

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = colorResource(id = R.color.saffron),
                trackColor = colorResource(id = R.color.parchment)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Question ${idx + 1} of ${questions.size} · Score: $score", fontSize = 13.sp, color = colorResource(id = R.color.muted_sage))
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("What does this Kannada word mean?", fontSize = 14.sp, color = colorResource(id = R.color.muted_sage))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(q.word, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            q.options.forEach { option ->
                val isCorrect = option == q.correctAnswer
                val wasSelected = selected == option
                val showResult = selected != null
                val bgColor = when {
                    showResult && isCorrect -> Color(0xFF388E3C)
                    showResult && wasSelected -> Color(0xFFD32F2F)
                    else -> colorResource(id = R.color.warm_cream)
                }
                val textColor = if (showResult && (isCorrect || wasSelected)) Color.White else colorResource(id = R.color.dark_walnut)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(if (wasSelected) 4.dp else 1.dp),
                    onClick = { if (selected == null) { selected = option; if (isCorrect) score++ } }
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(option, fontSize = 15.sp, color = textColor, modifier = Modifier.weight(1f))
                        if (showResult && isCorrect) Icon(Icons.Filled.Check, null, tint = Color.White)
                        else if (showResult && wasSelected) Icon(Icons.Filled.Close, null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            if (selected != null) {
                Button(
                    onClick = { if (idx < questions.size - 1) { idx++; selected = null } else finished = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deep_teal)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (idx < questions.size - 1) "Next →" else "See Results", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, total: Int, onRestart: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pct = if (total > 0) (score * 100) / total else 0
    val emoji = when { pct == 100 -> "🏆"; pct >= 70 -> "🌟"; pct >= 40 -> "📖"; else -> "💪" }
    val msg = when { pct == 100 -> "Perfect! You are a Kannada Scholar!"; pct >= 70 -> "Excellent knowledge of Kannada literature!"; pct >= 40 -> "Good effort! Keep reading!"; else -> "Keep practicing! You'll get better!" }

    val xpEarned = score * 10
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (xpEarned > 0) {
            com.kavyakanaja.app.util.FavoritesManager.addXP(context, xpEarned)
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 80.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Quiz Complete!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.dark_walnut))
        Spacer(modifier = Modifier.height(8.dp))
        Text("$score / $total", fontSize = 52.sp, fontWeight = FontWeight.ExtraBold, color = colorResource(id = R.color.saffron))
        Text("Correct Answers", color = colorResource(id = R.color.muted_sage))
        
        if (xpEarned > 0) {
            Text("+$xpEarned XP", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.parchment))) {
            Text(msg, modifier = Modifier.padding(20.dp), fontSize = 16.sp, textAlign = TextAlign.Center, color = colorResource(id = R.color.charcoal), lineHeight = 24.sp)
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.saffron)),
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("Try Again", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
