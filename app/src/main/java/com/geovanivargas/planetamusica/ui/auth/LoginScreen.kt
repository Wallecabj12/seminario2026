package com.geovanivargas.planetamusica.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.geovanivargas.planetamusica.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToRecovery: () -> Unit,
    onLoginSuccess: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reaccionar a Success / Error
    LaunchedEffect(state.status) {
        when (val status = state.status) {
            is AuthStatus.Success -> onLoginSuccess()
            is AuthStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.onEvent(LoginUiEvent.ClearStatus)
            }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SoundCraft",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sampler & Gestor de Efectos Sonoros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(LoginUiEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                isError = state.email.isNotBlank() && !state.isEmailValid,
                supportingText = {
                    if (state.email.isNotBlank() && !state.isEmailValid) {
                        Text("Email inválido")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Contraseña
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(LoginUiEvent.PasswordChanged(it)) },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (state.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(LoginUiEvent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (state.passwordVisible)
                                    R.drawable.ic_menu_view
                                else
                                    R.drawable.ic_secure,
                            ),
                            contentDescription = if (state.passwordVisible)
                                "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // Botón Login
            Button(
                onClick = { viewModel.onEvent(LoginUiEvent.Submit) },
                enabled = state.isFormValid && state.status !is AuthStatus.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (state.status is AuthStatus.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Iniciar sesión")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Continuar sin cuenta
            TextButton(
                onClick = onContinueWithoutAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Continuar sin cuenta")
            }

            Spacer(Modifier.height(16.dp))

            // Enlace a registro
            TextButton(onClick = onNavigateToSignUp) {
                Text("¿No tienes cuenta? Regístrate")
            }

            // Enlace a recuperar contraseña
            TextButton(onClick = onNavigateToRecovery) {
                Text("¿Olvidaste tu contraseña?")
            }
        }
    }
}
