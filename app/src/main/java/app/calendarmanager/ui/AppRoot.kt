package app.calendarmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.calendarmanager.R
import app.calendarmanager.ui.components.SectionScreen
import app.calendarmanager.ui.navigation.Routes
import app.calendarmanager.ui.navigation.TopLevel
import app.calendarmanager.ui.settings.SettingsScreen
import app.calendarmanager.ui.settings.SettingsViewModel
import app.calendarmanager.ui.study.StudyScreen
import app.calendarmanager.ui.study.StudyViewModel
import app.calendarmanager.ui.work.WorkScreen
import app.calendarmanager.ui.work.WorkViewModel
import app.calendarmanager.ui.theme.LocalCategoryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(settingsViewModel: SettingsViewModel, studyViewModel: StudyViewModel, workViewModel: WorkViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val inSettings = route == Routes.SETTINGS
    val colors = LocalCategoryColors.current
    val title = TopLevel.entries.firstOrNull { it.route == route }?.label
        ?: if (inSettings) R.string.settings else R.string.nav_dashboard

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    if (inSettings) IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!inSettings) IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.settings))
                    }
                },
            )
        },
        bottomBar = {
            if (!inSettings) NavigationBar {
                TopLevel.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = route == dest.route,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(stringResource(dest.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = TopLevel.DASHBOARD.route, modifier = Modifier.padding(padding)) {
            composable(TopLevel.DASHBOARD.route) {
                SectionScreen(TopLevel.DASHBOARD.testTag, TopLevel.DASHBOARD.icon, MaterialTheme.colorScheme.primary)
            }
            composable(TopLevel.STUDY.route) { StudyScreen(studyViewModel) }
            composable(TopLevel.WORK.route) { WorkScreen(workViewModel) }
            composable(TopLevel.TASKS.route) { SectionScreen(TopLevel.TASKS.testTag, TopLevel.TASKS.icon, colors.task) }
            composable(TopLevel.CALENDAR.route) {
                SectionScreen(TopLevel.CALENDAR.testTag, TopLevel.CALENDAR.icon, MaterialTheme.colorScheme.primary)
            }
            composable(Routes.SETTINGS) { SettingsScreen(settingsViewModel) }
        }
    }
}
