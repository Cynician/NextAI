package com.android.nextai.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.nextai.ui.screen.home.HomeScreen
import com.android.nextai.ui.screen.settings.SettingsScreen
import com.android.nextai.ui.theme.NeuronVerseTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeuronVerseTheme {
                AppNavHost()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.HOME
    ) {

        composable(AppRoute.HOME) {

            HomeScreen(
                onNavigateToSettings  = {
                    navController.navigate(AppRoute.SETTINGS)
                }
            )
        }

        composable(AppRoute.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Route define
 */
object AppRoute {

    const val HOME = "home"

    const val SETTINGS = "settings"
}
