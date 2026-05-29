package com.android.nextai.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.nextai.ui.screen.home.HomeScreen
import com.android.nextai.ui.screen.model_provider.QwenProviderScreen
import com.android.nextai.ui.screen.settings.SettingsScreen
import com.android.nextai.ui.theme.NeuronVerseTheme
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.provider.ProviderViewModel
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
    val providerViewModel: ProviderViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    fun NavHostController.safePop(): Boolean {
        return if (previousBackStackEntry != null) {
            popBackStack()
            true
        } else false
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.HOME
    ) {

        composable(AppRoute.HOME) {

            HomeScreen(
                chatViewModel = chatViewModel,
                providerViewModel = providerViewModel,
                onNavigateToSettings  = {
                    navController.navigate(AppRoute.SETTINGS)
                }
            )
        }

        composable(AppRoute.SETTINGS) {
            SettingsScreen(
                providerViewModel = providerViewModel,
                onBackClick = {
                    navController.safePop()
                },
                onNavigateToQwenProvider = {
                    navController.navigate(AppRoute.QWEN_PROVIDER)
                }
            )
        }
        composable(AppRoute.QWEN_PROVIDER) {
            QwenProviderScreen(
                providerViewModel = providerViewModel,
                onBackClick = {
                    navController.safePop()
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

    // Provider Config
    const val QWEN_PROVIDER = "qwen_provider"
}
