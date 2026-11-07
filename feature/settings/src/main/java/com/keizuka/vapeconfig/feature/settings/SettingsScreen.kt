package com.keizuka.vapeconfig.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.updater.GitHubReleaseChecker
import com.keizuka.vapeconfig.core.updater.ReleaseInfo
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(installedVersion: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val checker = remember { GitHubReleaseChecker() }
    var checking by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    var checked by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.settings_updates), style = MaterialTheme.typography.headlineMedium)
        Button(
            enabled = !checking,
            onClick = {
                checking = true
                checked = false
                failed = false
                scope.launch {
                    try { release = checker.latestRelease() } catch (_: Exception) { failed = true }
                    checked = true
                    checking = false
                }
            },
        ) { Text(stringResource(if (checking) R.string.settings_checking else R.string.settings_check_updates)) }
        if (failed) Text(stringResource(R.string.settings_update_error))
        if (checked && !failed && release == null) Text(stringResource(R.string.settings_no_release))
        release?.let { result ->
            val updateAvailable = compareVersions(result.version, installedVersion) > 0
            Text(
                stringResource(
                    if (updateAvailable) R.string.settings_update_available else R.string.settings_up_to_date,
                    result.version.takeIf { updateAvailable } ?: installedVersion,
                ),
            )
            TextButton(onClick = { uriHandler.openUri(result.pageUrl) }) {
                Text(stringResource(R.string.settings_open_release))
            }
        }
    }
}

private fun compareVersions(left: String, right: String): Int {
    val leftParts = left.removePrefix("v").split('.', '-', '+')
    val rightParts = right.removePrefix("v").split('.', '-', '+')
    val size = maxOf(leftParts.size, rightParts.size)
    for (index in 0 until size) {
        val a = leftParts.getOrNull(index).orEmpty()
        val b = rightParts.getOrNull(index).orEmpty()
        val comparison = if (a.toLongOrNull() != null && b.toLongOrNull() != null) {
            a.toLong().compareTo(b.toLong())
        } else {
            a.compareTo(b, ignoreCase = true)
        }
        if (comparison != 0) return comparison
    }
    return 0
}
