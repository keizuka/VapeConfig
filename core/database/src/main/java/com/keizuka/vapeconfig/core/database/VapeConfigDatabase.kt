package com.keizuka.vapeconfig.core.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val source: String = "local",
    val notes: String = "",
    @ColumnInfo(defaultValue = "''") val connection: String = "",
)

@Entity(tableName = "liquid_recipes")
data class LiquidRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val updatedAtEpochMillis: Long,
    val recipeJson: String,
)

@Entity(tableName = "coil_configurations")
data class CoilConfigurationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isRebuildable: Boolean,
    val notes: String = "",
)

@Entity(tableName = "battery_profiles")
data class BatteryProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,
    val model: String = "",
    val format: String = "",
    val capacityMah: String = "",
    val currentRatingA: String = "",
    val manufacturerSource: String = "",
    val notes: String = "",
)

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CatalogItemEntity>
    @Upsert suspend fun save(item: CatalogItemEntity)
    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface LiquidRecipeDao {
    @Query("SELECT * FROM liquid_recipes ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<LiquidRecipeEntity>
    @Insert suspend fun insert(recipe: LiquidRecipeEntity): Long
    @Query("DELETE FROM liquid_recipes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CoilConfigurationDao {
    @Query("SELECT * FROM coil_configurations ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CoilConfigurationEntity>
    @Insert suspend fun insert(configuration: CoilConfigurationEntity): Long
    @Query("DELETE FROM coil_configurations WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface BatteryProfileDao {
    @Query("SELECT * FROM battery_profiles ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<BatteryProfileEntity>
    @Insert suspend fun insert(profile: BatteryProfileEntity): Long
    @Query("DELETE FROM battery_profiles WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(
    entities = [CatalogItemEntity::class, LiquidRecipeEntity::class, CoilConfigurationEntity::class, BatteryProfileEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class VapeConfigDatabase : RoomDatabase() {
    abstract fun catalogItems(): CatalogItemDao
    abstract fun liquidRecipes(): LiquidRecipeDao
    abstract fun coilConfigurations(): CoilConfigurationDao
    abstract fun batteryProfiles(): BatteryProfileDao
}
