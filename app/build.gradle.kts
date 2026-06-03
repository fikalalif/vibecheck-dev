plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kalau mau pake Firebase, pastikan plugin google services ada di sini kalau lu pake file google-services.json
     id("com.google.gms.google-services") // Nyalain ini kalau lu udah donlot google-services.json
}

android {
    namespace = "com.example.vibecheck_dev"
    compileSdk = 36 // Turunin ke 34 biar stabil

    defaultConfig {
        applicationId = "com.example.vibecheck_dev"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BACA DARI local.properties BUAT GOOGLE LOGIN
        val googleWebClientId = project.findProperty("GOOGLE_WEB_CLIENT_ID") as? String ?: ""
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // WAJIB NYALA BIAR BISA BACA API KEY!
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // --- FIREBASE & GOOGLE AUTH (MODERN) ---
    // Pake BOM biar versi Firebase seragam dan gak tabrakan
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-common-ktx")

    // Credential Manager (G-Auth)
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // --- RETROFIT & GSON (BUAT NEMBAK SPRING BOOT) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- DEPENDENCIES LAMA LU ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Compose Navigation & Icons
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // CameraX
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-video:1.3.0")

    // ML Kit (Pose Detection)
    implementation("com.google.mlkit:pose-detection:17.0.0")

    // Ktor Server (P2P Socket)
    val ktor_version = "2.3.7"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")

    // WebRTC (P2P Video)
    implementation("io.getstream:stream-webrtc-android:1.1.1")

    // Coroutines & DataStore & Koin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Penting buat Firebase Coroutine (.await)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    implementation("androidx.print:print:1.0.0")
}