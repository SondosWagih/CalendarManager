package app.calendarmanager.di

import android.content.Context
import app.calendarmanager.data.local.db.AppDatabase
import app.calendarmanager.data.repository.RoomDeviceIdProvider
import app.calendarmanager.domain.repository.DeviceIdProvider

/** Manual dependency container (no DI framework to keep the build lean and fully offline). */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val deviceIdProvider: DeviceIdProvider = RoomDeviceIdProvider(database.appSettingDao())
}
