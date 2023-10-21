package com.michaelrayven.tetris_jetpack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.michaelrayven.tetris_jetpack.feature_game.presentation.GameScreen
import com.michaelrayven.tetris_jetpack.feature_game_over.presentation.GameOverScreen
import com.michaelrayven.tetris_jetpack.feature_main_menu.presentation.MainMenuScreen
import com.michaelrayven.tetris_jetpack.ui.theme.TetrisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            TetrisTheme {
                NavHost(navController = navController, startDestination = "main_menu") {
                    composable("main_menu") { MainMenuScreen(navController) }
                    composable("game") { GameScreen(navController) }
                    composable(
                        route = "game_over/{scoreId}",
                        arguments = listOf(
                            navArgument("scoreId") {
                                type = NavType.LongType
                            }
                        )
                    ) {
                        Log.d("Main Activity", it.arguments?.getLong("scoreId").toString())
                        GameOverScreen(navController)
                    }
                }
            }
        }
    }
}