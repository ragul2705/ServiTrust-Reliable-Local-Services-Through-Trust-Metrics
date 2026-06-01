package com.servitrust.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.servitrust.app.model.ComplaintRequest
import com.servitrust.app.model.ReviewRequest
import com.servitrust.app.model.ServiceRequest
import com.servitrust.app.network.ApiClient
import com.servitrust.app.ui.components.AppCard
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun UserRequestsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userId = SessionManager.getUserIdOrZero(context)

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var requests by remember { mutableStateOf<List<ServiceRequest>>(emptyList()) }

    var actionLoadingId by remember { mutableStateOf<Long?>(null) }

    // -------- Review states --------
    var reviewForRequestId by remember { mutableStateOf<Long?>(null) }
    var reviewRating by remember { mutableStateOf(5f) }
    var reviewFeedback by remember { mutableStateOf("") }
    var reviewSubmitting by remember { mutableStateOf(false) }

    // -------- Complaint states --------
    var complaintForRequestId by remember { mutableStateOf<Long?>(null) }
    var complaintReason by remember { mutableStateOf("") }
    var complaintDetails by remember { mutableStateOf("") }
    var complaintSubmitting by remember { mutableStateOf(false) }

    fun sorted(list: List<ServiceRequest>): List<ServiceRequest> {
        return list.sortedWith(
            compareBy<ServiceRequest> {
                when (it.status.trim().uppercase()) {
                    "PENDING" -> 0
                    "ACCEPTED" -> 1
                    "COMPLETED" -> 2
                    "CANCELLED_BY_USER" -> 3
                    "CANCELLED_BY_PROVIDER" -> 4
                    else -> 5
                }
            }.thenByDescending { it.id ?: 0L }
        )
    }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                requests = sorted(ApiClient.api.getRequestsByUser(userId))
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

    suspend fun runAction(requestId: Long, block: suspend () -> Unit) {
        if (actionLoadingId != null) return
        actionLoadingId = requestId
        error = null
        try {
            block()
            load()
        } catch (e: HttpException) {
            val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
        } catch (e: Exception) {
            error = e.message ?: "Failed"
        } finally {
            actionLoadingId = null
        }
    }

    LaunchedEffect(userId) {
        if (userId <= 0L) {
            error = "User not logged in. Please login again."
            return@LaunchedEffect
        }
        load()
    }

    // -------------------- Review Dialog --------------------
    if (reviewForRequestId != null) {
        AlertDialog(
            onDismissRequest = { if (!reviewSubmitting) reviewForRequestId = null },
            shape = RoundedCornerShape(18.dp),
            title = { Text("Submit Review") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Rating: ${reviewRating.toInt()}/5",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = reviewRating,
                        onValueChange = { reviewRating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        enabled = !reviewSubmitting
                    )
                    OutlinedTextField(
                        value = reviewFeedback,
                        onValueChange = { reviewFeedback = it },
                        label = { Text("Feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !reviewSubmitting
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rid = reviewForRequestId ?: return@Button
                        scope.launch {
                            reviewSubmitting = true
                            error = null
                            try {
                                ApiClient.api.submitReview(
                                    requestId = rid,
                                    userId = userId,
                                    review = ReviewRequest(
                                        rating = reviewRating.toInt(),
                                        feedback = reviewFeedback.trim()
                                    )
                                )
                                reviewForRequestId = null
                                reviewFeedback = ""
                                reviewRating = 5f
                                load()
                            } catch (e: HttpException) {
                                val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                                error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
                            } catch (e: Exception) {
                                error = e.message ?: "Failed"
                            } finally {
                                reviewSubmitting = false
                            }
                        }
                    },
                    enabled = !reviewSubmitting
                ) { Text(if (reviewSubmitting) "Submitting..." else "Submit") }
            },
            dismissButton = {
                TextButton(onClick = { if (!reviewSubmitting) reviewForRequestId = null }) { Text("Cancel") }
            }
        )
    }

    // -------------------- Complaint Dialog --------------------
    if (complaintForRequestId != null) {
        AlertDialog(
            onDismissRequest = { if (!complaintSubmitting) complaintForRequestId = null },
            shape = RoundedCornerShape(18.dp),
            title = { Text("Raise Complaint") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = complaintReason,
                        onValueChange = { complaintReason = it },
                        label = { Text("Reason (required)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !complaintSubmitting
                    )
                    OutlinedTextField(
                        value = complaintDetails,
                        onValueChange = { complaintDetails = it },
                        label = { Text("Details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !complaintSubmitting
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("Trust reduces only after verification") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rid = complaintForRequestId ?: return@Button
                        val reason = complaintReason.trim()
                        if (reason.isBlank()) {
                            error = "Complaint reason is required"
                            return@Button
                        }

                        scope.launch {
                            complaintSubmitting = true
                            error = null
                            try {
                                val reqItem = requests.firstOrNull { it.id == rid }
                                    ?: throw IllegalStateException("Request not found")

                                ApiClient.api.createComplaint(
                                    ComplaintRequest(
                                        requestId = rid,
                                        providerId = reqItem.providerId,
                                        userId = userId,
                                        reason = reason,
                                        details = complaintDetails.trim().takeIf { it.isNotBlank() }
                                    )
                                )

                                complaintForRequestId = null
                                complaintReason = ""
                                complaintDetails = ""
                                load()
                            } catch (e: HttpException) {
                                val msg = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                                error = msg?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
                            } catch (e: Exception) {
                                error = e.message ?: "Failed"
                            } finally {
                                complaintSubmitting = false
                            }
                        }
                    },
                    enabled = !complaintSubmitting
                ) { Text(if (complaintSubmitting) "Submitting..." else "Submit") }
            },
            dismissButton = {
                TextButton(onClick = { if (!complaintSubmitting) complaintForRequestId = null }) { Text("Cancel") }
            }
        )
    }

    // -------------------- Screen (Login-style) --------------------
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
                    .align(Alignment.Center)
                    .padding(18.dp)
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("My Requests", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Track pending / completed services",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Header card
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Requests", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    requests.size.toString(),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }

                            PrimaryButton(
                                text = if (loading) "Refreshing..." else "Refresh",
                                enabled = !loading,
                                modifier = Modifier.widthIn(min = 120.dp)
                            ) { load() }
                        }

                        error?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    if (loading && requests.isEmpty()) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        return@Card
                    }

                    if (error != null && requests.isEmpty()) {
                        Column {
                            Text(error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(10.dp))
                            PrimaryButton(text = "Retry") { load() }
                        }
                        return@Card
                    }

                    if (requests.isEmpty()) {
                        Text("No requests yet.")
                        return@Card
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 10.dp)
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

                                        StatusChip(status)

                                        Spacer(Modifier.height(8.dp))
                                        Text("Service: ${r.serviceType}")
                                    }
                                }

                                r.description?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Description: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                r.address?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Address: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(Modifier.height(12.dp))

                                if (status == "PENDING" || status == "ACCEPTED") {
                                    PrimaryButton(
                                        text = if (busy) "Working..." else "Cancel",
                                        enabled = !busy,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        scope.launch {
                                            runAction(requestId) { ApiClient.api.cancelRequestUser(requestId) }
                                        }
                                    }
                                }

                                if (status == "COMPLETED") {
                                    Spacer(Modifier.height(10.dp))

                                    val complaintDone = r.complaintRaised   // new flag from backend
                                    val reviewDone = r.reviewed

                                    if (!complaintDone) {
                                        PrimaryButton(text = "Raise Complaint") {
                                            complaintForRequestId = requestId
                                            complaintReason = ""
                                            complaintDetails = ""
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }

                                    if (!reviewDone) {
                                        PrimaryButton(text = "Add Review") {
                                            reviewForRequestId = requestId
                                            reviewRating = 5f
                                            reviewFeedback = ""
                                        }
                                    } else {
                                        Text(
                                            text = "Reviewed: ${r.rating ?: "-"} / 5",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        r.feedback?.takeIf { it.isNotBlank() }?.let {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                it,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Optional: show a small info when both are done
                                    if (complaintDone && reviewDone) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Review & complaint already submitted",
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
}

@Composable
private fun StatusChip(status: String) {
    val label = when (status) {
        "PENDING" -> "Pending"
        "ACCEPTED" -> "Accepted"
        "COMPLETED" -> "Completed"
        "CANCELLED_BY_USER" -> "Cancelled by you"
        "CANCELLED_BY_PROVIDER" -> "Cancelled by provider"
        else -> status
    }

    AssistChip(
        onClick = { },
        label = { Text(label) }
    )
}