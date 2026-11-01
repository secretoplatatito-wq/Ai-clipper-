package com.example.clipforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = StudioPrimary,
  onPrimary = Color.White,
  primaryContainer = StudioPrimaryVariant,
  onPrimaryContainer = Color(0xFFEDE9FE),
  secondary = StudioSecondary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF78350F),
  onSecondaryContainer = Color(0xFFFEF3C7),
  tertiary = StudioTertiary,
  onTertiary = Color.Black,
  background = StudioBackground,
  onBackground = StudioTextPrimary,
  surface = StudioSurface,
  onSurface = StudioTextPrimary,
  surfaceVariant = StudioSurfaceElevated,
  onSurfaceVariant = StudioTextSecondary,
  outline = StudioSurfaceBorder,
  error = StudioError,
  onError = Color.White
)

@Composable
fun ClipForgeTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
