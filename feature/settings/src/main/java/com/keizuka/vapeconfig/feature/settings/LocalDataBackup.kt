package com.keizuka.vapeconfig.feature.settings

import androidx.room.withTransaction
import com.keizuka.vapeconfig.core.database.BatteryProfileEntity
import com.keizuka.vapeconfig.core.database.CatalogItemEntity
import com.keizuka.vapeconfig.core.database.CoilConfigurationEntity
import com.keizuka.vapeconfig.core.database.LiquidRecipeEntity
import com.keizuka.vapeconfig.core.database.VapeConfigDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal data class BackupContents(
    val catalog: List<CatalogItemEntity>,
    val recipes: List<LiquidRecipeEntity>,
    val coils: List<CoilConfigurationEntity>,
    val batteries: List<BatteryProfileEntity>,
)

internal data class RestoreCounts(
    val catalog: Int,
    val recipes: Int,
    val coils: Int,
    val batteries: Int,
)

internal suspend fun VapeConfigDatabase.createBackupJson(): String {
    val backup = withTransaction {
        BackupContents(
            catalogItems().getAll(),
            liquidRecipes().getAll(),
            coilConfigurations().getAll(),
            batteryProfiles().getAll(),
        )
    }
    return JSONObject().apply {
        put("format", "VapeConfigBackup")
        put("formatVersion", 1)
        put("createdAtEpochMillis", System.currentTimeMillis())
        put("catalog", JSONArray().apply {
            backup.catalog.forEach { item -> put(JSONObject()
                .put("id", item.id).put("name", item.name).put("category", item.category)
                .put("source", item.source).put("notes", item.notes).put("connection", item.connection)) }
        })
        put("recipes", JSONArray().apply {
            backup.recipes.forEach { item -> put(JSONObject()
                .put("id", item.id).put("name", item.name).put("updatedAtEpochMillis", item.updatedAtEpochMillis)
                .put("recipeJson", item.recipeJson)) }
        })
        put("coils", JSONArray().apply {
            backup.coils.forEach { item -> put(JSONObject()
                .put("id", item.id).put("name", item.name).put("isRebuildable", item.isRebuildable)
                .put("notes", item.notes)) }
        })
        put("batteries", JSONArray().apply {
            backup.batteries.forEach { item -> put(JSONObject()
                .put("id", item.id).put("name", item.name).put("kind", item.kind).put("model", item.model)
                .put("format", item.format).put("capacityMah", item.capacityMah)
                .put("currentRatingA", item.currentRatingA).put("manufacturerSource", item.manufacturerSource)
                .put("notes", item.notes)) }
        })
    }.toString(2)
}

internal fun parseBackupJson(json: String): BackupContents {
    val root = JSONObject(json)
    require(root.getString("format") == "VapeConfigBackup") { "Fichier de sauvegarde VapeConfig invalide." }
    require(root.getInt("formatVersion") == 1) { "Version de sauvegarde non prise en charge." }
    fun array(key: String) = root.getJSONArray(key)
    fun JSONObject.string(key: String, default: String = ""): String =
        if (has(key) && !isNull(key)) getString(key) else default
    return BackupContents(
        catalog = array("catalog").objects().map {
            CatalogItemEntity(it.getString("id"), it.getString("name"), it.getString("category"),
                it.string("source", "local"), it.string("notes"), it.string("connection"))
        },
        recipes = array("recipes").objects().map {
            LiquidRecipeEntity(0, it.getString("name"), it.getLong("updatedAtEpochMillis"), it.getString("recipeJson"))
        },
        coils = array("coils").objects().map {
            CoilConfigurationEntity(0, it.getString("name"), it.getBoolean("isRebuildable"), it.string("notes"))
        },
        batteries = array("batteries").objects().map {
            BatteryProfileEntity(0, it.getString("name"), it.getString("kind"), it.string("model"),
                it.string("format"), it.string("capacityMah"), it.string("currentRatingA"),
                it.string("manufacturerSource"), it.string("notes"))
        },
    )
}

private fun JSONArray.objects(): List<JSONObject> =
    (0 until length()).map { getJSONObject(it) }

internal suspend fun VapeConfigDatabase.restoreAdditively(backup: BackupContents): RestoreCounts =
    withTransaction {
        val existingCatalogIds = catalogItems().getAll().mapTo(mutableSetOf()) { it.id }
        backup.catalog.forEach { item ->
            var id = item.id
            if (!existingCatalogIds.add(id)) {
                do { id = "import-${UUID.randomUUID()}" } while (!existingCatalogIds.add(id))
            }
            catalogItems().save(item.copy(id = id))
        }
        backup.recipes.forEach { liquidRecipes().insert(it.copy(id = 0)) }
        backup.coils.forEach { coilConfigurations().insert(it.copy(id = 0)) }
        backup.batteries.forEach { batteryProfiles().insert(it.copy(id = 0)) }
        RestoreCounts(backup.catalog.size, backup.recipes.size, backup.coils.size, backup.batteries.size)
    }
