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
        // Curated manufacturer references remain available when the network cannot be reached.
        val builtInCatalog = listOf(
            SharedCatalogItem("format-18650", "Format d’accu 18650", "Accus amovibles", "Format dimensionnel 18650 (environ 18 mm de diamètre × 65.0 mm de long). Fiche d’identification uniquement, sans recommandation d’achat ou d’utilisation. Les cellules lithium-ion nues sont des composants industriels; plusieurs fabricants déconseillent explicitement leur emploi direct dans les cigarettes électroniques. Le format seul ne confirme jamais la compatibilité. Vérifiez le manuel exact du mod et les consignes officielles.", "https://www.lgensol.com/en/business/factor-cylindrical-21-18", "accu batterie amovible cell battery 18650 format"),
            SharedCatalogItem("format-20700", "Format d’accu 20700", "Accus amovibles", "Format dimensionnel 20700 (environ 20 mm de diamètre × 70.0 mm de long). Fiche d’identification uniquement, sans recommandation d’achat ou d’utilisation. Les cellules lithium-ion nues sont des composants industriels; plusieurs fabricants déconseillent explicitement leur emploi direct dans les cigarettes électroniques. Le format seul ne confirme jamais la compatibilité. Vérifiez le manuel exact du mod et les consignes officielles.", "https://www.lgensol.com/en/business/factor-cylindrical-21-18", "accu batterie amovible cell battery 20700 format"),
            SharedCatalogItem("format-21700", "Format d’accu 21700", "Accus amovibles", "Format dimensionnel 21700 (environ 21 mm de diamètre × 70.0 mm de long). Fiche d’identification uniquement, sans recommandation d’achat ou d’utilisation. Les cellules lithium-ion nues sont des composants industriels; plusieurs fabricants déconseillent explicitement leur emploi direct dans les cigarettes électroniques. Le format seul ne confirme jamais la compatibilité. Vérifiez le manuel exact du mod et les consignes officielles.", "https://www.lgensol.com/en/business/factor-cylindrical-21-18", "accu batterie amovible cell battery 21700 format"),
            SharedCatalogItem("maker-molicel", "Molicel — cellules cylindriques", "Accus amovibles", "Fiche d’identification d’un fabricant de cellules nues uniquement, sans modèle précis ni recommandation d’achat ou d’utilisation. Ce sont des composants industriels; certains fabricants déconseillent explicitement l’usage direct en cigarette électronique. Le nom de marque ou le format ne confirme jamais la compatibilité. Vérifiez la notice exacte du mod et les consignes officielles.", "https://www.molicel.com/application/", "Molicel batterie accu 18650 21700 cell"),
            SharedCatalogItem("maker-murata", "Murata — cellules cylindriques", "Accus amovibles", "Fiche d’identification d’un fabricant de cellules nues uniquement, sans modèle précis ni recommandation d’achat ou d’utilisation. Ce sont des composants industriels; certains fabricants déconseillent explicitement l’usage direct en cigarette électronique. Le nom de marque ou le format ne confirme jamais la compatibilité. Vérifiez la notice exacte du mod et les consignes officielles.", "https://www.murata.com/-/media/webrenewal/products/batteries/cylindrical/datasheet/us18650vtc5a-product-datasheet.ashx", "Murata Sony batterie accu 18650 cell"),
            SharedCatalogItem("maker-lg-energy", "LG Energy Solution — cellules cylindriques", "Accus amovibles", "Fiche d’identification d’un fabricant de cellules nues uniquement, sans modèle précis ni recommandation d’achat ou d’utilisation. Ce sont des composants industriels; certains fabricants déconseillent explicitement l’usage direct en cigarette électronique. Le nom de marque ou le format ne confirme jamais la compatibilité. Vérifiez la notice exacte du mod et les consignes officielles.", "https://www.lgensol.com/en/etc/cs-safetyguide-warning", "LG batterie accu 18650 21700 cell"),
            SharedCatalogItem("maker-samsung-sdi", "Samsung SDI — cellules cylindriques", "Accus amovibles", "Fiche d’identification d’un fabricant de cellules nues uniquement, sans modèle précis ni recommandation d’achat ou d’utilisation. Ce sont des composants industriels; certains fabricants déconseillent explicitement l’usage direct en cigarette électronique. Le nom de marque ou le format ne confirme jamais la compatibilité. Vérifiez la notice exacte du mod et les consignes officielles.", "https://news.samsungsdi.com/global/click?seq=324", "Samsung batterie accu 18650 21700 cell"),
            SharedCatalogItem("aegis-solo-3", "Geekvape Aegis Solo 3", "Box", "Box proposée en version avec accu amovible 18650 ou batterie intégrée 3000 mAh selon la variante. Vérifiez la version et son manuel avant achat.", "https://us.geekvape.com/product/AegisSolo3.html", "Geekvape mod box aegis solo 3 18650"),
            SharedCatalogItem("vaporesso-gen-max", "Vaporesso GEN MAX", "Box", "Box double accu 18650; fiche constructeur indiquant 5–220 W. La puissance affichée ne valide pas un montage ni sa sécurité.", "https://store.vaporesso.com/products/gen-max", "Vaporesso mod box gen max double dual 18650"),
            SharedCatalogItem("voopoo-drag-5", "VOOPOO DRAG 5", "Box", "Box pour deux accus 18650 amovibles; consulter le manuel du modèle et respecter les consignes du fabricant.", "https://www.voopoo.com/drag-series/drag-5", "Voopoo mod box drag 5 double dual 18650"),
            SharedCatalogItem("aspire-zelos-3", "Aspire Zelos 3", "Box", "Box à batterie intégrée 3200 mAh selon le manuel Aspire; gamme associée au clearomiseur Nautilus 3.", "https://www.aspirecig.com/wp-content/uploads/2023/03/Zelos-3-kit-user-manual.pdf", "Aspire box mod zelos 3 batterie intégrée"),
            SharedCatalogItem("geekvape-z-tank-5", "Geekvape Z (Zeus) Sub-ohm Tank 5", "Clearomiseurs", "Clearomiseur de la plateforme Z Series. Vérifiez la génération exacte du réservoir et la référence de résistance Z compatible.", "https://store.geekvape.com/products/geekvape-z-zeus-sub-ohm-tank-5", "Geekvape Z Zeus tank clearomiseur z sub ohm"),
            SharedCatalogItem("aspire-nautilus-3s", "Aspire Nautilus 3S", "Clearomiseurs", "Clearomiseur MTL compatible avec plusieurs familles de résistances Nautilus. Confirmez les références acceptées par la fiche du fabricant.", "https://www.aspirecig.com/nautilus-series/nautilus-3s/", "Aspire nautilus 3s tank clearomiseur mtl"),
            SharedCatalogItem("vaporesso-itank-t", "Vaporesso iTank T", "Clearomiseurs", "Clearomiseur utilisant la plateforme GTi. La fiche fabricant indique notamment des résistances GTi 0,2 Ω et 0,4 Ω selon le kit.", "https://store.vaporesso.com/en-lv/products/itank-t", "Vaporesso itank t tank GTi clearomiseur"),
            SharedCatalogItem("voopoo-uforce-x", "VOOPOO UFORCE-X", "Clearomiseurs", "Clearomiseur de la plateforme PnP X; la compatibilité concerne les résistances PnP X, pas toute la famille PnP.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "Voopoo uforce x clearomiseur tank PnP X"),
            SharedCatalogItem("innokin-zenith-pro", "Innokin Zenith Pro", "Clearomiseurs", "Clearomiseur MTL utilisant la plateforme Z-Coil. Vérifiez la version et la résistance indiquées sur la notice.", "https://www.innokin.com/zenith-pro", "Innokin zenith pro tank clearomiseur Z coil"),
            SharedCatalogItem("geekvape-zx-ii-rta", "Geekvape ZX II RTA", "Atomiseurs reconstructibles", "Atomiseur reconstructible RTA. La fiche fabricant et la notice exactes restent nécessaires pour montage et utilisation.", "https://us.geekvape.com/product/Geekvape-ZX-II-RTA.html", "Geekvape ZX II RTA atomiseur reconstructible"),
            SharedCatalogItem("wotofo-profile-m", "Wotofo Profile M RTA", "Atomiseurs reconstructibles", "RTA mesh reconstructible. La résistance calculée est une estimation: mesurez le montage et faites une vérification indépendante; aucune fiche catalogue ne certifie la sécurité.", "https://www.wotofo.com/pages/profile-m-rta", "Wotofo Profile M RTA mesh atomiseur reconstructible"),
            SharedCatalogItem("nautilus-03", "Aspire Nautilus Mesh 0,3 Ω", "Résistances préfabriquées", "Aspire Nautilus — 0,3 Ω, plage fabricant 23–28 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.aspirecig.com/coil-platform/nautilus-coils/", "Aspire Nautilus mesh coil résistance 0,3 ohm"),
            SharedCatalogItem("nautilus-10", "Aspire Nautilus Mesh 1,0 Ω", "Résistances préfabriquées", "Aspire Nautilus — 1,0 Ω, plage fabricant 13–15 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.aspirecig.com/coil-platform/nautilus-coils/", "Aspire Nautilus mesh coil résistance 1,0 ohm"),
            SharedCatalogItem("nautilus-16", "Aspire Nautilus BVC 1,6 Ω", "Résistances préfabriquées", "Aspire Nautilus — 1,6 Ω, plage fabricant 7–11 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.aspirecig.com/coil-platform/nautilus-coils/", "Aspire Nautilus BVC coil résistance 1,6 ohm"),
            SharedCatalogItem("nautilus-18", "Aspire Nautilus BVC 1,8 Ω", "Résistances préfabriquées", "Aspire Nautilus — 1,8 Ω, plage fabricant 10–14 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.aspirecig.com/coil-platform/nautilus-coils/", "Aspire Nautilus BVC coil résistance 1,8 ohm"),
            SharedCatalogItem("geekvape-z-015", "Geekvape Z 0,15 Ω", "Résistances préfabriquées", "Geekvape Z Series — 0,15 Ω, plage fabricant 80–90 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://store.geekvape.com/products/geekvape-z-series-coil-5pcs", "Geekvape Z Zeus coil résistance 0,15 ohm"),
            SharedCatalogItem("geekvape-z-02", "Geekvape Z 0,2 Ω", "Résistances préfabriquées", "Geekvape Z Series — 0,2 Ω, plage fabricant 70–80 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://store.geekvape.com/products/geekvape-z-series-coil-5pcs", "Geekvape Z Zeus coil résistance 0,2 ohm"),
            SharedCatalogItem("geekvape-z-04", "Geekvape Z 0,4 Ω", "Résistances préfabriquées", "Geekvape Z Series — 0,4 Ω, plage fabricant 50–60 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://store.geekvape.com/products/geekvape-z-series-coil-5pcs", "Geekvape Z Zeus coil résistance 0,4 ohm"),
            SharedCatalogItem("vaporesso-gti-02", "Vaporesso GTi 0,2 Ω", "Résistances préfabriquées", "Vaporesso GTi — 0,2 Ω, plage fabricant 60–75 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://store.vaporesso.com/en-cz/products/gti-coils-5pcs", "Vaporesso GTi coil résistance 0,2 ohm"),
            SharedCatalogItem("vaporesso-gti-04", "Vaporesso GTi 0,4 Ω", "Résistances préfabriquées", "Vaporesso GTi — 0,4 Ω, plage fabricant 50–60 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://store.vaporesso.com/en-cz/products/gti-coils-5pcs", "Vaporesso GTi coil résistance 0,4 ohm"),
            SharedCatalogItem("voopoo-pnpx-015", "VOOPOO PnP X 0,15 Ω", "Résistances préfabriquées", "VOOPOO PnP X — 0,15 Ω, plage fabricant 60–80 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "VOOPOO PnP X coil résistance 0,15 ohm"),
            SharedCatalogItem("voopoo-pnpx-02", "VOOPOO PnP X 0,2 Ω", "Résistances préfabriquées", "VOOPOO PnP X — 0,2 Ω, plage fabricant 40–60 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "VOOPOO PnP X coil résistance 0,2 ohm"),
            SharedCatalogItem("voopoo-pnpx-04", "VOOPOO PnP X 0,45 Ω", "Résistances préfabriquées", "VOOPOO PnP X — 0,45 Ω, plage fabricant 25–32 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "VOOPOO PnP X coil résistance 0,45 ohm"),
            SharedCatalogItem("voopoo-pnpx-06", "VOOPOO PnP X 0,6 Ω", "Résistances préfabriquées", "VOOPOO PnP X — 0,6 Ω, plage fabricant 18–23 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "VOOPOO PnP X coil résistance 0,6 ohm"),
            SharedCatalogItem("voopoo-pnpx-08", "VOOPOO PnP X 0,8 Ω", "Résistances préfabriquées", "VOOPOO PnP X — 0,8 Ω, plage fabricant 12–16 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.voopoo.com/pnpx-platform/podtank-dtl.html", "VOOPOO PnP X coil résistance 0,8 ohm"),
            SharedCatalogItem("innokin-z-12", "Innokin Z-Coil 1,2 Ω", "Résistances préfabriquées", "Innokin Zenith / Z-Coil — 1,2 Ω, plage fabricant 10–14 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.innokin.com/zenith-pro", "Innokin Zenith Z Coil coil résistance 1,2 ohm"),
            SharedCatalogItem("innokin-z-10", "Innokin Z-Coil 1,0 Ω", "Résistances préfabriquées", "Innokin Zenith / Z-Coil — 1,0 Ω, plage fabricant 20–25 W. Vérifiez que la référence correspond à la version exacte de votre réservoir et suivez sa notice.", "https://www.innokin.com/zenith-pro", "Innokin Zenith Z Coil coil résistance 1,0 ohm"),
            SharedCatalogItem("wotofo-m1", "Wotofo nexMESH M1 0,18 Ω", "Consommables reconstructibles", "Feuille mesh indiquée pour le Profile M — M1 mesh, plage fabricant 45–65 W. Vérifiez la référence, mesurez le montage et effectuez une vérification indépendante. Une estimation ne certifie pas la sécurité.", "https://www.wotofo.com/pages/profile-m-rta", "Wotofo Profile M nexMESH mesh reconstructible résistance Wotofo nexMESH M1 0,18 Ω"),
            SharedCatalogItem("wotofo-clapton", "Wotofo Clapton Mesh 0,15 Ω", "Consommables reconstructibles", "Feuille mesh indiquée pour le Profile M — Clapton mesh, plage fabricant 55–65 W. Vérifiez la référence, mesurez le montage et effectuez une vérification indépendante. Une estimation ne certifie pas la sécurité.", "https://www.wotofo.com/pages/profile-m-rta", "Wotofo Profile M nexMESH mesh reconstructible résistance Wotofo Clapton Mesh 0,15 Ω"),
            SharedCatalogItem("wotofo-a1", "Wotofo nexMESH A1 0,13 Ω", "Consommables reconstructibles", "Feuille mesh indiquée pour le Profile M — A1 mesh, plage fabricant 60–70 W. Vérifiez la référence, mesurez le montage et effectuez une vérification indépendante. Une estimation ne certifie pas la sécurité.", "https://www.wotofo.com/pages/profile-m-rta", "Wotofo Profile M nexMESH mesh reconstructible résistance Wotofo nexMESH A1 0,13 Ω")
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
