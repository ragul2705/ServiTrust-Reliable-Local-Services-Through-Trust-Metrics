package com.servitrust.app.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.servitrust.app.model.ProviderLoginRequest
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.navigation.Routes
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ProviderLoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun validate(): Boolean {
        emailError = null
        passwordError = null

        val e = email.trim()
        if (e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            emailError = "Enter a valid email"
        }
        if (password.trim().length < 5) {
            passwordError = "Password must be at least 5 characters"
        }
        return emailError == null && passwordError == null
    }

    // show backend errors nicely
    LaunchedEffect(error) {
        error?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {

            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(18.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Provider Login",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Manage requests and improve trust score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        isError = emailError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(
                        visible = emailError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = emailError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(
                        visible = passwordError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = passwordError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Button(
                        onClick = {
                            if (!validate()) return@Button
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    val res = ApiClient.api.providerLogin(
                                        ProviderLoginRequest(
                                            email = email.trim(),
                                            password = password.trim()
                                        )
                                    )

                                    // store providerId (service_providers.id) not authId
                                    SessionManager.saveProviderSession(
                                        context = context,
                                        providerId = res.providerId,
                                        providerName = res.name
                                    )

                                    navController.navigate(Routes.PROVIDER_HOME) {
                                        popUpTo(Routes.PROVIDER_LOGIN) { inclusive = true }
                                        launchSingleTop = true
                                    }

                                } catch (e: HttpException) {
                                    val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                                    error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
                                } catch (e: Exception) {
                                    error = e.message ?: "Login failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Signing in...")
                        } else {
                            Text("Login")
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.PROVIDER_REGISTER) },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("New Provider? Register")
                    }
                }
            }
        }
    }
}