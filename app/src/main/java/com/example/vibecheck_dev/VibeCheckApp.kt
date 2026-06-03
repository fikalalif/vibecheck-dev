package com.example.vibecheck_dev

import android.app.Application
import com.example.vibecheck_dev.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VibeCheckApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@VibeCheckApp)
            modules(appModule)
        }
    }
}