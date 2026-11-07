package com.keizuka.vapeconfig.feature.batteries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.database.BatteryProfileDao
import com.keizuka.vapeconfig.core.database.BatteryProfileEntity
import kotlinx.coroutines.launch

private const val INTERNAL = "INTERNAL"
private const val REMOVABLE = "REMOVABLE"

@Composable
fun BatteriesScreen(dao: BatteryProfileDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var kind by remember { mutableStateOf(INTERNAL) }
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var currentRating by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var profiles by remember { mutableStateOf(emptyList<BatteryProfileEntity>()) }
    suspend fun refresh() { profiles = dao.getAll() }
    LaunchedEffect(dao) { refresh() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.batteries_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.batteries_intro), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.batteries_add_title), style = MaterialTheme.typography.titleMedium)
                        FilterChip(kind == INTERNAL, { kind = INTERNAL }, label = { Text(stringResource(R.string.batteries_internal)) })
                        FilterChip(kind == REMOVABLE, { kind = REMOVABLE }, label = { Text(stringResource(R.string.batteries_removable)) })
                        Field(name, { name = it }, R.string.batteries_name)
                        Field(model, { model = it }, R.string.batteries_model)
                        Field(format, { format = it }, R.string.batteries_format)
                        Field(capacity, { capacity = it }, R.string.batteries_capacity, KeyboardType.Number)
                        Field(currentRating, { currentRating = it }, R.string.batteries_current_rating)
                        Field(source, { source = it }, R.string.batteries_source)
                        Field(notes, { notes = it }, R.string.batteries_notes)
                        Button(onClick = {
                            val profileName = name.trim()
                            if (profileName.isNotEmpty()) scope.launch {
                                dao.insert(
                                    BatteryProfileEntity(
                                        name = profileName,
                                        kind = kind,
                                        model = model.trim(),
                                        format = format.trim(),
                                        capacityMah = capacity.trim(),
                                        currentRatingA = currentRating.trim(),
                                        manufacturerSource = source.trim(),
                                        notes = notes.trim(),
                                    ),
                                )
                                name = ""; model = ""; format = ""; capacity = ""; currentRating = ""; source = ""; notes = ""
                                refresh()
                            }
                        }, enabled = name.isNotBlank()) { Text(stringResource(R.string.batteries_save)) }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(stringResource(R.string.batteries_safety), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (profiles.isEmpty()) item { Text(stringResource(R.string.batteries_empty)) }
            items(profiles, key = { it.id }) { profile ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(profile.name, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (profile.kind == INTERNAL) R.string.batteries_internal else R.string.batteries_removable))
                        if (profile.model.isNotBlank()) Text(stringResource(R.string.batteries_model_value, profile.model))
                        if (profile.format.isNotBlank()) Text(stringResource(R.string.batteries_format_value, profile.format))
                        if (profile.capacityMah.isNotBlank()) Text(stringResource(R.string.batteries_capacity_value, profile.capacityMah))
                        if (profile.currentRatingA.isNotBlank()) Text(stringResource(R.string.batteries_current_value, profile.currentRatingA))
                        if (profile.manufacturerSource.isNotBlank()) Text(profile.manufacturerSource, style = MaterialTheme.typography.bodySmall)
                        if (profile.notes.isNotBlank()) Text(profile.notes)
                        TextButton(onClick = { scope.launch { dao.delete(profile.id); refresh() } }) { Text(stringResource(R.string.batteries_delete)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, label: Int, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        singleLine = label != R.string.batteries_notes,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}
