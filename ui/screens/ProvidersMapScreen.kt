package com.servitrust.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.servitrust.app.model.ServiceProvider
import com.servitrust.app.network.ApiClient
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersMapScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var providers by remember { mutableStateOf<List<ServiceProvider>>(emptyList()) }
    var userPoint by remember { mutableStateOf<GeoPoint?>(null) }

    fun loadProviders() {
        scope.launch {
            try {
                providers = ApiClient.api.getAllProviders()
            } catch (_: Exception) {
                providers = emptyList()
            }
        }
    }

    LaunchedEffect(Unit) {
        initOsmdroid(context)
        loadProviders()

        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) userPoint = GeoPoint(loc.latitude, loc.longitude)
            }
        }
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
                )
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Providers Map", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "Tap markers to view trust + service type",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        IconButton(onClick = { loadProviders() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Map in a rounded container look
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(13.0)
                                }
                            },
                            update = { map ->
                                map.overlays.clear()

                                val center = userPoint ?: firstProviderPoint(providers) ?: GeoPoint(13.0827, 80.2707)
                                map.controller.setCenter(center)

                                userPoint?.let { u ->
                                    map.overlays.add(
                                        Marker(map).apply {
                                            position = u
                                            title = "You"
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        }
                                    )
                                }

                                providers.forEach { p ->
                                    val lat = p.latitude
                                    val lng = p.longitude
                                    if (lat != null && lng != null) {
                                        map.overlays.add(
                                            Marker(map).apply {
                                                position = GeoPoint(lat, lng)
                                                title = p.name
                                                subDescription = "${p.serviceType} | Trust ${(p.trustScore ?: 0.0).toInt()}"
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }
                                        )
                                    }
                                }

                                map.invalidate()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun initOsmdroid(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    Configuration.getInstance().load(context, prefs)
    Configuration.getInstance().userAgentValue = context.packageName
}

private fun firstProviderPoint(providers: List<ServiceProvider>): GeoPoint? {
    val p = providers.firstOrNull { it.latitude != null && it.longitude != null } ?: return null
    return GeoPoint(p.latitude!!, p.longitude!!)
}