package com.servitrust.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.servitrust.app.model.ProviderRegisterRequest
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderRegisterScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val serviceTypeMap = linkedMapOf(
        "Plumber" to "PLUMBER",
        "Electrician" to "ELECTRICIAN",
        "Technician" to "TECHNICIAN",
        "Carpenter" to "CARPENTER",
        "AC Repair" to "AC_REPAIR"
    )
    val serviceTypeDisplayList = serviceTypeMap.keys.toList()

    var expanded by remember { mutableStateOf(false) }
    var selectedServiceTypeDisplay by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // field errors (UI-only)
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var serviceError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun validate(): Boolean {
        nameError = null
        emailError = null
        passwordError = null
        serviceError = null
        locationError = null

        val n = name.trim()
        val e = email.trim()
        val p = password.trim()
        val l = location.trim()

        if (n.isBlank()) nameError = "Enter your name"
        if (e.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) emailError = "Enter a valid email"
        if (p.length < 5) passwordError = "Password must be at least 5 characters"
        if (selectedServiceTypeDisplay.isBlank()) serviceError = "Choose service type"
        if (l.isBlank()) locationError = "Enter location (city/area)"

        return listOf(nameError, emailError, passwordError, serviceError, locationError).all { it == null }
    }

    // show backend errors nicely
    LaunchedEffect(error) {
        error?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showSnackbar(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

        if (!granted) {
            error = "Location permission not granted"
            return@rememberLauncherForActivityResult
        }

        fusedClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    scope.launch { snackbarHostState.showSnackbar("GPS location captured ✅") }
                } else {
                    error = "Unable to fetch location. Turn on GPS and try again."
                }
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Location error"
            }
    }

    suspend fun geocodeLocation(query: String): Pair<Double, Double>? {
        val q = query.trim()
        if (q.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())
                val list = geocoder.getFromLocationName(q, 1)
                val a = list?.firstOrNull()
                if (a != null) a.latitude to a.longitude else null
            } catch (_: Exception) {
                null
            }
        }
    }

    fun resolveLocationText() {
        scope.launch {
            error = null
            val res = geocodeLocation(location)
            if (res == null) {
                error = "Could not resolve location. Try: Hosur, Tamil Nadu"
            } else {
                latitude = res.first
                longitude = res.second
                snackbarHostState.showSnackbar("Location resolved ✅")
            }
        }
    }

    fun fetchCurrentLocation() {
        error = null
        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        scope.launch { snackbarHostState.showSnackbar("GPS location captured ✅") }
                    } else {
                        error = "Unable to fetch location. Turn on GPS and try again."
                    }
                }
                .addOnFailureListener { e ->
                    error = e.message ?: "Location error"
                }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val canSubmit =
        !loading &&
                name.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                selectedServiceTypeDisplay.isNotBlank() &&
                location.isNotBlank()

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Registration Completed") },
            text = {
                Text(
                    "Your provider account has been created.\nPlease login using your email and password."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(Routes.PROVIDER_LOGIN) {
                            popUpTo(Routes.PROVIDER_REGISTER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) { Text("Go to Login") }
            }
        )
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
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Provider Register", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Create your provider account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = nameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = nameError != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(nameError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = emailError != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(emailError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = passwordError != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(passwordError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (!loading) expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedServiceTypeDisplay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Service Type") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            isError = serviceError != null,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            serviceTypeDisplayList.forEach { display ->
                                DropdownMenuItem(
                                    text = { Text(display) },
                                    onClick = {
                                        selectedServiceTypeDisplay = display
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = serviceError != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(serviceError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedTextField(
                        value = location,
                        onValueChange = {
                            location = it
                            if (it.isBlank()) {
                                latitude = null
                                longitude = null
                            }
                        },
                        label = { Text("Location (city/area)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        isError = locationError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = locationError != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(locationError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }

                    // GPS / Resolve buttons (same logic)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { fetchCurrentLocation() },
                            enabled = !loading,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (latitude != null && longitude != null) "GPS Done" else "Use GPS")
                        }

                        OutlinedButton(
                            onClick = { resolveLocationText() },
                            enabled = !loading && location.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Resolve")
                        }
                    }

                    if (latitude != null && longitude != null) {
                        Text(
                            text = "Lat: ${"%.5f".format(latitude)} • Lng: ${"%.5f".format(longitude)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (loading) return@Button
                            if (!validate()) return@Button

                            loading = true
                            error = null

                            val serviceTypeApi = serviceTypeMap[selectedServiceTypeDisplay] ?: ""

                            scope.launch {
                                try {
                                    ApiClient.api.providerRegister(
                                        ProviderRegisterRequest(
                                            name = name.trim(),
                                            email = email.trim(),
                                            password = password.trim(),
                                            serviceType = serviceTypeApi.trim(),
                                            location = location.trim(),
                                            latitude = latitude,
                                            longitude = longitude
                                        )
                                    )

                                    name = ""
                                    email = ""
                                    password = ""
                                    location = ""
                                    selectedServiceTypeDisplay = ""
                                    latitude = null
                                    longitude = null

                                    showSuccessDialog = true

                                } catch (e: HttpException) {
                                    val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                                    error = body?.takeIf { it.isNotBlank() } ?: "Register failed (${e.code()})"
                                } catch (e: Exception) {
                                    error = e.message ?: "Register failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSubmit && !loading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Registering...")
                        } else {
                            Text("Register")
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.PROVIDER_LOGIN) },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Already a Provider? Login")
                    }
                }
            }
        }
    }
}