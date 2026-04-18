package com.example.freshtrack

import com.example.freshtrack.presentation.inventory.InventoryViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreshTrackTheme {
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
                            onEditClick = { id -> navController.navigate("item_edit/$id") }
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
                            onMarkConsumed = {},     // not reachable in create mode (button hidden)
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
                            onMarkConsumed = {
                                itemEditViewModel.markConsumed { navController.popBackStack() }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
