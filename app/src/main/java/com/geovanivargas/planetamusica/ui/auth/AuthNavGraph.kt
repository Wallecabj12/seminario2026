package com.geovanivargas.planetamusica.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geovanivargas.planetamusica.ui.main.MainScreen
import com.geovanivargas.planetamusica.ui.presets.PresetsScreen
import com.geovanivargas.planetamusica.ui.sampler.SamplerScreen

/**
 * Rutas de navegación de la app.
 */
object AppRoutes {
    const val SAMPLER = "sampler"
    const val PRESETS = "presets"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val RECOVERY = "recovery"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AppRoutes.SAMPLER) {

        // Pantalla principal DAW (Pads | Secuencia | Presets)
        composable(AppRoutes.SAMPLER) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN)
                },
            )
        }

        // Pantalla de presets independiente
        composable(AppRoutes.PRESETS) {
            PresetsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(AppRoutes.SIGN_UP)
                },
                onNavigateToRecovery = {
                    navController.navigate(AppRoutes.RECOVERY)
                },
                onLoginSuccess = {
                    navController.popBackStack(AppRoutes.SAMPLER, inclusive = false)
                },
                onContinueWithoutAccount = {
                    navController.popBackStack(AppRoutes.SAMPLER, inclusive = false)
                },
            )
        }

        composable(AppRoutes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.popBackStack(AppRoutes.SAMPLER, inclusive = false)
                },
            )
        }

        composable(AppRoutes.RECOVERY) {
            RecoveryScreen(
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
    }
}
