package com.servitrust.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.servitrust.app.model.ServiceProvider
import com.servitrust.app.model.TrustScoreResponse
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.components.AppCard
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.ui.components.ScreenContainer
import com.servitrust.app.ui.components.SoftTag
import com.servitrust.app.ui.navigation.Routes
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

private enum class SortMode { TRUST, NEAREST, LOCATION }

private fun trustLevelFromScore(score: Double): String {
    return when {
        score >= 85 -> "A"
        score >= 70 -> "B"
        score >= 55 -> "C"
        else -> "D"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ✅ service type picker
    val serviceTypes = listOf(
        "CARPENTER",
        "PLUMBER",
        "ELECTRICIAN",
        "AC_REPAIR",
        "TECHNICIAN"
    )
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    var selectedServiceType by remember { mutableStateOf(serviceTypes.first()) }

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var sortMode by remember { mutableStateOf(SortMode.TRUST) }
    var locationFilter by remember { mutableStateOf("") }

    var rawProviders by remember { mutableStateOf<List<ServiceProvider>>(emptyList()) }
    var providersWithDistance by remember { mutableStateOf<List<Pair<ServiceProvider, Double?>>>(emptyList()) }

    var expandedProviderId by remember { mutableStateOf<Long?>(null) }
    var trustCache by remember { mutableStateOf<Map<Long, TrustScoreResponse>>(emptyMap()) }
    var trustLoadingIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var trustError by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    fun recompute() {
        val enriched = rawProviders.map { p ->
            val d = if (userLat != null && userLng != null && p.latitude != null && p.longitude != null) {
                calculateDistanceKm(userLat!!, userLng!!, p.latitude!!, p.longitude!!)
            } else null
            p to d
        }

        val q = locationFilter.trim().lowercase()
        val filtered = if (sortMode == SortMode.LOCATION && q.isNotBlank()) {
            enriched.filter { (p, _) -> (p.location ?: "").trim().lowercase().contains(q) }
        } else enriched

        val sorted = when (sortMode) {
            SortMode.TRUST -> filtered.sortedWith(
                compareByDescending<Pair<ServiceProvider, Double?>> { it.first.trustScore ?: 0.0 }
                    .thenByDescending { it.first.rating ?: 0.0 }
                    .thenBy { (it.first.name ?: "").lowercase() }
            )

            SortMode.NEAREST -> filtered.sortedWith(
                compareBy<Pair<ServiceProvider, Double?>> { it.second ?: Double.MAX_VALUE }
                    .thenByDescending { it.first.trustScore ?: 0.0 }
                    .thenByDescending { it.first.rating ?: 0.0 }
                    .thenBy { (it.first.name ?: "").lowercase() }
            )

            SortMode.LOCATION -> filtered.sortedWith(
                compareByDescending<Pair<ServiceProvider, Double?>> { it.first.trustScore ?: 0.0 }
                    .thenBy { it.second ?: Double.MAX_VALUE }
                    .thenBy { (it.first.name ?: "").lowercase() }
            )
        }

        providersWithDistance = sorted
    }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                rawProviders = ApiClient.api.searchProvidersByServiceType(selectedServiceType.trim())
                recompute()
            } catch (e: HttpException) {
                val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                error = body?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
            } catch (e: Exception) {
                error = e.message ?: "Failed"
            } finally {
                loading = false
            }
        }
    }

    fun fetchTrust(providerId: Long) {
        if (providerId <= 0L) return
        if (trustCache.containsKey(providerId)) return
        if (trustLoadingIds.contains(providerId)) return

        scope.launch {
            trustLoadingIds = trustLoadingIds + providerId
            trustError = trustError.toMutableMap().apply { remove(providerId) }

            try {
                val t = ApiClient.api.getProviderTrustScore(providerId)
                trustCache = trustCache + (providerId to t)
            } catch (e: HttpException) {
                val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                trustError = trustError + (providerId to (body?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"))
            } catch (e: Exception) {
                trustError = trustError + (providerId to (e.message ?: "Failed"))
            } finally {
                trustLoadingIds = trustLoadingIds - providerId
            }
        }
    }

    LaunchedEffect(Unit) {
        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLat = loc.latitude
                        userLng = loc.longitude
                    }
                    load()
                }
                .addOnFailureListener { load() }
        } else {
            load()
        }
    }

    LaunchedEffect(sortMode, locationFilter, rawProviders, userLat, userLng) {
        recompute()
    }

    ScreenContainer(
        title = "Find Providers",
        subtitle = "Pick service type • sort smart • request instantly",
        enableScroll = false
    ) {

        // ✅ Top controls card
        AppCard {
            Text("Service Type", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = serviceTypeExpanded,
                onExpandedChange = { serviceTypeExpanded = !serviceTypeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedServiceType,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceTypeExpanded) }
                )

                ExposedDropdownMenu(
                    expanded = serviceTypeExpanded,
                    onDismissRequest = { serviceTypeExpanded = false }
                ) {
                    serviceTypes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                selectedServiceType = t
                                serviceTypeExpanded = false
                                load()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Sort", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = sortMode == SortMode.TRUST,
                    onClick = { sortMode = SortMode.TRUST },
                    label = { Text("Trust") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = sortMode == SortMode.NEAREST,
                    onClick = { sortMode = SortMode.NEAREST },
                    label = { Text("Nearest") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = sortMode == SortMode.LOCATION,
                    onClick = { sortMode = SortMode.LOCATION },
                    label = { Text("Location") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (sortMode == SortMode.LOCATION) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = locationFilter,
                    onValueChange = { locationFilter = it },
                    label = { Text("Enter your area (ex: Hosur)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                text = if (loading) "Loading..." else "Open Map View",
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                navController.navigate(Routes.PROVIDER_MAP)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            Spacer(Modifier.height(12.dp))
        }

        error?.let {
            AppCard {
                Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "Retry", enabled = !loading) { load() }
            }
            return@ScreenContainer
        }

        if (providersWithDistance.isEmpty()) {
            AppCard {
                Text("No providers found", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "No providers available for $selectedServiceType right now.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@ScreenContainer
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(providersWithDistance) { (p, distanceKm) ->
                val pid = p.id ?: 0L
                val trustScore = p.trustScore ?: 0.0
                val trustLevel = trustLevelFromScore(trustScore)
                val expanded = expandedProviderId == pid

                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name ?: "Provider", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SoftTag(p.serviceType ?: selectedServiceType)
                                SoftTag(p.location ?: "Unknown")
                                SoftTag("Level $trustLevel")
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "Status: ${p.availabilityStatus ?: "UNKNOWN"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (distanceKm != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = String.format("Distance: %.2f km", distanceKm),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trust ${trustScore.toInt()}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Rating ${p.rating ?: 0.0}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    PrimaryButton(
                        text = "Request Service",
                        enabled = pid > 0L
                    ) {
                        val safeServiceType = URLEncoder.encode(
                            p.serviceType ?: selectedServiceType,
                            StandardCharsets.UTF_8.toString()
                        )
                        navController.navigate(Routes.userCreateRequest(pid, safeServiceType))
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (expanded) "Hide Trust Formula" else "View Trust Formula",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = pid > 0L) {
                                if (!expanded) fetchTrust(pid)
                                expandedProviderId = if (expanded) null else pid
                            }
                            .padding(vertical = 6.dp)
                    )

                    if (expanded) {
                        val isLoading = trustLoadingIds.contains(pid)
                        val trustErr = trustError[pid]
                        val trust = trustCache[pid]

                        when {
                            isLoading -> {
                                Spacer(Modifier.height(8.dp))
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            }
                            trustErr != null -> {
                                Spacer(Modifier.height(8.dp))
                                Text(trustErr, color = MaterialTheme.colorScheme.error)
                            }
                            trust != null -> {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Level: ${trust.level}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                trust.breakdown.entries.forEach { (k, v) ->
                                    Text(
                                        "$k : $v",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}