package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.parent.ParentMainScreen
import com.example.ui.screens.pro.ProMainScreen
import com.example.ui.screens.splash.DemoSelectorScreen
import com.example.ui.screens.splash.LoginScreen
import com.example.ui.screens.splash.RegisterScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.VacciTrackViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val application = context.applicationContext as Application
                val viewModel: VacciTrackViewModel = viewModel(
                    factory = VacciTrackViewModel.provideFactory(application)
                )

                val destination by viewModel.destination.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiMessage) {
                    uiMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg.text,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearUiMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = destination, label = "AppNav") { targetDest ->
                            when (targetDest) {
                                AppDestination.SPLASH -> SplashScreen(viewModel = viewModel)
                                AppDestination.LOGIN -> LoginScreen(viewModel = viewModel)
                                AppDestination.REGISTER -> RegisterScreen(viewModel = viewModel)
                                AppDestination.DEMO_SELECTOR -> DemoSelectorScreen(viewModel = viewModel)
                                AppDestination.PARENT_PORTAL -> ParentMainScreen(viewModel = viewModel)
                                AppDestination.PRO_PORTAL -> ProMainScreen(viewModel = viewModel)
                                AppDestination.ADMIN_PORTAL -> AdminMainScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
