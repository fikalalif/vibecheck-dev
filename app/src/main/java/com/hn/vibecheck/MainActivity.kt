package com.hn.vibecheck

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hn.vibecheck.data.local.UserPreferences
import com.hn.vibecheck.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import com.hn.vibecheck.ui.theme.VibeCheckdevTheme
import com.hn.vibecheck.presentation.permission.PermissionScreen
import com.hn.vibecheck.presentation.home.HomeScreen
import com.hn.vibecheck.presentation.camera.CameraScreen
import com.hn.vibecheck.presentation.camera.CameraViewModel
import com.hn.vibecheck.presentation.remote.RemoteScreen
import com.hn.vibecheck.presentation.remote.RemoteViewModel
import com.hn.vibecheck.presentation.components.VibeBottomNav
import com.hn.vibecheck.presentation.auth.AuthScreen
import com.hn.vibecheck.presentation.auth.LoginScreen
import com.hn.vibecheck.presentation.auth.OnboardingScreen
import com.hn.vibecheck.presentation.auth.ProfileSetupScreen
import com.hn.vibecheck.presentation.auth.QuickAuthDialog
import com.hn.vibecheck.presentation.home.AnalyticsScreen
import com.hn.vibecheck.presentation.home.AnalyticsViewModel
import com.hn.vibecheck.presentation.studio.StudioScreen
import com.hn.vibecheck.presentation.studio.StudioViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FIKAL_DEBUG", "Aplikasi VibeCheck Mulai Berjalan!")

        setContent {
            val context = LocalContext.current
            val userPreferences = remember { UserPreferences(context) }
            val activeTheme by userPreferences.themeFlow.collectAsState(initial = "Y2K BRIGHT NEON")

            VibeCheckdevTheme(themeName = activeTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(userPreferences) // Lempar data memori ke AppNavigation
                }
            }
        }
    }
}

