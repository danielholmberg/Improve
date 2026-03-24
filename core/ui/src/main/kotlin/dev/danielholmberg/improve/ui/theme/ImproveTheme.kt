package dev.danielholmberg.improve.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The top-level theme composable for the Improve app.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to the system setting.
 * @param dynamicColor Whether to use Android 12+ dynamic (Material You) colors when available.
 *                     Defaults to false to preserve brand colors.
 * @param content The content to render inside this theme.
 */
@Composable
fun ImproveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ImproveTypography,
        shapes = ImproveShapes,
        content = content,
    )
}
