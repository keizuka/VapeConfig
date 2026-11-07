package com.keizuka.vapeconfig.core.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val version: String,
    val pageUrl: String,
    val apkDownloadUrl: String?,
)

/** Reads public release metadata only. The app never installs an APK automatically. */
class GitHubReleaseChecker(
    private val owner: String = "Keizuka",
    private val repository: String = "VapeConfig",
) {
    suspend fun latestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/$owner/$repository/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "VapeConfig-Android")
        }
        try {
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return@withContext null
            check(connection.responseCode in 200..299) { "GitHub API HTTP ${connection.responseCode}" }
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val version = json.optString("tag_name").removePrefix("v").ifBlank { return@withContext null }
            val apkUrl = json.optJSONArray("assets")?.let { assets ->
                (0 until assets.length()).asSequence()
                    .map { assets.getJSONObject(it) }
                    .firstOrNull { it.optString("name").endsWith(".apk", ignoreCase = true) }
                    ?.optString("browser_download_url")
            }
            ReleaseInfo(version, json.optString("html_url"), apkUrl)
        } finally {
            connection.disconnect()
        }
    }
}
