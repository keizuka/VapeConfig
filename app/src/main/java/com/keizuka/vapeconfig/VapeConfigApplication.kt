package com.keizuka.vapeconfig

import android.app.Application
import com.keizuka.vapeconfig.core.database.DatabaseFactory

class VapeConfigApplication : Application() {
    val database by lazy { DatabaseFactory.create(this) }
}
