package com.servitrust.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.servitrust.app.R
import com.servitrust.app.ui.components.PrimaryButton
import com.servitrust.app.ui.components.SecondaryButton
import com.servitrust.app.ui.theme.TextSoft

@Composable
fun RoleSelectScreen(
    onUserClick: () -> Unit,
    onProviderClick: () -> Unit,
    onAdminClick: () -> Unit
) {
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
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.servitrust_logo),
                            contentDescription = "ServiTrust Logo",
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ServiTrust", style = MaterialTheme.typography.headlineSmall)
                            Text("Trust-first local services", color = TextSoft)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Choose Role",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Select how you want to continue",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    PrimaryButton(
                        text = "Continue as User",
                        modifier = Modifier.fillMaxWidth()
                    ) { onUserClick() }

                    PrimaryButton(
                        text = "Continue as Provider",
                        modifier = Modifier.fillMaxWidth()
                    ) { onProviderClick() }

                    SecondaryButton(
                        text = "Admin",
                        modifier = Modifier.fillMaxWidth()
                    ) { onAdminClick() }

                    Divider()

                    Text(
                        text = "Providers are ranked by trust score so users can choose safer, more reliable services.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}