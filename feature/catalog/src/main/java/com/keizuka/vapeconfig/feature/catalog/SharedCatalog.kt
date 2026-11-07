package com.keizuka.vapeconfig.feature.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class SharedCatalogItem(val id: String, val name: String, val category: String, val description: String, val sourceUrl: String)

class SharedCatalogRepository(
    private val url: String = "https://raw.githubusercontent.com/keizuka/VapeConfig/main/catalog/catalog.json",
) {
    suspend fun load(): List<SharedCatalogItem> = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "VapeConfig-Android")
            setRequestProperty("Accept", "application/json")
        }
        try {
            check(connection.responseCode in 200..299) { "Catalog HTTP ${connection.responseCode}" }
            val data = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            (0 until data.length()).map { index ->
                val item = data.getJSONObject(index)
                SharedCatalogItem(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    category = item.getString("category"),
                    description = item.getString("description"),
                    sourceUrl = item.optString("source_url"),
                )
            }
        } finally {
            connection.disconnect()
        }
    }
}
