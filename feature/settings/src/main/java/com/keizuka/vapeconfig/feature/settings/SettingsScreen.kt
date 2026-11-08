package com.keizuka.vapeconfig.feature.settings

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keizuka.vapeconfig.core.database.VapeConfigDatabase
import com.keizuka.vapeconfig.core.updater.GitHubReleaseChecker
import com.keizuka.vapeconfig.core.updater.ReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

private const val PREFS_UPDATER = "vapeconfig_updater"
private const val PREF_DOWNLOAD_ID = "download_id"
private const val PREF_DOWNLOAD_VERSION = "download_version"
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

@Composable
fun SettingsScreen(installedVersion: String, database: VapeConfigDatabase, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val checker = remember { GitHubReleaseChecker() }
    val downloadManager = remember { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    val preferences = remember { context.getSharedPreferences(PREFS_UPDATER, Context.MODE_PRIVATE) }
    var checking by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    var checked by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var downloadId by remember {
        mutableStateOf(preferences.getLong(PREF_DOWNLOAD_ID, -1L).takeIf { it >= 0L })
    }
    var downloadedVersion by remember { mutableStateOf(preferences.getString(PREF_DOWNLOAD_VERSION, null)) }
    var downloadComplete by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var updateMessage by remember { mutableIntStateOf(0) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var pendingBackup by remember { mutableStateOf<BackupContents?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = withContext(Dispatchers.IO) { database.createBackupJson() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(json) }
                        ?: error("Impossible d’écrire le fichier.")
                }
                backupMessage = context.getString(R.string.settings_backup_saved)
            } catch (_: Exception) {
                backupMessage = context.getString(R.string.settings_backup_error)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Impossible de lire le fichier.")
                }
                pendingBackup = parseBackupJson(json)
                backupMessage = null
            } catch (_: Exception) {
                backupMessage = context.getString(R.string.settings_backup_invalid)
            }
        }
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        downloadComplete = false
        while (isActive) {
            val snapshot = withContext(Dispatchers.IO) { queryDownload(downloadManager, id) }
            downloadProgress = snapshot.progress
            when (snapshot.status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloadComplete = true
                    break
                }
                DownloadManager.STATUS_FAILED -> {
                    downloadId = null
                    downloadedVersion = null
                    preferences.edit().remove(PREF_DOWNLOAD_ID).remove(PREF_DOWNLOAD_VERSION).apply()
                    updateMessage = R.string.settings_download_failed
                    break
                }
                else -> delay(500)
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.back)) }
        Text(stringResource(R.string.settings_data_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.settings_data_description), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { exportLauncher.launch("VapeConfig-sauvegarde.json") }) {
            Text(stringResource(R.string.settings_backup_export))
        }
        Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }) {
            Text(stringResource(R.string.settings_backup_import))
        }
        backupMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(stringResource(R.string.settings_updates), style = MaterialTheme.typography.headlineMedium)
        Button(
            enabled = !checking,
            onClick = {
                checking = true
                checked = false
                failed = false
                updateMessage = 0
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
            TextButton(onClick = { openExternal(context, Intent(Intent.ACTION_VIEW, Uri.parse(result.pageUrl))) }) {
                Text(stringResource(R.string.settings_open_release))
            }
            if (updateAvailable) {
                val apkUrl = result.apkDownloadUrl
                if (apkUrl == null) {
                    Text(stringResource(R.string.settings_no_apk), color = MaterialTheme.colorScheme.error)
                } else {
                    val hasDownloadedThisVersion = downloadId != null &&
                        downloadedVersion == result.version && downloadComplete
                    when {
                        hasDownloadedThisVersion -> {
                            Text(stringResource(R.string.settings_download_complete))
                            Button(onClick = {
                                updateMessage = 0
                                scope.launch {
                                    val fileName = "VapeConfig-" + result.version + ".apk"
                                    val apkFile = File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                        fileName,
                                    )
                                    if (!apkFile.isFile) {
                                        updateMessage = R.string.settings_download_failed
                                        return@launch
                                    }
                                    val expectedDigest = result.apkSha256
                                    if (expectedDigest != null && !withContext(Dispatchers.IO) { matchesSha256(apkFile, expectedDigest) }) {
                                        updateMessage = R.string.settings_integrity_error
                                        return@launch
                                    }
                                    if (!withContext(Dispatchers.IO) { hasMatchingSigningCertificate(context, apkFile) }) {
                                        updateMessage = R.string.settings_signature_mismatch
                                        return@launch
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                        !context.packageManager.canRequestPackageInstalls()
                                    ) {
                                        updateMessage = R.string.settings_install_permission
                                        try {
                                            openExternal(
                                                context,
                                                Intent(
                                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                    Uri.parse("package:" + context.packageName),
                                                ),
                                            )
                                        } catch (_: Exception) {
                                            updateMessage = R.string.settings_install_failed
                                        }
                                        return@launch
                                    }
                                    val uri = downloadManager.getUriForDownloadedFile(downloadId!!)
                                    if (uri == null) {
                                        updateMessage = R.string.settings_download_failed
                                        return@launch
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                                            setDataAndType(uri, APK_MIME_TYPE)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        updateMessage = R.string.settings_install_failed
                                    }
                                }
                            }) { Text(stringResource(R.string.settings_install_update)) }
                        }
                        downloadId != null && downloadedVersion == result.version -> {
                            LinearProgressIndicator()
                            Text(stringResource(R.string.settings_download_progress, downloadProgress))
                        }
                        else -> {
                            Button(onClick = {
                                updateMessage = 0
                                try {
                                    val request = DownloadManager.Request(Uri.parse(apkUrl))
                                        .setTitle("VapeConfig " + result.version)
                                        .setDescription(context.getString(R.string.settings_download_description))
                                        .setMimeType(APK_MIME_TYPE)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setDestinationInExternalFilesDir(
                                            context,
                                            Environment.DIRECTORY_DOWNLOADS,
                                            "VapeConfig-" + result.version + ".apk",
                                        )
                                    val id = downloadManager.enqueue(request)
                                    preferences.edit()
                                        .putLong(PREF_DOWNLOAD_ID, id)
                                        .putString(PREF_DOWNLOAD_VERSION, result.version)
                                        .apply()
                                    downloadedVersion = result.version
                                    downloadComplete = false
                                    downloadProgress = 0
                                    downloadId = id
                                } catch (_: Exception) {
                                    updateMessage = R.string.settings_download_failed
                                }
                            }) { Text(stringResource(R.string.settings_download_apk)) }
                        }
                    }
                }
            }
            Text(stringResource(R.string.settings_install_note), style = MaterialTheme.typography.bodySmall)
        }
        if (updateMessage != 0) {
            Text(stringResource(updateMessage), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        pendingBackup?.let { backup ->
            AlertDialog(
                onDismissRequest = { pendingBackup = null },
                title = { Text(stringResource(R.string.settings_backup_confirm_title)) },
                text = { Text(stringResource(
                    R.string.settings_backup_confirm_body,
                    backup.catalog.size, backup.recipes.size, backup.coils.size, backup.batteries.size,
                )) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingBackup = null
                        scope.launch {
                            try {
                                val counts = withContext(Dispatchers.IO) { database.restoreAdditively(backup) }
                                backupMessage = context.getString(
                                    R.string.settings_backup_restored,
                                    counts.catalog, counts.recipes, counts.coils, counts.batteries,
                                )
                            } catch (_: Exception) {
                                backupMessage = context.getString(R.string.settings_backup_error)
                            }
                        }
                    }) { Text(stringResource(R.string.settings_backup_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingBackup = null }) {
                        Text(stringResource(R.string.settings_backup_cancel))
                    }
                },
            )
        }
    }
}

