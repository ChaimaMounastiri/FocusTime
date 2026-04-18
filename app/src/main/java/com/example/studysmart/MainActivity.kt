package com.example.studysmart

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.studysmart.presentation.NavGraphs
import com.example.studysmart.presentation.destinations.SessionScreenRouteDestination
import com.example.studysmart.presentation.session.StudySessionTimerService
import com.example.studysmart.presentation.theme.FocusTimeTheme
import com.example.studysmart.presentation.theme.LocalThemeController
import com.example.studysmart.presentation.theme.ThemeController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

private const val PREFS = "focustime_prefs"
private const val KEY_THEME = "theme_mode"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(false)
    private lateinit var timerService: StudySessionTimerService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as StudySessionTimerService.StudySessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, StudySessionTimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            if (isBound) {
                val prefs = remember { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
                var themeMode by rememberSaveable {
                    mutableStateOf(prefs.getString(KEY_THEME, "system") ?: "system")
                }
                LaunchedEffect(themeMode) {
                    prefs.edit().putString(KEY_THEME, themeMode).apply()
                }
                val darkTheme = when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
                val themeController = ThemeController(mode = themeMode, setMode = { themeMode = it })
                CompositionLocalProvider(LocalThemeController provides themeController) {
                    FocusTimeTheme(darkTheme = darkTheme) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppScreenBackground()
                            DestinationsNavHost(
                                navGraph = NavGraphs.root,
                                modifier = Modifier.fillMaxSize(),
                                dependenciesContainerBuilder = {
                                    dependency(SessionScreenRouteDestination) { timerService }
                                }
                            )
                        }
                    }
                }
            }
        }
        requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}

@Composable
private fun AppScreenBackground() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.background,
                        colors.primaryContainer.copy(alpha = 0.18f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {}
}
