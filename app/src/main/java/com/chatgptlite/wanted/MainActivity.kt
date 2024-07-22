package com.chatgptlite.wanted

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chatgptlite.wanted.ui.NavRoute
import com.chatgptlite.wanted.ui.common.AppBar
import com.chatgptlite.wanted.ui.common.AppScaffold
import com.chatgptlite.wanted.ui.conversations.Conversation
import com.chatgptlite.wanted.ui.settings.SettingsScreen
import com.chatgptlite.wanted.ui.settings.MlCModelSettings
import com.chatgptlite.wanted.ui.settings.MlcModelSettingsViewModel
import com.chatgptlite.wanted.ui.theme.ChatGPTLiteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val drawerOpen by mainViewModel.drawerShouldBeOpened.collectAsState()
                    val modelViewController = viewModel<MlcModelSettingsViewModel>()

                    if (drawerOpen) {
                        // Open drawer and reset state in VM.
                        LaunchedEffect(Unit) {
                            // wrap in try-finally to handle interruption whiles opening drawer
                            try {
                                drawerState.open()
                            } finally {
                                mainViewModel.resetOpenDrawerAction()
                            }
                        }
                    }

                    // Intercepts back navigation when the drawer is open
                    val scope = rememberCoroutineScope()
                    val focusManager = LocalFocusManager.current

                    BackHandler {
                        if (drawerState.isOpen) {
                            scope.launch {
                                drawerState.close()
                            }
                        } else {
                            focusManager.clearFocus()
                        }
                    }
                    val darkTheme = remember(key1 = "darkTheme") {
                        mutableStateOf(true)
                    }
                    ChatGPTLiteTheme(darkTheme.value) {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            AppScaffold(
                                drawerState = drawerState,
                                onSettingsClicked = {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(NavRoute.ROVER_SETTINGS)
                                    }
                                },
                                onModelSettingsClicked = {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(NavRoute.MlcSettings)
                                    }
                                },
                                onChatClicked = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                onNewChatClicked = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                onIconClicked = {
                                    darkTheme.value = !darkTheme.value
                                }
                            ) {
                                NavHost(navController = navController, startDestination = NavRoute.HOME) {
                                    composable(NavRoute.HOME) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            AppBar(onClickMenu = {
                                                scope.launch { drawerState.open() }
                                            })
                                            Divider()
                                            Conversation()
                                        }
                                    }
                                    composable(NavRoute.ROVER_SETTINGS) {
                                        SettingsScreen(
                                            onBackPressed={
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                    composable(NavRoute.MlcSettings) {
                                        MlCModelSettings(
                                            navController = navController,
                                            modelViewController = modelViewController
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatGPTLiteTheme {
    }
}