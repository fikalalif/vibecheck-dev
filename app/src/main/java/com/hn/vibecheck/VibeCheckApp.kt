package com.hn.vibecheck

import android.app.Application
import com.hn.vibecheck.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VibeCheckApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Menyalakan mesin Koin saat aplikasi pertama kali dibuka
        startKoin {
            androidLogger() // Untuk melihat log error Koin di Logcat
            androidContext(this@VibeCheckApp)
            modules(appModule) // Memuat pabrik objek yang kita buat tadi
        }
    }
}