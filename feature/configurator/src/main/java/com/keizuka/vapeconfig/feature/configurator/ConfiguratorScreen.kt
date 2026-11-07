package com.keizuka.vapeconfig.feature.configurator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ConfiguratorScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.configurator_rebuildable), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.configurator_intro), style = MaterialTheme.typography.bodyLarge)
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.configurator_safety_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.configurator_safety_body), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
