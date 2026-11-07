package com.keizuka.vapeconfig.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.R

enum class Section(val route: String, val titleRes: Int, val summaryRes: Int) {
    Catalog("catalog", R.string.catalog_title, R.string.catalog_summary),
    Configurator("configurator", R.string.configurator_title, R.string.configurator_summary),
    Liquids("liquids", R.string.liquids_title, R.string.liquids_summary),
    Settings("settings", R.string.settings_title, R.string.settings_summary),
}

@Composable
fun DashboardScreen(onOpenSection: (Section) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.dashboard_welcome),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 155.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Section.entries) { section ->
                Card(
                    onClick = { onOpenSection(section) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(section.titleRes), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(section.summaryRes),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