private data class DownloadSnapshot(val status: Int, val progress: Int)

private fun queryDownload(manager: DownloadManager, id: Long): DownloadSnapshot {
    val cursor = manager.query(DownloadManager.Query().setFilterById(id))
        ?: return DownloadSnapshot(DownloadManager.STATUS_FAILED, 0)
    cursor.use {
        if (!it.moveToFirst()) return DownloadSnapshot(DownloadManager.STATUS_FAILED, 0)
        val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val progress = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
        return DownloadSnapshot(status, progress)
    }
}

private fun openExternal(context: Context, intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun matchesSha256(file: File, expected: String): Boolean {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().buffered().use { input ->
        val buffer = ByteArray(16 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    val actual = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    return actual.equals(expected, ignoreCase = true)
}

private fun hasMatchingSigningCertificate(context: Context, apkFile: File): Boolean {
    val manager = context.packageManager
    return try {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val installed = manager.getPackageInfo(context.packageName, flags)
        val downloaded = manager.getPackageArchiveInfo(apkFile.absolutePath, flags) ?: return false
        if (downloaded.packageName != context.packageName) return false
        signingCertificates(installed) == signingCertificates(downloaded)
    } catch (_: Exception) {
        false
    }
}

@Suppress("DEPRECATION")
private fun signingCertificates(info: PackageInfo): Set<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet().orEmpty()
    } else {
        info.signatures?.map { it.toCharsString() }?.toSet().orEmpty()
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
        } else a.compareTo(b, ignoreCase = true)
        if (comparison != 0) return comparison
    }
    return 0
}