@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isFirstTime by userPreferences.isFirstTimeFlow.collectAsState(initial = null)
    val playerName by userPreferences.playerNameFlow.collectAsState(initial = null)
    val isLoggedIn by userPreferences.isLoggedInFlow.collectAsState(initial = null)
    val activeTheme by userPreferences.themeFlow.collectAsState(initial = "Y2K BRIGHT NEON")

    if (isFirstTime == null || isLoggedIn == null) {
        return // Tunggu DataStore kelar baca
    }

    // LOGIKA PENENTUAN GUEST MODE
    // Guest = Udah punya nama (lewat setup), tapi status login-nya false
    val isGuestMode = !playerName.isNullOrEmpty() && isLoggedIn == false
    val isRealUser = isLoggedIn == true

    // 🔴 1. ATURAN BOTTOM NAVIGATION (Semua bisa liat kalau lagi di halaman utama)
    val isMainScreen = currentRoute in listOf(Screen.Home.route, Screen.Studio.route, Screen.Purikura.route)
    val showBottomNav = isMainScreen

    // 🔴 2. STATE UNTUK DIALOG TIKTOK-STYLE
    var showQuickAuthDialog by remember { mutableStateOf(false) }
    var pendingRoute by remember { mutableStateOf<String?>(null) } // Catat rute tujuan

    // 🔴 3. LOGIKA ROUTING PINTAR (Tentukan tujuan SETELAH Splash Screen beres)
    val nextDestination = when {
        isFirstTime == true -> Screen.Onboarding.route
        isRealUser || isGuestMode -> Screen.Home.route
        else -> Screen.Auth.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                VibeBottomNav(
                    navController = navController,
                    isGuestMode = isGuestMode,
                    onAuthRequested = { targetRoute ->
                        // 🔴 Cegat user, catat rutenya, lalu munculin form dialog!
                        pendingRoute = targetRoute
                        showQuickAuthDialog = true
                    }
                )
            }
        }) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route, // 🔴 SELALU MULAI DARI SPLASH!
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    com.hn.vibecheck.presentation.splash.SplashScreen(
                        navController = navController,
                        nextRoute = nextDestination // 🔴 Lempar tujuan aslinya ke Splash
                    )
                }

                // --- LAYAR SETUP & ONBOARDING ---
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            // 🔴 FIX: Kasih tau DataStore kalau Onboarding udah selesai
                            coroutineScope.launch {
                                userPreferences.setFirstTimeCompleted()
                            }

                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        })
                }

                composable(Screen.Auth.route) {
                    AuthScreen(
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToGuest = { navController.navigate(Screen.ProfileSetup.route) }
                    )
                }

                composable(Screen.ProfileSetup.route) {
                    ProfileSetupScreen(
                        onSaveSuccess = {
                            // Guest Baru -> Habis setup lempar ke Permission
                            navController.navigate(Screen.Permission.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }, userPreferences = userPreferences
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            // User Baru -> Habis login lempar ke Permission
                            navController.navigate(Screen.Permission.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }, userPreferences = userPreferences
                    )
                }

                // --- LAYAR UTAMA ---
                composable(Screen.Permission.route) {
                    PermissionScreen(
                        onAllPermissionsGranted = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Permission.route) { inclusive = true }
                            }
                        })
                }

                composable(Screen.Home.route) {
                    // Cek lagi karena State bisa berubah on-the-fly
                    val currentIsGuest = !playerName.isNullOrEmpty() && isLoggedIn == false

                    HomeScreen(
                        onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                        onNavigateToRemote = { navController.navigate(Screen.Remote.route) },
                        onNavigateToLogs = { navController.navigate(Screen.SystemLog.route) },
                        onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                        onLogout = {
                            coroutineScope.launch {
                                userPreferences.saveAuthSession(token = "", isLogged = false)
                                userPreferences.savePlayerName("")

                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true } // Kill semua backstack
                                }
                            }
                        },
                        isGuestMode = currentIsGuest,
                        guestName = playerName ?: "GUEST_USER",
                        activeThemeName = activeTheme,
                        onThemeChanged = { newTheme ->
                            coroutineScope.launch { userPreferences.saveTheme(newTheme) }
                        }
                    )
                }

                composable(Screen.Camera.route) {
                    val cameraViewModel: CameraViewModel = koinViewModel()
                    CameraScreen(viewModel = cameraViewModel, onNavigateBack = { navController.popBackStack() })
                }

                composable(Screen.Remote.route) {
                    val remoteViewModel: RemoteViewModel = koinViewModel()
                    RemoteScreen(viewModel = remoteViewModel, onNavigateBack = { navController.popBackStack() })
                }

                // STUDIO & PURIKURA
                composable(Screen.Studio.route) {
                    if (isRealUser) {
                        val studioViewModel: StudioViewModel = koinViewModel()
                        StudioScreen(viewModel = studioViewModel)
                    }
                }

                composable(Screen.Purikura.route) {
                    if (isRealUser) {
                        val purikuraViewModel: com.hn.vibecheck.presentation.purikura.PurikuraViewModel = koinViewModel()
                        com.hn.vibecheck.presentation.purikura.PurikuraScreen(viewModel = purikuraViewModel)
                    }
                }
                composable(Screen.SystemLog.route) {
                    // Panggil koinViewModel biar dia otomatis ngambil HomeViewModel
                    val homeViewModel: com.hn.vibecheck.presentation.home.HomeViewModel = koinViewModel()
                    com.hn.vibecheck.presentation.home.SystemLogScreen(viewModel = homeViewModel)
                }
                composable(Screen.Analytics.route) {
                    val analyticsViewModel: AnalyticsViewModel = koinViewModel()
                    AnalyticsScreen(viewModel = analyticsViewModel)
                }
            }

            // 🔴 4. RENDER DIALOG AUTH DI ATAS SEMUA LAYAR
            if (showQuickAuthDialog) {
                QuickAuthDialog(
                    onDismiss = {
                        showQuickAuthDialog = false
                        pendingRoute = null // Reset niat navigasi kalau batal
                    },
                    onSuccess = {
                        showQuickAuthDialog = false

                        // MAGIC: Langsung loncat ke layar yg diklik tadi!
                        pendingRoute?.let { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            pendingRoute = null
                        }
                    },
                    userPreferences = userPreferences
                )
            }
        }
    }
}