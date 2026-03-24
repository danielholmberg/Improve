package dev.danielholmberg.improve.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand colors
val Primary = Color(0xFF0097A7)
val PrimaryDark = Color(0xFF006978)
val PrimaryLight = Color(0xFF56C8D8)
val Secondary = Color(0xFFFFA726)
val SecondaryDark = Color(0xFFC77800)
val SecondaryLight = Color(0xFFFFD95B)
val Background = Color(0xFF80DEEA)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFB00020)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFF000000)
val OnSurface = Color(0xFF000000)
val OnError = Color(0xFFFFFFFF)

// Dark theme variants
val PrimaryDarkTheme = Color(0xFF56C8D8)
val SecondaryDarkTheme = Color(0xFFFFD95B)
val BackgroundDarkTheme = Color(0xFF121212)
val SurfaceDarkTheme = Color(0xFF1E1E1E)
val ErrorDarkTheme = Color(0xFFCF6679)
val OnPrimaryDarkTheme = Color(0xFF000000)
val OnSecondaryDarkTheme = Color(0xFF000000)
val OnBackgroundDarkTheme = Color(0xFFFFFFFF)
val OnSurfaceDarkTheme = Color(0xFFFFFFFF)
val OnErrorDarkTheme = Color(0xFF000000)

val ImproveTagColors: List<String> = listOf(
    "#2F4550",
    "#DA2C38",
    "#2F1847",
    "#0E79B2",
    "#60463B",
    "#EF709D",
    "#63C132",
    "#FF7B00",
)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = Color(0xFF2C1700),
    tertiary = Color(0xFF006978),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF97F0FF),
    onTertiaryContainer = Color(0xFF001F24),
    error = Error,
    onError = OnError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFDBE4E7),
    onSurfaceVariant = Color(0xFF3F484B),
    outline = Color(0xFF6F797C),
    outlineVariant = Color(0xFFBFC8CB),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2D3133),
    inverseOnSurface = Color(0xFFEFF1F2),
    inversePrimary = PrimaryLight,
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkTheme,
    onPrimary = OnPrimaryDarkTheme,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = SecondaryDarkTheme,
    onSecondary = OnSecondaryDarkTheme,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = Color(0xFFFFDDB4),
    tertiary = Color(0xFF4FD8EB),
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F57),
    onTertiaryContainer = Color(0xFF97F0FF),
    error = ErrorDarkTheme,
    onError = OnErrorDarkTheme,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = BackgroundDarkTheme,
    onBackground = OnBackgroundDarkTheme,
    surface = SurfaceDarkTheme,
    onSurface = OnSurfaceDarkTheme,
    surfaceVariant = Color(0xFF3F484B),
    onSurfaceVariant = Color(0xFFBFC8CB),
    outline = Color(0xFF899295),
    outlineVariant = Color(0xFF3F484B),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E3E4),
    inverseOnSurface = Color(0xFF2D3133),
    inversePrimary = Primary,
)
