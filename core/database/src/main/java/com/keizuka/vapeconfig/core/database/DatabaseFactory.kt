package com.keizuka.vapeconfig.core.database

import android.content.Context
import androidx.room.Room

object DatabaseFactory {
    fun create(context: Context): VapeConfigDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            VapeConfigDatabase::class.java,
            "vapeconfig.db",
        ).build()
}
