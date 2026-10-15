package com.geovanivargas.planetamusica.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geovanivargas.planetamusica.ui.sampler.SamplerScreen

/**
 * Rutas de navegación de la app.
 */
object AppRoutes {
    const val SAMPLER = "sampler"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val RECOVERY = "recovery"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AppRoutes.SAMPLER) {

        // Pantalla principal – el sampler
        composable(AppRoutes.SAMPLER) {
            SamplerScreen(
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN)
                },
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
