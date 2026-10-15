package com.geovanivargas.planetamusica.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.status) {
        when (val status = state.status) {
            is AuthStatus.Success -> onSignUpSuccess()
            is AuthStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.onEvent(SignUpUiEvent.ClearStatus)
            }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(24.dp))

            // Nombre
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(SignUpUiEvent.NameChanged(it)) },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(SignUpUiEvent.EmailChanged(it)) },
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
                onValueChange = { viewModel.onEvent(SignUpUiEvent.PasswordChanged(it)) },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = state.password.isNotBlank() && !state.isPasswordValid,
                supportingText = {
                    if (state.password.isNotBlank() && !state.isPasswordValid) {
                        Text("Mínimo 6 caracteres")
                    }
                },
                visualTransformation = if (state.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(SignUpUiEvent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (state.passwordVisible)
                                    android.R.drawable.ic_menu_view
                                else
                                    android.R.drawable.ic_secure,
                            ),
                            contentDescription = if (state.passwordVisible)
                                "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(SignUpUiEvent.ConfirmPasswordChanged(it)) },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                isError = state.confirmPassword.isNotBlank() && !state.doPasswordsMatch,
                supportingText = {
                    if (state.confirmPassword.isNotBlank() && !state.doPasswordsMatch) {
                        Text("Las contraseñas no coinciden")
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Términos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { viewModel.onEvent(SignUpUiEvent.TermsChanged(it)) },
                )
                Text(
                    text = "Acepto los Términos y Condiciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Botón registro
            Button(
                onClick = { viewModel.onEvent(SignUpUiEvent.Submit) },
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
                    Text("Registrarse")
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
