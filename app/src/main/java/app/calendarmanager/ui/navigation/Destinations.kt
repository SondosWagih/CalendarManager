package app.calendarmanager.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import app.calendarmanager.R

object Routes { const val SETTINGS = "settings" }

enum class TopLevel(val route: String, @StringRes val label: Int, val icon: ImageVector, val testTag: String) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Outlined.Dashboard, "screen_dashboard"),
    STUDY("study", R.string.nav_study, Icons.Outlined.School, "screen_study"),
    WORK("work", R.string.nav_work, Icons.Outlined.Work, "screen_work"),
    TASKS("tasks", R.string.nav_tasks, Icons.Outlined.TaskAlt, "screen_tasks"),
    CALENDAR("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth, "screen_calendar"),
}
