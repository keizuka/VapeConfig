package com.keizuka.vapeconfig.feature.configurator

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.calculator.CoilEstimate
import com.keizuka.vapeconfig.core.calculator.CoilParameters
import com.keizuka.vapeconfig.core.calculator.WireMaterial
import com.keizuka.vapeconfig.core.calculator.estimateCoilResistance
import com.keizuka.vapeconfig.core.database.CoilConfigurationDao
import com.keizuka.vapeconfig.core.database.CoilConfigurationEntity
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ConfiguratorScreen(dao: CoilConfigurationDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var rebuildable by remember { mutableStateOf(true) }
    var material by remember { mutableStateOf(WireMaterial.KANTHAL_A1) }
    var wireDiameter by remember { mutableStateOf("0.32") }
    var innerDiameter by remember { mutableStateOf("3.0") }
    var wraps by remember { mutableStateOf("6") }
    var legLength by remember { mutableStateOf("5") }
    var coilCount by remember { mutableStateOf("1") }
    var estimate by remember { mutableStateOf<CoilEstimate?>(null) }
    var calculationError by remember { mutableStateOf<String?>(null) }
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
                        Text(stringResource(R.string.configurator_calculator_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.configurator_material))
                        WireMaterial.entries.forEach { option ->
                            FilterChip(
                                selected = material == option,
                                onClick = { material = option; estimate = null },
                                label = { Text(stringResource(option.labelResource())) },
                            )
                        }
                        NumericInput(wireDiameter, { wireDiameter = it; estimate = null }, R.string.configurator_wire_diameter)
                        NumericInput(innerDiameter, { innerDiameter = it; estimate = null }, R.string.configurator_inner_diameter)
                        NumericInput(wraps, { wraps = it; estimate = null }, R.string.configurator_wraps, KeyboardType.Number)
                        NumericInput(legLength, { legLength = it; estimate = null }, R.string.configurator_legs)
                        NumericInput(coilCount, { coilCount = it; estimate = null }, R.string.configurator_coil_count, KeyboardType.Number)
                        Button(onClick = {
                            try {
                                estimate = estimateCoilResistance(
                                    CoilParameters(
                                        material = material,
                                        wireDiameterMm = wireDiameter.number(),
                                        innerDiameterMm = innerDiameter.number(),
                                        wraps = wraps.toInt(),
                                        legLengthMm = legLength.number(),
                                        coilCount = coilCount.toInt(),
                                    ),
                                )
                                calculationError = null
                            } catch (exception: Exception) {
                                estimate = null
                                calculationError = exception.message ?: "Invalid values."
                            }
                        }) { Text(stringResource(R.string.configurator_calculate)) }
                        calculationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        estimate?.let { Text(stringResource(R.string.configurator_estimate_result, it.resistanceOhms.format()), style = MaterialTheme.typography.titleMedium) }
                        estimate?.let { Text(stringResource(R.string.configurator_estimate_warning), style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.configurator_safety_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.configurator_safety_body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.configurator_add_title), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.configurator_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(details, { details = it }, label = { Text(stringResource(R.string.configurator_notes)) }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.configurator_rebuildable))
                        Switch(checked = rebuildable, onCheckedChange = { rebuildable = it })
                        val estimateNote = estimate?.let { stringResource(R.string.configurator_saved_estimate, it.resistanceOhms.format()) }.orEmpty()
                        Button(onClick = {
                            val setupName = name.trim()
                            if (setupName.isNotEmpty()) scope.launch {
                                dao.insert(CoilConfigurationEntity(name = setupName, isRebuildable = rebuildable, notes = listOf(details.trim(), estimateNote).filter(String::isNotBlank).joinToString("\n")))
                                name = ""; details = ""; estimate = null; refresh()
                            }
                        }, enabled = name.isNotBlank()) { Text(stringResource(R.string.configurator_save)) }
                    }
                }
            }
            if (configurations.isEmpty()) item { Text(stringResource(R.string.configurator_empty)) }
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

@Composable
private fun NumericInput(value: String, onValueChange: (String) -> Unit, label: Int, keyboardType: KeyboardType = KeyboardType.Decimal) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun String.number(): Double = replace(',', '.').toDouble()
private fun Double.format(): String = String.format(Locale.getDefault(), "%.3f", this)
private fun WireMaterial.labelResource(): Int = when (this) {
    WireMaterial.KANTHAL_A1 -> R.string.material_kanthal
    WireMaterial.NICHROME_80 -> R.string.material_nichrome
    WireMaterial.STAINLESS_316L -> R.string.material_stainless
}
