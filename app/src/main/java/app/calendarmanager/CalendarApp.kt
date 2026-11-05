package app.calendarmanager

import android.app.Application
import app.calendarmanager.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarApp : Application() {
    lateinit var container: AppContainer
        private set
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Make sure the local device identifier exists from the very first launch.
        appScope.launch { container.deviceIdProvider.getOrCreate() }
    }
}
