package com.example.vibecheck_dev.di

import com.example.vibecheck_dev.data.local.UserPreferences
import com.example.vibecheck_dev.data.repository_impl.AuthRepositoryImpl
import com.example.vibecheck_dev.data.repository_impl.P2pRepositoryImpl
import com.example.vibecheck_dev.data.source.remote.dto.VibeCheckApi
import com.example.vibecheck_dev.domain.repository.AuthRepository
import com.example.vibecheck_dev.domain.repository.P2pRepository
import com.example.vibecheck_dev.presentation.auth.AuthViewModel
import com.example.vibecheck_dev.presentation.camera.CameraViewModel
import com.example.vibecheck_dev.presentation.purikura.PurikuraViewModel
import com.example.vibecheck_dev.presentation.remote.RemoteViewModel
import com.example.vibecheck_dev.presentation.studio.StudioViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // 1. FIREBASE & API
    single<FirebaseAuth> { FirebaseAuth.getInstance() }

    single<VibeCheckApi> {
        Retrofit.Builder()
            .baseUrl("http://10.112.214.184:8085/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VibeCheckApi::class.java)
    }

    // 2. REPOSITORY (Gw udah hapus yang dobel)
    single<P2pRepository> { P2pRepositoryImpl(androidContext()) }
    single<AuthRepository> { AuthRepositoryImpl(api = get(), firebaseAuth = get()) }

    // 3. PREFERENCES
    single { UserPreferences(androidContext()) }

    // 4. VIEWMODELS
    viewModel { CameraViewModel(get()) }
    viewModel { RemoteViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { StudioViewModel() }
    viewModel { PurikuraViewModel() }
}