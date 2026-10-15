package com.geovanivargas.planetamusica.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Rutas de navegación de autenticación.
 */
object AuthRoutes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val RECOVERY = "recovery"
    const val HOME = "home" // Placeholder – se reemplazará por la pantalla principal del sampler
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                },
                onNavigateToRecovery = {
                    navController.navigate(AuthRoutes.RECOVERY)
                },
                onLoginSuccess = {
                    navController.navigate(AuthRoutes.HOME) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    }
                },
                onContinueWithoutAccount = {
                    navController.navigate(AuthRoutes.HOME) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(AuthRoutes.HOME) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.RECOVERY) {
            RecoveryScreen(
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        // Placeholder home – se implementará en iteraciones futuras
        composable(AuthRoutes.HOME) {
            HomePlaceholder()
        }
    }
}
