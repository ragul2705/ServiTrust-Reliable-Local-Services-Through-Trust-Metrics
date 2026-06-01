package com.servitrust.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.servitrust.app.model.TrustScoreResponse
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.components.AppCard
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.ui.components.ScreenContainer
import com.servitrust.app.ui.components.SecondaryButton
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.math.abs

@Composable
fun ProviderRankingScreen(navController: NavController) {

    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var ranking by remember { mutableStateOf<List<TrustScoreResponse>>(emptyList()) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                ranking = ApiClient.api.getProviderRanking()
                    .sortedByDescending { it.trustScore }
            } catch (e: HttpException) {
                val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
            } catch (e: Exception) {
                error = e.message ?: "Failed"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenContainer(
        title = "Top Providers",
        subtitle = "Ranking by Trust Score",
        enableScroll = false
    ) {

        if (loading && ranking.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenContainer
        }

        if (error != null && ranking.isEmpty()) {
            AppCard {
                Text("Couldn't load ranking", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "Retry", enabled = !loading) { load() }
                Spacer(Modifier.height(10.dp))
                SecondaryButton(text = "Back", enabled = !loading) { navController.popBackStack() }
            }
            return@ScreenContainer
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {

            item {
                AppCard {
                    Text("Overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Providers ranked: ${ranking.size}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    PrimaryButton(
                        text = if (loading) "Refreshing..." else "Refresh",
                        enabled = !loading
                    ) { load() }

                    error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (ranking.isEmpty()) {
                item {
                    AppCard {
                        Text("No providers found.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "No ranking data available right now.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(ranking) { index, p ->
                    val rank = index + 1
                    val score = (p.trustScore).toInt()

                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rank #$rank", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Text("Provider ID: ${p.providerId}")
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Level: ${p.level}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$score / 100",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Trust Score",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text("Completion rate: ${p.completionRate}%")
                        Text("Average rating: ${p.averageRating}/5")
                        Text("Completed jobs: ${p.completedJobs}")
                        Text("Total requests: ${p.totalRequests}")
                        Text("Penalty: ${p.penalty}")

                        p.breakdown.takeIf { it.isNotEmpty() }?.let { b ->
                            Spacer(Modifier.height(10.dp))
                            Text("Points", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            b.entries.forEach { (k, v) ->
                                Text("${prettyKey(k)}: ${formatSigned(v)}")
                            }
                        }
                    }
                }
            }
        }

        SecondaryButton(text = "Back", enabled = !loading) {
            navController.popBackStack()
        }

        Spacer(Modifier.height(8.dp))
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