package ru.carport.app

import android.app.Application
import ru.carport.app.components.AppComponent
import ru.carport.app.components.DaggerAppComponent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        var fbToken: String = ""
        var cpToken: String = ""

        fun getComponent(): AppComponent {
            return DaggerAppComponent.create()
        }
    }
}