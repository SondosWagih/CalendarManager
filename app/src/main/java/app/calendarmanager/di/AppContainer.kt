package app.calendarmanager.di

import android.content.Context
import app.calendarmanager.data.local.db.AppDatabase
import app.calendarmanager.data.repository.RoomDeviceIdProvider
import app.calendarmanager.data.repository.StudyRepository
import app.calendarmanager.data.settings.SettingsRepository
import app.calendarmanager.data.settings.settingsDataStore
import app.calendarmanager.domain.repository.DeviceIdProvider

/** Manual dependency container (no DI framework to keep the build lean and fully offline). */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val studyRepository = StudyRepository(database)
    val settingsRepository = SettingsRepository(context.applicationContext.settingsDataStore)
    val deviceIdProvider: DeviceIdProvider = RoomDeviceIdProvider(database.appSettingDao())
}
