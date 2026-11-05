package app.calendarmanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.calendarmanager.domain.model.ThemeMode
import app.calendarmanager.ui.AppRoot
import app.calendarmanager.ui.settings.SettingsViewModel
import app.calendarmanager.ui.study.StudyViewModel
import app.calendarmanager.ui.work.WorkViewModel
import app.calendarmanager.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CalendarApp).container
        setContent {
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container.settingsRepository))
            val studyVm: StudyViewModel = viewModel(factory = StudyViewModel.factory(container.studyRepository))
            val workVm: WorkViewModel = viewModel(factory = WorkViewModel.factory(container.workRepository))
            val mode by vm.themeMode.collectAsStateWithLifecycle()
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppTheme(dark) { AppRoot(vm, studyVm, workVm) }
        }
    }
}
