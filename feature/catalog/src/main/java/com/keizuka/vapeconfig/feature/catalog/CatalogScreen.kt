package com.keizuka.vapeconfig.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.database.CatalogItemDao
import com.keizuka.vapeconfig.core.database.CatalogItemEntity
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CatalogScreen(dao: CatalogItemDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(emptyList<CatalogItemEntity>()) }
    suspend fun refresh() { entries = dao.getAll() }
    LaunchedEffect(dao) { refresh() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.catalog_local), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.catalog_intro), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.catalog_add_title), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.catalog_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(category, { category = it }, label = { Text(stringResource(R.string.catalog_category)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.catalog_notes)) }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            val itemName = name.trim()
                            if (itemName.isNotEmpty()) scope.launch {
                                dao.save(CatalogItemEntity(UUID.randomUUID().toString(), itemName, category.trim().ifEmpty { "Autre" }, notes = notes.trim()))
                                name = ""; category = ""; notes = ""; refresh()
                            }
                        }, enabled = name.isNotBlank()) { Text(stringResource(R.string.catalog_save)) }
                    }
                }
            }
            if (entries.isEmpty()) item { Text(stringResource(R.string.catalog_empty), style = MaterialTheme.typography.bodyMedium) }
            items(entries, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(item.category, style = MaterialTheme.typography.labelMedium)
                        if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        TextButton(onClick = { scope.launch { dao.delete(item.id); refresh() } }) { Text(stringResource(R.string.catalog_delete)) }
                    }
                }
            }
        }
    }
}
