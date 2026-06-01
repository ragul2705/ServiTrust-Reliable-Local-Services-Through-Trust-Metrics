package com.servitrust.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.servitrust.app.model.ServiceRequest
import com.servitrust.app.network.ApiClient
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun UserCreateRequestScreen(
    navController: NavController,
    providerId: Long,
    serviceType: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val userId = SessionManager.getUserId(context) ?: 0L
    val userName = SessionManager.getUserName(context)

    val decodedServiceType = remember(serviceType) {
        URLDecoder.decode(serviceType, StandardCharsets.UTF_8.toString())
    }

    val normalizedServiceType = remember(decodedServiceType) {
        decodedServiceType.trim().uppercase().replace(" ", "_")
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Confirm Request", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Review details before sending",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    AppInfoRow(label = "Provider ID", value = providerId.toString())
                    AppInfoRow(label = "Service Type", value = normalizedServiceType)

                    if (success) {
                        AssistChip(
                            onClick = {},
                            label = { Text("✅ Request Sent Successfully") }
                        )
                    }

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
                            if (loading) return@Button

                            loading = true
                            error = null
                            success = false

                            scope.launch {
                                try {
                                    if (providerId <= 0L) {
                                        error = "Invalid Provider ID. Please select provider again."
                                        return@launch
                                    }
                                    if (userId <= 0L) {
                                        error = "User not logged in. Please login again."
                                        return@launch
                                    }

                                    val req = ServiceRequest(
                                        userId = userId,
                                        providerId = providerId,
                                        userName = userName,
                                        serviceType = normalizedServiceType,
                                        status = "PENDING"
                                    )

                                    ApiClient.api.createRequest(req)
                                    success = true
                                } catch (e: HttpException) {
                                    val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                                    error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
                                } catch (e: Exception) {
                                    error = e.message ?: "Request failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Sending...")
                        } else {
                            Text("Send Request")
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}