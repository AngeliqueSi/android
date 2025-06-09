package ru.yavshok.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.yavshok.app.data.storage.TokenStorage
import ru.yavshok.app.ui.screens.MainScreen
import ru.yavshok.app.ui.screens.SplashScreen
import ru.yavshok.app.ui.screens.login.LoginScreen
import ru.yavshok.app.ui.screens.register.RegisterScreen
import ru.yavshok.app.ui.screens.profile.ProfileScreen
import ru.yavshok.app.ui.screens.profile.EditProfileScreen
import ru.yavshok.app.ui.theme.YavshokTheme
import ru.yavshok.app.viewmodel.ViewModelFactory
import ru.yavshok.app.viewmodel.ProfileViewModel
import ru.yavshok.app.viewmodel.EditProfileViewModel
import ru.yavshok.app.viewmodel.SplashViewModel

enum class Screen {
    SPLASH, MAIN, LOGIN, REGISTER, PROFILE, EDIT_PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize App Metrica with API key from BuildConfig
        val config = AppMetricaConfig.newConfigBuilder(BuildConfig.APP_METRICA_API_KEY).build()
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this.application)

        setContent {
            YavshokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tokenStorage = remember { TokenStorage(this@MainActivity) }
                    val viewModelFactory = remember { ViewModelFactory(this@MainActivity) }
                    
                    // Simple navigation state with logging
                    var currentScreen by remember { 
                        mutableStateOf(Screen.SPLASH).also {
                            Log.d("MainActivity", "🟢 Initial currentScreen set to: SPLASH")
                        }
                    }
                    var isLoggedIn by remember { 
                        mutableStateOf(tokenStorage.isLoggedIn()).also {
                            Log.d("MainActivity", "🟢 Initial isLoggedIn set to: ${tokenStorage.isLoggedIn()}")
                        }
                    }


                    
                    // Log state changes
                    LaunchedEffect(currentScreen) {
                        Log.d("MainActivity", "🔄 Navigation changed to: ${currentScreen.name}")
                    }
                    
                    LaunchedEffect(isLoggedIn) {
                        Log.d("MainActivity", "🔄 Login state changed to: $isLoggedIn")
                    }
                    
                    // Note: Initial navigation now handled by SplashScreen

                    when (currentScreen) {
                        Screen.SPLASH -> {
                            Log.d("MainActivity", "🌟 Rendering SPLASH screen")
                            val splashViewModel: SplashViewModel = viewModel(factory = viewModelFactory)
                            SplashScreen(
                                viewModel = splashViewModel,
                                onNavigate = { userIsLoggedIn ->
                                    Log.d("MainActivity", "🚀 SPLASH navigation - isLoggedIn: $userIsLoggedIn")
                                    isLoggedIn = userIsLoggedIn
                                    currentScreen = if (userIsLoggedIn) Screen.PROFILE else Screen.MAIN
                                }
                            )
                        }
                        Screen.MAIN -> {
                            Log.d("MainActivity", "📱 Rendering MAIN screen")
                            MainScreen(
                                onNavigateToLogin = {
                                    Log.d("MainActivity", "🔵 MAIN -> LOGIN button clicked")
                                    AppMetrica.reportEvent("click.login_button")
                                    val eventParameters = """{"from":"Main","to":"Login"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }
                        Screen.LOGIN -> {
                            Log.d("MainActivity", "🔐 Rendering LOGIN screen")
                            val loginViewModel: ru.yavshok.app.viewmodel.LoginViewModel = viewModel(factory = viewModelFactory)
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = {
                                    Log.d("MainActivity", "🔵 LOGIN -> REGISTER clicked")
                                    AppMetrica.reportEvent("click.register_link")
                                    val eventParameters = """{"from":"Login","to":"Register"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.REGISTER
                                },
                                onNavigateBack = {
                                    Log.d("MainActivity", "🔙 LOGIN -> MAIN back clicked")
                                    AppMetrica.reportEvent("click.back_button")
                                    val eventParameters = """{"from":"Login","to":"Main"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.MAIN
                                },
                                onLoginSuccess = {
                                    Log.d("MainActivity", "✅ LOGIN SUCCESS -> PROFILE")
                                    AppMetrica.reportEvent("login.success")
                                    val eventParameters = """{"from":"Login","to":"Profile"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    isLoggedIn = true
                                    currentScreen = Screen.PROFILE
                                }
                            )
                        }
                        Screen.REGISTER -> {
                            val registerViewModel: ru.yavshok.app.viewmodel.RegisterViewModel = viewModel(factory = viewModelFactory)
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateBack = {
                                    AppMetrica.reportEvent("click.back_button")
                                    val eventParameters = """{"from":"Register","to":"Login"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.LOGIN
                                },
                                onRegistrationSuccess = {
                                    AppMetrica.reportEvent("register.success")
                                    val eventParameters = """{"from":"Register","to":"Profile"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    isLoggedIn = true
                                    currentScreen = Screen.PROFILE
                                }
                            )
                        }
                        Screen.PROFILE -> {
                            Log.d("MainActivity", "👤 Rendering PROFILE screen - tokenStorage.isLoggedIn(): ${tokenStorage.isLoggedIn()}")
                            if (tokenStorage.isLoggedIn()) {
                                // Create a stable key for ViewModel scoping
                                val profileKey = "profile_user"
                                val profileViewModel: ProfileViewModel = viewModel(
                                    key = profileKey,
                                    factory = viewModelFactory
                                )
                                ProfileScreen(
                                    viewModel = profileViewModel,
                                    onEditProfileClick = {
                                        Log.d("MainActivity", "✏️ PROFILE -> EDIT_PROFILE clicked")
                                        AppMetrica.reportEvent("click.edit_profile")
                                        val eventParameters = """{"from":"Profile","to":"EditProfile"}"""
                                        AppMetrica.reportEvent("navigate", eventParameters)
                                        currentScreen = Screen.EDIT_PROFILE
                                    },
                                    onLogout = {
                                        Log.d("MainActivity", "🚪 LOGOUT clicked - clearing state")
                                        AppMetrica.reportEvent("click.logout")
                                        AppMetrica.reportEvent("logout.success")
                                        val eventParameters = """{"from":"Profile","to":"Main"}"""
                                        AppMetrica.reportEvent("navigate", eventParameters)
                                        isLoggedIn = false
                                        currentScreen = Screen.MAIN
                                    }
                                )
                            } else {
                                Log.d("MainActivity", "❌ PROFILE screen but not logged in - redirecting to MAIN")
                                LaunchedEffect(Unit) {
                                    Log.d("MainActivity", "🔄 LaunchedEffect redirecting PROFILE -> MAIN")
                                    currentScreen = Screen.MAIN
                                }
                            }
                        }
                        Screen.EDIT_PROFILE -> {
                            Log.d("MainActivity", "✏️ Rendering EDIT_PROFILE screen")
                            val editProfileViewModel: EditProfileViewModel = viewModel(factory = viewModelFactory)
                            EditProfileScreen(
                                viewModel = editProfileViewModel,
                                onNavigateBack = {
                                    Log.d("MainActivity", "🔙 EDIT_PROFILE -> PROFILE back clicked")
                                    AppMetrica.reportEvent("click.back_button")
                                    val eventParameters = """{"from":"EditProfile","to":"Profile"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.PROFILE
                                },
                                onProfileUpdated = {
                                    Log.d("MainActivity", "✅ PROFILE UPDATED -> PROFILE")
                                    AppMetrica.reportEvent("profile.update_success")
                                    val eventParameters = """{"from":"EditProfile","to":"Profile"}"""
                                    AppMetrica.reportEvent("navigate", eventParameters)
                                    currentScreen = Screen.PROFILE
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
