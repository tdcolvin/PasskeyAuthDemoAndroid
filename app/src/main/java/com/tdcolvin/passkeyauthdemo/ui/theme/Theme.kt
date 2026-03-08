package com.tdcolvin.passkeyauthdemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFDE1659),
    onPrimary = Color.White,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    background = Color.Black,
    onBackground = Color.White,

    surface = Color.Black,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = Color(0xFFDE1659),
    onBackground = Color.White,

    surface = Color(0xFF830C35),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF830C35),
    onSurfaceVariant = Color.White,

   /*primaryContainer = DarkGreen,
    onPrimaryContainer = DarkGreen,
    inversePrimary = DarkGreen,
    onSecondary = DarkGreen,
    secondaryContainer = DarkGreen,
    onSecondaryContainer = DarkGreen,
    onTertiary = DarkGreen,
    tertiaryContainer = DarkGreen,
    onTertiaryContainer = DarkGreen,
    surfaceTint = DarkGreen,
    inverseSurface = DarkGreen,
    inverseOnSurface = DarkGreen,
    error = DarkGreen,
    onError = DarkGreen,
    errorContainer = DarkGreen,
    onErrorContainer = DarkGreen,
    outline = DarkGreen,
    outlineVariant = DarkGreen,
    scrim = DarkGreen,
    surfaceBright = DarkGreen,*/
    surfaceContainer = Color(0xFF830C35),
    surfaceContainerHigh = Color(0xFF830C35),
    surfaceContainerHighest = Color(0xFF830C35),
    /*surfaceContainerLow = DarkGreen,
    surfaceContainerLowest = DarkGreen,
    surfaceDim = DarkGreen,
    primaryFixed = DarkGreen,
    primaryFixedDim = DarkGreen,
    onPrimaryFixed = DarkGreen,
    onPrimaryFixedVariant = DarkGreen,
    secondaryFixed = DarkGreen,
    secondaryFixedDim = DarkGreen,
    onSecondaryFixed = DarkGreen,
    onSecondaryFixedVariant = DarkGreen,
    tertiaryFixed = DarkGreen,
    tertiaryFixedDim = DarkGreen,
    onTertiaryFixed = DarkGreen,
    onTertiaryFixedVariant = DarkGreen*/

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PasskeyAuthDemoAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}