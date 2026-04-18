package com.example.freshtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freshtrack.presentation.inventory.InventoryViewModel
import com.example.freshtrack.presentation.inventory.InventoryViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels {
        val app = application as FreshTrackApplication
        InventoryViewModelFactory(app.container.itemRepository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            Scaffold(
                topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
            ) { padding ->
                if (uiState.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.empty_items),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            Card {
                                Column(Modifier.padding(12.dp)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Text(item.quantityText)
                                    Text(stringResource(R.string.status_label, item.status.name))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}