package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.SubscriptionPlan
import com.example.ui.components.AngoflyBottomBar
import com.example.ui.components.AngoflyTab
import com.example.ui.components.AngoflyTopBar
import com.example.ui.player.ExoPlayerScreen
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MenuDrawerScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.ANGOFLYTheme
import com.example.ui.theme.AngoBlack
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.IptvViewModel

enum class AppScreen {
    SPLASH,
    LOGIN,
    MAIN,
    PLAYLISTS,
    FAVORITES,
    HISTORY,
    SUBSCRIPTIONS,
    PAYMENTS,
    ACCOUNT,
    ADMIN,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val iptvViewModel: IptvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ANGOFLYTheme {
                AngoflyApp(
                    authViewModel = authViewModel,
                    iptvViewModel = iptvViewModel
                )
            }
        }
    }
}

@Composable
fun AngoflyApp(
    authViewModel: AuthViewModel,
    iptvViewModel: IptvViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSyncing by iptvViewModel.isSyncing.collectAsState()
    val currentPlayingChannel by iptvViewModel.currentPlayingChannel.collectAsState()
    val allChannels by iptvViewModel.allChannels.collectAsState()
    val supportWhatsAppNumber by authViewModel.supportWhatsAppNumber.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentTab by remember { mutableStateOf(AngoflyTab.HOME) }
    var selectedPlanForPayment by remember { mutableStateOf<SubscriptionPlan?>(null) }

    // Full screen video player
    if (currentPlayingChannel != null) {
        ExoPlayerScreen(
            channel = currentPlayingChannel!!,
            allChannels = allChannels,
            onClose = { iptvViewModel.stopPlayback() },
            onSelectChannel = { iptvViewModel.playChannel(it) },
            onToggleFavorite = { iptvViewModel.toggleFavorite(it) }
        )
        return
    }

    Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
        when (screen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onFinished = {
                        if (currentUser != null) {
                            currentScreen = AppScreen.MAIN
                        } else {
                            currentScreen = AppScreen.LOGIN
                        }
                    }
                )
            }

            AppScreen.LOGIN -> {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        currentScreen = AppScreen.MAIN
                    }
                )
            }

            AppScreen.MAIN -> {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AngoBlack),
                    containerColor = AngoBlack,
                    topBar = {
                        AngoflyTopBar(
                            currentUser = currentUser,
                            isSyncing = isSyncing,
                            onSyncClick = { iptvViewModel.syncDefaultPlaylist(force = true) },
                            onSearchClick = {
                                currentTab = AngoflyTab.LIVE_TV
                            },
                            onAccountClick = { currentScreen = AppScreen.ACCOUNT },
                            onAdminClick = { currentScreen = AppScreen.ADMIN }
                        )
                    },
                    bottomBar = {
                        AngoflyBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AngoflyTab.HOME -> {
                                HomeScreen(
                                    iptvViewModel = iptvViewModel,
                                    currentUser = currentUser,
                                    onPlayChannel = { iptvViewModel.playChannel(it) },
                                    onNavigateToLiveTv = { currentTab = AngoflyTab.LIVE_TV },
                                    onNavigateToMovies = { currentTab = AngoflyTab.MOVIES },
                                    onNavigateToSeries = { currentTab = AngoflyTab.SERIES },
                                    onNavigateToSubscriptions = { currentScreen = AppScreen.SUBSCRIPTIONS }
                                )
                            }

                            AngoflyTab.LIVE_TV -> {
                                LiveTvScreen(
                                    iptvViewModel = iptvViewModel,
                                    onPlayChannel = { iptvViewModel.playChannel(it) }
                                )
                            }

                            AngoflyTab.MOVIES -> {
                                MoviesScreen(
                                    iptvViewModel = iptvViewModel,
                                    onPlayMovie = { iptvViewModel.playChannel(it) }
                                )
                            }

                            AngoflyTab.SERIES -> {
                                SeriesScreen(
                                    iptvViewModel = iptvViewModel,
                                    onPlaySeries = { iptvViewModel.playChannel(it) }
                                )
                            }

                            AngoflyTab.MENU -> {
                                MenuDrawerScreen(
                                    currentUser = currentUser,
                                    supportWhatsAppNumber = supportWhatsAppNumber,
                                    onNavigateTab = { currentTab = it },
                                    onNavigateToPlaylists = { currentScreen = AppScreen.PLAYLISTS },
                                    onNavigateToFavorites = { currentScreen = AppScreen.FAVORITES },
                                    onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                                    onNavigateToSubscriptions = { currentScreen = AppScreen.SUBSCRIPTIONS },
                                    onNavigateToPayments = {
                                        selectedPlanForPayment = null
                                        currentScreen = AppScreen.PAYMENTS
                                    },
                                    onNavigateToAccount = { currentScreen = AppScreen.ACCOUNT },
                                    onNavigateToAdmin = { currentScreen = AppScreen.ADMIN },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                    onLogout = {
                                        authViewModel.logout()
                                        currentScreen = AppScreen.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
            }

            AppScreen.PLAYLISTS -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AngoBlack,
                    topBar = {
                        AngoflyTopBar(
                            currentUser = currentUser,
                            isSyncing = isSyncing,
                            onSyncClick = { iptvViewModel.syncDefaultPlaylist(force = true) },
                            onSearchClick = {
                                currentScreen = AppScreen.MAIN
                                currentTab = AngoflyTab.LIVE_TV
                            },
                            onAccountClick = { currentScreen = AppScreen.ACCOUNT },
                            onAdminClick = { currentScreen = AppScreen.ADMIN }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PlaylistsScreen(iptvViewModel = iptvViewModel)
                    }
                }
            }

            AppScreen.FAVORITES -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                FavoritesScreen(
                    iptvViewModel = iptvViewModel,
                    onBack = { currentScreen = AppScreen.MAIN },
                    onPlayChannel = { iptvViewModel.playChannel(it) }
                )
            }

            AppScreen.HISTORY -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                HistoryScreen(
                    iptvViewModel = iptvViewModel,
                    onBack = { currentScreen = AppScreen.MAIN },
                    onPlayChannel = { iptvViewModel.playChannel(it) }
                )
            }

            AppScreen.SUBSCRIPTIONS -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AngoBlack,
                    topBar = {
                        AngoflyTopBar(
                            currentUser = currentUser,
                            isSyncing = isSyncing,
                            onSyncClick = { iptvViewModel.syncDefaultPlaylist(force = true) },
                            onSearchClick = {
                                currentScreen = AppScreen.MAIN
                                currentTab = AngoflyTab.LIVE_TV
                            },
                            onAccountClick = { currentScreen = AppScreen.ACCOUNT },
                            onAdminClick = { currentScreen = AppScreen.ADMIN }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SubscriptionsScreen(
                            onSelectPlanForPayment = { plan ->
                                selectedPlanForPayment = plan
                                currentScreen = AppScreen.PAYMENTS
                            }
                        )
                    }
                }
            }

            AppScreen.PAYMENTS -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AngoBlack,
                    topBar = {
                        AngoflyTopBar(
                            currentUser = currentUser,
                            isSyncing = isSyncing,
                            onSyncClick = { iptvViewModel.syncDefaultPlaylist(force = true) },
                            onSearchClick = {
                                currentScreen = AppScreen.MAIN
                                currentTab = AngoflyTab.LIVE_TV
                            },
                            onAccountClick = { currentScreen = AppScreen.ACCOUNT },
                            onAdminClick = { currentScreen = AppScreen.ADMIN }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PaymentsScreen(
                            authViewModel = authViewModel,
                            currentUser = currentUser,
                            initialPlan = selectedPlanForPayment
                        )
                    }
                }
            }

            AppScreen.ACCOUNT -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                AccountScreen(
                    currentUser = currentUser,
                    onBack = { currentScreen = AppScreen.MAIN },
                    onNavigateToSubscriptions = { currentScreen = AppScreen.SUBSCRIPTIONS },
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = AppScreen.LOGIN
                    }
                )
            }

            AppScreen.ADMIN -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                AdminPanelScreen(
                    authViewModel = authViewModel,
                    iptvViewModel = iptvViewModel,
                    onClose = { currentScreen = AppScreen.MAIN }
                )
            }

            AppScreen.SETTINGS -> {
                BackHandler { currentScreen = AppScreen.MAIN }
                SettingsScreen(
                    iptvViewModel = iptvViewModel,
                    onBack = { currentScreen = AppScreen.MAIN }
                )
            }
        }
    }
}
