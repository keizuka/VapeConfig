package com.keizuka.vapeconfig.feature.liquids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LiquidsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.liquids_recipes), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.liquids_intro), style = MaterialTheme.typography.bodyLarge)
        AssistChip(onClick = {}, label = { Text(stringResource(R.string.liquids_ingredients)) })
    }
}
