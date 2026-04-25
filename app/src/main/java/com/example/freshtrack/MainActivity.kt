package com.example.freshtrack

import com.example.freshtrack.presentation.inventory.InventoryViewModel
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.freshtrack.presentation.inventory.InventoryScreen
import com.example.freshtrack.presentation.inventory.ItemEditScreen
import com.example.freshtrack.presentation.inventory.ItemEditViewModel
import com.example.freshtrack.presentation.inventory.InventoryViewModelFactory
import com.example.freshtrack.presentation.settings.AppTheme
import com.example.freshtrack.presentation.settings.SettingsScreen
import com.example.freshtrack.presentation.settings.SettingsViewModel
import com.example.freshtrack.ui.theme.FreshTrackTheme

class MainActivity : ComponentActivity() {

    private val inventoryViewModel: InventoryViewModel by viewModels {
        val app = application as FreshTrackApplication
        InventoryViewModelFactory(app.container.itemRepository)
    }

    private val itemEditViewModel: ItemEditViewModel by viewModels {
        val app = application as FreshTrackApplication
        ItemEditViewModel.Factory(app.container.itemRepository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as FreshTrackApplication
        SettingsViewModel.Factory(app.container.settingsDataStore)
    }

    // Launcher na vyžiadanie POST_NOTIFICATIONS permission
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* výsledok ignorujeme — ak odmietne, notifikácie jednoducho nechodia */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vyžiadaj permission pri prvom spustení (len Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val useDarkTheme = when (settingsState.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> null
            }

            FreshTrackTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "inventory"
                ) {
                    composable("inventory") {
                        val uiState by inventoryViewModel.uiState.collectAsStateWithLifecycle()
                        InventoryScreen(
                            uiState = uiState,
                            onAddClick = { navController.navigate("item_edit") },
                            onEditClick = { id -> navController.navigate("item_edit/$id") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }

                    // Add new item
                    composable("item_edit") {
                        val uiState by itemEditViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            itemEditViewModel.resetForCreate()
                        }

                        ItemEditScreen(
                            uiState = uiState,
                            onNameChange = itemEditViewModel::onNameChange,
                            onQuantityChange = itemEditViewModel::onQuantityChange,
                            onUnitChange = itemEditViewModel::onUnitChange,
                            onNoteChange = itemEditViewModel::onNoteChange,
                            onCategoryChange = itemEditViewModel::onCategoryChange,
                            onExpirationDateChange = itemEditViewModel::onExpirationDateChange,
                            onSave = {
                                itemEditViewModel.save { navController.popBackStack() }
                            },
                            onDelete = {},           // not reachable in create mode (button hidden)
                            onConsumedChange = itemEditViewModel::onConsumedChange,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Edit existing item
                    composable(
                        route = "item_edit/{itemId}",
                        arguments = listOf(navArgument("itemId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
                        val uiState by itemEditViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(itemId) {
                            if (itemId != -1) itemEditViewModel.loadItem(itemId)
                        }

                        ItemEditScreen(
                            uiState = uiState,
                            onNameChange = itemEditViewModel::onNameChange,
                            onQuantityChange = itemEditViewModel::onQuantityChange,
                            onUnitChange = itemEditViewModel::onUnitChange,
                            onNoteChange = itemEditViewModel::onNoteChange,
                            onCategoryChange = itemEditViewModel::onCategoryChange,
                            onExpirationDateChange = itemEditViewModel::onExpirationDateChange,
                            onSave = {
                                itemEditViewModel.save { navController.popBackStack() }
                            },
                            onDelete = {
                                itemEditViewModel.delete { navController.popBackStack() }
                            },
                            onConsumedChange = itemEditViewModel::onConsumedChange,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Settings
                    composable("settings") {
                        var daysInput by remember {
                            mutableStateOf(settingsState.daysBeforeExpiry.toString())
                        }
                        var daysError by remember { mutableStateOf<String?>(null) }

                        SettingsScreen(
                            uiState = settingsState,
                            onInventoryClick = { navController.navigate("inventory") { popUpTo("inventory") { inclusive = true } } },
                            onNotificationsToggle = settingsViewModel::setNotificationsEnabled,
                            onDaysInputChange = { input ->
                                val result = settingsViewModel.onDaysInputChange(input)
                                daysInput = result.daysInput
                                daysError = result.daysInputError.toString()
                            },
                            onDaysSave = { days ->
                                settingsViewModel.saveDaysBeforeExpiry(days)
                            },
                            onThemeChange = settingsViewModel::setTheme,
                            onNotificationTimeSave = { hour, minute ->
                                settingsViewModel.saveNotificationTime(hour, minute)
                            },
                            daysInputState = daysInput,
                            daysInputError = daysError?.toInt()
                        )
                    }
                }
            }
        }
    }
}