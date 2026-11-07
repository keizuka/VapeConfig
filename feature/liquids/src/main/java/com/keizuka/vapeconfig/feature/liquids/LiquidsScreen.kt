package com.keizuka.vapeconfig.feature.liquids

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
import com.keizuka.vapeconfig.core.calculator.BaseCarrier
import com.keizuka.vapeconfig.core.calculator.LiquidMixParameters
import com.keizuka.vapeconfig.core.calculator.LiquidMixResult
import com.keizuka.vapeconfig.core.calculator.calculateLiquidMix
import com.keizuka.vapeconfig.core.database.LiquidRecipeDao
import com.keizuka.vapeconfig.core.database.LiquidRecipeEntity
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LiquidsScreen(dao: LiquidRecipeDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("100") }
    var targetNicotine by remember { mutableStateOf("3") }
    var baseStrength by remember { mutableStateOf("20") }
    var flavorPercent by remember { mutableStateOf("10") }
    var vgPercent by remember { mutableStateOf("70") }
    var nicotineCarrier by remember { mutableStateOf(BaseCarrier.PG) }
    var flavorCarrier by remember { mutableStateOf(BaseCarrier.PG) }
    var result by remember { mutableStateOf<LiquidMixResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var recipes by remember { mutableStateOf(emptyList<LiquidRecipeEntity>()) }
    suspend fun refresh() { recipes = dao.getAll() }
    LaunchedEffect(dao) { refresh() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.liquids_recipes), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.liquids_intro), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.liquids_mixer_title), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.liquids_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        LiquidNumber(volume, { volume = it; result = null }, R.string.liquids_volume)
                        LiquidNumber(targetNicotine, { targetNicotine = it; result = null }, R.string.liquids_target_nicotine)
                        LiquidNumber(baseStrength, { baseStrength = it; result = null }, R.string.liquids_base_strength)
                        LiquidNumber(flavorPercent, { flavorPercent = it; result = null }, R.string.liquids_flavor_percent)
                        LiquidNumber(vgPercent, { vgPercent = it; result = null }, R.string.liquids_vg_percent)
                        Text(stringResource(R.string.liquids_nicotine_carrier))
                        CarrierSelector(nicotineCarrier) { nicotineCarrier = it; result = null }
                        Text(stringResource(R.string.liquids_flavor_carrier))
                        CarrierSelector(flavorCarrier) { flavorCarrier = it; result = null }
                        Button(onClick = {
                            try {
                                result = calculateLiquidMix(
                                    LiquidMixParameters(
                                        targetVolumeMl = volume.number(),
                                        targetNicotineMgMl = targetNicotine.number(),
                                        nicotineBaseMgMl = baseStrength.number(),
                                        flavorPercent = flavorPercent.number(),
                                        targetVgPercent = vgPercent.number(),
                                        nicotineCarrier = nicotineCarrier,
                                        flavorCarrier = flavorCarrier,
                                    ),
                                )
                                error = null
                            } catch (exception: Exception) {
                                result = null
                                error = exception.message ?: "Invalid values."
                            }
                        }) { Text(stringResource(R.string.liquids_calculate)) }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        result?.let { mix ->
                            Text(stringResource(R.string.liquids_result_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.liquids_result_nicotine, mix.nicotineBaseMl.format()))
                            Text(stringResource(R.string.liquids_result_flavor, mix.flavorMl.format()))
                            Text(stringResource(R.string.liquids_result_pg, mix.pgMl.format()))
                            Text(stringResource(R.string.liquids_result_vg, mix.vgMl.format()))
                            Text(stringResource(R.string.liquids_result_total, mix.totalMl.format()))
                            Text(stringResource(R.string.liquids_mixer_warning), style = MaterialTheme.typography.bodySmall)
                            val savedDetails = listOf(
                                stringResource(R.string.liquids_result_nicotine, mix.nicotineBaseMl.format()),
                                stringResource(R.string.liquids_result_flavor, mix.flavorMl.format()),
                                stringResource(R.string.liquids_result_pg, mix.pgMl.format()),
                                stringResource(R.string.liquids_result_vg, mix.vgMl.format()),
                                stringResource(R.string.liquids_result_total, mix.totalMl.format()),
                            ).joinToString("\n")
                            Button(onClick = {
                                val recipeName = name.trim()
                                if (recipeName.isNotEmpty()) scope.launch {
                                    dao.insert(LiquidRecipeEntity(name = recipeName, updatedAtEpochMillis = System.currentTimeMillis(), recipeJson = savedDetails))
                                    name = ""; result = null; refresh()
                                }
                            }, enabled = name.isNotBlank()) { Text(stringResource(R.string.liquids_save)) }
                        }
                    }
                }
            }
            if (recipes.isEmpty()) item { Text(stringResource(R.string.liquids_empty)) }
            items(recipes, key = { it.id }) { recipe ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                        if (recipe.recipeJson.isNotBlank()) Text(recipe.recipeJson, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        TextButton(onClick = { scope.launch { dao.delete(recipe.id); refresh() } }) { Text(stringResource(R.string.liquids_delete)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidNumber(value: String, onValueChange: (String) -> Unit, label: Int) {
    OutlinedTextField(value, onValueChange, label = { Text(stringResource(label)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun CarrierSelector(selected: BaseCarrier, onSelect: (BaseCarrier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BaseCarrier.entries.forEach { carrier ->
            FilterChip(
                selected = carrier == selected,
                onClick = { onSelect(carrier) },
                label = { Text(if (carrier == BaseCarrier.PG) stringResource(R.string.liquids_carrier_pg) else stringResource(R.string.liquids_carrier_vg)) },
            )
        }
    }
}

private fun String.number(): Double = replace(',', '.').toDouble()
private fun Double.format(): String = String.format(Locale.getDefault(), "%.2f ml", this)
