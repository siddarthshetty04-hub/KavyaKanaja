package com.kavyakanaja.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.kavyakanaja.app.ui.*

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.kavyakanaja.app.util.ReminderReceiver

class MainActivity : ComponentActivity() {
    private val viewModel: PoemViewModel by viewModels {
        PoemViewModelFactory(application)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // User handled permission
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Notification Permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Schedule the daily streak reminder
        ReminderReceiver.scheduleDailyReminder(this)

        setContent { MaterialTheme { KavyaKanajaApp(viewModel) } }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home       : Screen("home",   "Home",   Icons.Filled.Home)
    object Collection : Screen("poems",  "Poems",  Icons.Filled.Book)
    object Quiz       : Screen("quiz",   "Quiz",   Icons.Filled.Quiz)
    object Poets      : Screen("poets",  "Poets",  Icons.Filled.Person)
    object Stats      : Screen("stats",  "Stats",  Icons.Filled.BarChart)
}

@Composable
fun KavyaKanajaApp(viewModel: PoemViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Collection, Screen.Quiz, Screen.Poets, Screen.Stats)

    val poem        by viewModel.poemOfTheDay.collectAsState()
    val allPoems    by viewModel.allPoems.collectAsState()
    val isPlaying   by viewModel.isPlaying.collectAsState()
    val lineIndex   by viewModel.currentLineIndex.collectAsState()
    val ttsReady    by viewModel.ttsReady.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = colorResource(id = R.color.parchment)) {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = current == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorResource(id = R.color.ivory),
                            selectedTextColor = colorResource(id = R.color.deep_teal),
                            indicatorColor = colorResource(id = R.color.saffron),
                            unselectedIconColor = colorResource(id = R.color.muted_sage),
                            unselectedTextColor = colorResource(id = R.color.muted_sage)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        poem = poem,
                        isPlaying = isPlaying,
                        currentLineIndex = lineIndex,
                        ttsReady = ttsReady,
                        onPlay = { lines -> viewModel.speakLines(lines) },
                        onStop = { viewModel.stopSpeaking() }
                    )
                }
                composable(Screen.Collection.route) {
                    CollectionScreen(
                        allPoems = allPoems,
                        isPlaying = isPlaying,
                        currentLineIndex = lineIndex,
                        ttsReady = ttsReady,
                        onPlay = { lines -> viewModel.speakLines(lines) },
                        onStop = { viewModel.stopSpeaking() }
                    )
                }
                composable(Screen.Quiz.route) {
                    QuizScreen(allPoems = allPoems)
                }
                composable(Screen.Poets.route) {
                    PoetsCornerScreen()
                }
                composable(Screen.Stats.route) {
                    StatsScreen(allPoems = allPoems)
                }
            }
        }
    }
}
