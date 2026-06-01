package com.servitrust.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import com.servitrust.app.ui.theme.ServiTypography

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = BrandDark,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = BackgroundLight,
    onBackground = TextStrong,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = TextStrong,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = TextSoft,
    error = ErrorRed
)

@Composable
fun ServiTrustTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ServiTypography,
        content = content
    )
}