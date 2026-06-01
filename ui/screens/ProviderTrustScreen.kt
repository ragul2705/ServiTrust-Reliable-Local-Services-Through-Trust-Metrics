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
import com.servitrust.app.model.TrustHistoryItem
import com.servitrust.app.model.TrustScoreResponse
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.components.AppCard
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.ui.components.SecondaryButton
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ProviderTrustScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val providerId = SessionManager.getProviderIdOrZero(context)

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var trust by remember { mutableStateOf<TrustScoreResponse?>(null) }
    var history by remember { mutableStateOf<List<TrustHistoryItem>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun loadAll() {
        scope.launch {
            loading = true
            error = null
            try {
                trust = ApiClient.api.getProviderTrustScore(providerId)
                history = ApiClient.api.getProviderTrustHistory(providerId)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load trust info"
            } finally {
                loading = false
            }
        }
    }

    fun refreshRecompute() {
        scope.launch {
            loading = true
            error = null
            try {
                ApiClient.api.recomputeTrust(providerId, "Manual refresh from provider app")
                trust = ApiClient.api.getProviderTrustScore(providerId)
                history = ApiClient.api.getProviderTrustHistory(providerId)
            } catch (e: Exception) {
                error = e.message ?: "Refresh failed"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (providerId <= 0) {
            error = "Provider session not found. Please login again."
        } else {
            loadAll()
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
                            Text("My Trust Score", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Score, breakdown and history",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        IconButton(onClick = { refreshRecompute() }, enabled = !loading) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }

                    if (loading && trust == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        return@Card
                    }

                    val t = trust ?: run {
                        AppCard {
                            Text("No trust data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            PrimaryButton(text = "Retry", enabled = !loading) { loadAll() }
                        }
                        return@Card
                    }

                    // Score summary
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Current Trust", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "${t.trustScore.toInt()} / 100",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(t.level, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            PrimaryButton(
                                text = if (loading) "Refreshing..." else "Refresh",
                                enabled = !loading,
                                modifier = Modifier.widthIn(min = 120.dp)
                            ) { refreshRecompute() }
                        }

                        if (loading) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // Content list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {

                        item {
                            // Breakdown
                            AppCard {
                                Text("Breakdown", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(10.dp))

                                Text("Completion rate: ${t.completionRate}%")
                                Text("Avg rating: ${t.averageRating}/5")
                                Text("Completed jobs: ${t.completedJobs}")
                                Text("Total requests: ${t.totalRequests}")
                                Text("Availability score: ${t.availabilityScore}")
                                Text("Penalty: ${t.penalty}")

                                Spacer(Modifier.height(10.dp))

                                val b = t.breakdown ?: emptyMap()
                                if (b.isNotEmpty()) {
                                    Text("Points", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(6.dp))

                                    val orderedKeys = listOf(
                                        "baseScore",
                                        "ratingPoints",
                                        "completionPoints",
                                        "experiencePoints",
                                        "availabilityPoints",
                                        "complaintPenaltyPoints",
                                        "cancelPenaltyPoints"
                                    )

                                    orderedKeys.forEach { key ->
                                        val v = b[key] ?: return@forEach
                                        Text("${prettyKey(key)}: ${formatSigned(v)}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item {
                            // History title
                            AppCard {
                                Text("History", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                if (history.isEmpty()) {
                                    Text("No history yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text(
                                        "Latest changes shown below",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (history.isNotEmpty()) {
                            items(history) { h ->
                                val positive = (h.delta ?: 0.0) >= 0
                                AppCard {
                                    Text(
                                        text = (if (positive) "▲ " else "▼ ") + formatSigned(h.delta ?: 0.0),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (positive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text("Reason: ${h.reason}")
                                    Spacer(Modifier.height(4.dp))
                                    Text("Score after: ${(h.scoreAfter ?: 0.0).toInt()}")
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "At: ${h.createdAt}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    SecondaryButton(
                        text = "Back",
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) { navController.popBackStack() }
                }
            }
        }
    }
}

private fun prettyKey(k: String): String {
    return k.replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }
}

private fun formatSigned(v: Double): String {
    val sign = if (v >= 0) "+" else "-"
    val absV = abs(v)
    return "$sign${"%.2f".format(absV)}"
}