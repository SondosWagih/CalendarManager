package app.calendarmanager

import android.app.Application
import app.calendarmanager.di.AppContainer

class CalendarApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
