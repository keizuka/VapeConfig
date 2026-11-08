package com.keizuka.vapeconfig.feature.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class SharedCatalogItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val sourceUrl: String,
    val searchTerms: String = "",
) {
    companion object {
        val builtInCatalog = listOf(
            SharedCatalogItem("removable-cell", "Accumulateur amovible", "Batteries", "Fiche générique : vérifiez le format et les caractéristiques dans la documentation du fabricant du modèle exact.", "", "accu batterie cellule removable battery cell 18650 20700 21700"),
            SharedCatalogItem("integrated-cell", "Batterie intégrée", "Batteries", "Fiche générique pour les appareils à batterie intégrée. Consultez le manuel de l’appareil pour ses caractéristiques.", "", "accu batterie interne incorporée integrated built-in"),
            SharedCatalogItem("mod-device", "Mod électronique", "Appareils", "Fiche de classement. Consultez le manuel du modèle exact pour ses fonctions et ses caractéristiques.", "", "box appareil cigarette electronique mod électronique device"),
            SharedCatalogItem("tank-device", "Atomiseur / clearomiseur", "Atomiseurs", "Fiche de classement. Vérifiez le raccord et les consignes du fabricant du modèle exact.", "", "tank atomizer atomiseur clearomiseur reservoir"),
            SharedCatalogItem("prefab-coil", "Résistance préfabriquée", "Résistances", "Notez la référence et les indications du fabricant de la résistance et de l’appareil.", "", "coil tête résistance commerciale prefabricated head"),
            SharedCatalogItem("rebuildable-coil", "Coil reconstructible", "Résistances", "Une estimation théorique ne remplace jamais la mesure réelle ni une vérification indépendante.", "", "reconstructible rba rda rta fil coil"),
            SharedCatalogItem("liquid-base", "Bases PG / VG", "Liquides", "Consignez les concentrations et les références exactes figurant sur l’étiquette.", "", "propylène glycol glycerine vegetale base e-liquide ejuice pg vg"),
            SharedCatalogItem("flavors", "Arômes", "Liquides", "Suivez les consignes et proportions indiquées par le fabricant de chaque arôme.", "", "arome aromes flavor flavour concentre concentrate")
        )
    }
}

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
            check(connection.responseCode in 200..299) { "Catalog HTTP " + connection.responseCode }
            val data = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            (0 until data.length()).map { index ->
                val item = data.getJSONObject(index)
                SharedCatalogItem(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    category = item.getString("category"),
                    description = item.getString("description"),
                    sourceUrl = item.optString("source_url"),
                    searchTerms = item.optString("search_terms"),
                )
            }.ifEmpty { SharedCatalogItem.builtInCatalog }
        } finally {
            connection.disconnect()
        }
    }
}
