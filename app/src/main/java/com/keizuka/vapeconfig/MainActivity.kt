package com.keizuka.vapeconfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.keizuka.vapeconfig.navigation.VapeConfigApp
import com.keizuka.vapeconfig.ui.theme.VapeConfigTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VapeConfigTheme {
                VapeConfigApp()
            }
        }
    }
}
