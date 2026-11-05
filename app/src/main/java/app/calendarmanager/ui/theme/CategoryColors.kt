package app.calendarmanager.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CategoryColors(
    val study: Color, val work: Color, val task: Color, val exam: Color, val assignment: Color,
)

val LightCategoryColors = CategoryColors(
    study = Color(0xFF3F6FD8), work = Color(0xFF2E9E6B), task = Color(0xFFE08A1E),
    exam = Color(0xFFD0453D), assignment = Color(0xFF8A55C7),
)
val DarkCategoryColors = CategoryColors(
    study = Color(0xFF8AA9FF), work = Color(0xFF6FD3A2), task = Color(0xFFFFB866),
    exam = Color(0xFFFF8A82), assignment = Color(0xFFC9A0F2),
)

val LocalCategoryColors = staticCompositionLocalOf { LightCategoryColors }
