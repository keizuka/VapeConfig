package com.keizuka.vapeconfig.feature.configurator

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
import androidx.compose.material3.Switch
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
import com.keizuka.vapeconfig.core.database.CoilConfigurationDao
import com.keizuka.vapeconfig.core.database.CoilConfigurationEntity
import kotlinx.coroutines.launch

@Composable
fun ConfiguratorScreen(dao: CoilConfigurationDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var rebuildable by remember { mutableStateOf(true) }
    var configurations by remember { mutableStateOf(emptyList<CoilConfigurationEntity>()) }
    suspend fun refresh() { configurations = dao.getAll() }
    LaunchedEffect(dao) { refresh() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.configurator_rebuildable), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.configurator_intro), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.configurator_add_title), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.configurator_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(details, { details = it }, label = { Text(stringResource(R.string.configurator_notes)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.configurator_rebuildable))
                        Switch(checked = rebuildable, onCheckedChange = { rebuildable = it })
                        Button(onClick = {
                            val setupName = name.trim()
                            if (setupName.isNotEmpty()) scope.launch {
                                dao.insert(CoilConfigurationEntity(name = setupName, isRebuildable = rebuildable, notes = details.trim()))
                                name = ""; details = ""; refresh()
                            }
                        }, enabled = name.isNotBlank()) { Text(stringResource(R.string.configurator_save)) }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.configurator_safety_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.configurator_safety_body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(stringResource(R.string.configurator_not_calculator), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
            if (configurations.isEmpty()) item { Text(stringResource(R.string.configurator_empty), style = MaterialTheme.typography.bodyMedium) }
            items(configurations, key = { it.id }) { setup ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(setup.name, style = MaterialTheme.typography.titleMedium)
                        Text(if (setup.isRebuildable) stringResource(R.string.configurator_rebuildable) else stringResource(R.string.configurator_prefab), style = MaterialTheme.typography.labelMedium)
                        if (setup.notes.isNotBlank()) Text(setup.notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        TextButton(onClick = { scope.launch { dao.delete(setup.id); refresh() } }) { Text(stringResource(R.string.configurator_delete)) }
                    }
                }
            }
        }
    }
}
