package com.servitrust.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.servitrust.app.ui.components.AppCard
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.ui.components.SecondaryButton
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ProviderRequestsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val providerId = SessionManager.getProviderIdOrZero(context)

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var requests by remember { mutableStateOf<List<ServiceRequest>>(emptyList()) }

    var actionLoadingId by remember { mutableStateOf<Long?>(null) }
    var fetching by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun fetch() {
        if (fetching) return
        fetching = true
        loading = true
        try {
            requests = ApiClient.api.getRequestsByProvider(providerId)
            error = null
        } catch (e: HttpException) {
            val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
        } catch (e: Exception) {
            error = e.message ?: "Failed"
        } finally {
            loading = false
            fetching = false
        }
    }

    fun loadOnce() {
        scope.launch { fetch() }
    }

    suspend fun runAction(requestId: Long, block: suspend () -> Unit) {
        if (actionLoadingId != null) return
        actionLoadingId = requestId
        try {
            block()
            fetch()
        } catch (e: HttpException) {
            val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
        } catch (e: Exception) {
            error = e.message ?: "Failed"
        } finally {
            actionLoadingId = null
        }
    }

    LaunchedEffect(providerId) {
        if (providerId <= 0L) {
            error = "Provider not logged in. Please login again."
            return@LaunchedEffect
        }
        while (isActive) {
            if (actionLoadingId == null) fetch()
            delay(5000)
        }
    }

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
                    .padding(16.dp)
                    .fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Incoming Requests", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Auto refresh every 5 seconds",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        IconButton(
                            onClick = { loadOnce() },
                            enabled = !loading
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }

                    // Summary card
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Requests", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${requests.size}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }

                            PrimaryButton(
                                text = if (loading) "Refreshing..." else "Refresh",
                                enabled = !loading,
                                modifier = Modifier.widthIn(min = 130.dp)
                            ) { loadOnce() }
                        }

                        if (loading && requests.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // Empty/first-load
                    if (loading && requests.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        return@Card
                    }

                    if (requests.isEmpty()) {
                        AppCard {
                            Text(
                                "No requests yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            SecondaryButton(text = "Back") { navController.popBackStack() }
                        }
                        return@Card
                    }

                    // List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(requests) { r ->
                            val requestId = r.id ?: 0L
                            val status = r.status.trim().uppercase()
                            val busy = actionLoadingId == requestId

                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Request #$requestId", style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Status: $status",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text("User: ${r.userName.ifBlank { "Unknown" }}")
                                        Spacer(Modifier.height(4.dp))
                                        Text("Service: ${r.serviceType}")
                                    }
                                }

                                r.description?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Description: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                r.address?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Address: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (status == "PENDING") {
                                        PrimaryButton(
                                            text = if (busy) "Working..." else "Accept",
                                            enabled = !busy,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            scope.launch { runAction(requestId) { ApiClient.api.acceptRequest(requestId) } }
                                        }
                                    }

                                    if (status == "ACCEPTED") {
                                        PrimaryButton(
                                            text = if (busy) "Working..." else "Complete",
                                            enabled = !busy,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            scope.launch { runAction(requestId) { ApiClient.api.completeRequest(requestId) } }
                                        }
                                    }

                                    if (status == "PENDING" || status == "ACCEPTED") {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    runAction(requestId) { ApiClient.api.cancelRequestProvider(requestId) }
                                                }
                                            },
                                            enabled = !busy,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(if (busy) "Working..." else "Cancel")
                                        }
                                    }
                                }

                                if (busy) {
                                    Spacer(Modifier.height(10.dp))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }

                    SecondaryButton(
                        text = "Back",
                        enabled = !loading && actionLoadingId == null,
                        modifier = Modifier.fillMaxWidth()
                    ) { navController.popBackStack() }
                }
            }
        }
    }
}