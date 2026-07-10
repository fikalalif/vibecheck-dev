package com.hn.vibecheck.di

import com.hn.vibecheck.data.remote.network.AuthInterceptor
import com.hn.vibecheck.data.remote.network.ImgBBApi
import com.hn.vibecheck.data.remote.network.VibeCheckApi
import com.hn.vibecheck.data.repository_impl.P2pRepositoryImpl
import com.hn.vibecheck.data.repository_impl.UserRepositoryImpl
import com.hn.vibecheck.domain.repository.P2pRepository
import com.hn.vibecheck.domain.repository.UserRepository
import com.hn.vibecheck.presentation.auth.AuthViewModel
import com.hn.vibecheck.presentation.camera.CameraViewModel
import com.hn.vibecheck.presentation.home.AnalyticsViewModel
import com.hn.vibecheck.presentation.home.HomeViewModel
import com.hn.vibecheck.presentation.purikura.PurikuraViewModel
import com.hn.vibecheck.presentation.remote.RemoteViewModel
import com.hn.vibecheck.presentation.studio.StudioViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single<P2pRepository> { P2pRepositoryImpl(androidContext()) }

    // --- TAMBAHAN UNTUK AUTH ---
    single { FirebaseAuth.getInstance() }

    viewModel { CameraViewModel(get(), get()) }
    viewModel { RemoteViewModel(get(), get()) }


    viewModel { StudioViewModel(get()) }
    // 🔴 TAMBAHIN get() BUAT PURIKURA
    viewModel { PurikuraViewModel(get()) }
    // --- TAMBAHAN UNTUK AUTH ---
    viewModel { AuthViewModel(get(), get()) }
    single { AuthInterceptor(get()) }
    viewModel { AnalyticsViewModel(get()) }

    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Biar bisa liat log API di Logcat
        }
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    single {
        Retrofit.Builder()
            // 🔴 URL INI NANTI GANTI PAKE URL VERCEL LU KALAU UDAH DEPLOY (misal: https://vibecheck.vercel.app/)
            .baseUrl("https://vibecheck-backend-jshl.vercel.app/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ImgBBApi> {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ImgBBApi::class.java)
    }

    single<VibeCheckApi> { get<Retrofit>().create(VibeCheckApi::class.java) }

    // --- REPOSITORY ---
    single<UserRepository> { UserRepositoryImpl(get()) }

    // --- VIEW MODEL UPDATE ---
    // Ubah HomeViewModel lu biar nerima UserRepository juga
    viewModel { HomeViewModel(get(), get(), get()) }
}