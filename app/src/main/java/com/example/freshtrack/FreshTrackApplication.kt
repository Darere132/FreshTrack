package com.example.freshtrack

import android.app.Application
import com.example.freshtrack.data.AppContainer
import com.example.freshtrack.data.AppDataContainer

class FreshTrackApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}