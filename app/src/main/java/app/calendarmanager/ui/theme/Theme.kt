package app.calendarmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCategoryColors provides if (dark) DarkCategoryColors else LightCategoryColors) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(), content = content)
    }
}
