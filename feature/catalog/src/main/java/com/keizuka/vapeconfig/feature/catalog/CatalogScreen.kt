package com.keizuka.vapeconfig.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.compatibility.CompatibilityStatus
import com.keizuka.vapeconfig.core.compatibility.checkConnectorCompatibility
import com.keizuka.vapeconfig.core.database.CatalogItemDao
import com.keizuka.vapeconfig.core.database.CatalogItemEntity
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

@Composable
fun CatalogScreen(dao: CatalogItemDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val repository = remember { SharedCatalogRepository() }
    var selectedTab by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var connection by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(emptyList<CatalogItemEntity>()) }
    var shared by remember { mutableStateOf(SharedCatalogItem.builtInCatalog) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var sharedError by remember { mutableStateOf(false) }
    var selectedShared by remember { mutableStateOf<SharedCatalogItem?>(null) }
    var selectedLocal by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var deviceConnector by remember { mutableStateOf("") }
    var atomizerConnector by remember { mutableStateOf("") }
    var compatibility by remember { mutableStateOf<CompatibilityStatus?>(null) }

    suspend fun refreshLocal() { entries = dao.getAll() }
    fun refreshShared() {
        scope.launch {
            loading = true
            sharedError = false
            try { shared = repository.load() } catch (_: Exception) { sharedError = true }
            loading = false
        }
    }
    LaunchedEffect(dao) { refreshLocal() }
    LaunchedEffect(Unit) { refreshShared() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.catalog_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.catalog_intro), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selectedTab == 0, { selectedTab = 0 }, text = { Text(stringResource(R.string.catalog_shared_tab)) })
            Tab(selectedTab == 1, { selectedTab = 1 }, text = { Text(stringResource(R.string.catalog_local_tab)) })
        }
        if (selectedTab == 0) {
            TextButton(onClick = { refreshShared() }, enabled = !loading) {
                Text(stringResource(if (loading) R.string.catalog_loading else R.string.catalog_refresh))
            }
            OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.catalog_search)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            if (sharedError) {
                Text(stringResource(R.string.catalog_offline_fallback), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                val normalizedQuery = normalizeForSearch(query)
                val filtered = shared.filter { item ->
                    normalizeForSearch(item.name + " " + item.category + " " + item.description + " " + item.searchTerms + " " + item.id + " " + item.sourceUrl)
                        .contains(normalizedQuery)
                }
                if (filtered.isEmpty() && !loading) item { Text(stringResource(R.string.catalog_shared_empty), modifier = Modifier.padding(12.dp)) }
                items(filtered, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { selectedShared = item }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { query = item.category }) { Text(item.category) }
                            Text(item.description, style = MaterialTheme.typography.bodyMedium)
                            if (item.sourceUrl.isNotBlank()) TextButton(onClick = { uriHandler.openUri(item.sourceUrl) }) { Text(stringResource(R.string.catalog_source)) }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.catalog_add_title), style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.catalog_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(category, { category = it }, label = { Text(stringResource(R.string.catalog_category)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(connection, { connection = it }, label = { Text(stringResource(R.string.catalog_connection)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.catalog_notes)) }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = {
                                val itemName = name.trim()
                                if (itemName.isNotEmpty()) scope.launch {
                                    dao.save(CatalogItemEntity(UUID.randomUUID().toString(), itemName, category.trim().ifEmpty { "Autre" }, notes = notes.trim(), connection = connection.trim()))
                                    name = ""; category = ""; connection = ""; notes = ""; refreshLocal()
                                }
                            }, enabled = name.isNotBlank()) { Text(stringResource(R.string.catalog_save)) }
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.compatibility_title), style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(deviceConnector, { deviceConnector = it; compatibility = null }, label = { Text(stringResource(R.string.compatibility_device_connector)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(atomizerConnector, { atomizerConnector = it; compatibility = null }, label = { Text(stringResource(R.string.compatibility_atomizer_connector)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(onClick = { compatibility = checkConnectorCompatibility(deviceConnector, atomizerConnector).status }) {
                                Text(stringResource(R.string.compatibility_check))
                            }
                            compatibility?.let { status ->
                                val label = when (status) {
                                    CompatibilityStatus.COMPATIBLE -> R.string.compatibility_matching
                                    CompatibilityStatus.INCOMPATIBLE -> R.string.compatibility_different
                                    CompatibilityStatus.UNKNOWN -> R.string.compatibility_unknown
                                }
                                Text(stringResource(label), style = MaterialTheme.typography.titleSmall)
                            }
                            Text(stringResource(R.string.compatibility_limit), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (entries.isEmpty()) item { Text(stringResource(R.string.catalog_empty)) }
                items(entries, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { selectedLocal = item }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { query = item.category; selectedTab = 0 }) { Text(item.category) }
                            if (item.connection.isNotBlank()) Text(stringResource(R.string.catalog_connection_value, item.connection))
                            if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                            TextButton(onClick = { scope.launch { dao.delete(item.id); refreshLocal() } }) { Text(stringResource(R.string.catalog_delete)) }
                        }
                    }
                }
            }
        }
    }

    selectedShared?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedShared = null },
            title = { Text(item.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.category, style = MaterialTheme.typography.labelLarge)
                    Text(item.description)
                    if (item.searchTerms.isNotBlank()) Text(item.searchTerms, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Row {
                    if (item.sourceUrl.isNotBlank()) TextButton(onClick = { uriHandler.openUri(item.sourceUrl) }) { Text(stringResource(R.string.catalog_source)) }
                    TextButton(onClick = { selectedShared = null }) { Text(stringResource(R.string.catalog_close)) }
                }
            },
        )
    }

    selectedLocal?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedLocal = null },
            title = { Text(item.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.category, style = MaterialTheme.typography.labelLarge)
                    if (item.connection.isNotBlank()) Text(stringResource(R.string.catalog_connection_value, item.connection))
                    if (item.notes.isNotBlank()) Text(item.notes)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLocal = null }) { Text(stringResource(R.string.catalog_close)) }
            },
        )
    }
}

private fun normalizeForSearch(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
