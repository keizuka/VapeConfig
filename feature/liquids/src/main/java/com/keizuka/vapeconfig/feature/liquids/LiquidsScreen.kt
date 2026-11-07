package com.keizuka.vapeconfig.feature.liquids

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
import com.keizuka.vapeconfig.core.database.LiquidRecipeDao
import com.keizuka.vapeconfig.core.database.LiquidRecipeEntity
import kotlinx.coroutines.launch

@Composable
fun LiquidsScreen(dao: LiquidRecipeDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
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
                        Text(stringResource(R.string.liquids_add_title), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.liquids_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(ingredients, { ingredients = it }, label = { Text(stringResource(R.string.liquids_ingredients)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            val recipeName = name.trim()
                            if (recipeName.isNotEmpty()) scope.launch {
                                dao.insert(LiquidRecipeEntity(name = recipeName, updatedAtEpochMillis = System.currentTimeMillis(), recipeJson = ingredients.trim()))
                                name = ""; ingredients = ""; refresh()
                            }
                        }, enabled = name.isNotBlank()) { Text(stringResource(R.string.liquids_save)) }
                    }
                }
            }
            if (recipes.isEmpty()) item { Text(stringResource(R.string.liquids_empty), style = MaterialTheme.typography.bodyMedium) }
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
