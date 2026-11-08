package com.keizuka.vapeconfig.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keizuka.vapeconfig.BuildConfig
import com.keizuka.vapeconfig.VapeConfigApplication
import com.keizuka.vapeconfig.feature.batteries.BatteriesScreen
import com.keizuka.vapeconfig.feature.catalog.CatalogScreen
import com.keizuka.vapeconfig.feature.configurator.ConfiguratorScreen
import com.keizuka.vapeconfig.feature.liquids.LiquidsScreen
import com.keizuka.vapeconfig.feature.settings.SettingsScreen
import com.keizuka.vapeconfig.ui.DashboardScreen
import com.keizuka.vapeconfig.ui.Section

private object Routes {
    const val Dashboard = "dashboard"
    const val Section = "section/{section}"
    fun section(name: String) = "section/$name"
}

@Composable
fun VapeConfigApp() {
    val navController = rememberNavController()
    val database = (LocalContext.current.applicationContext as VapeConfigApplication).database
    Scaffold { padding ->
        NavHost(navController, startDestination = Routes.Dashboard, modifier = Modifier.padding(padding)) {
            composable(Routes.Dashboard) {
                DashboardScreen(onOpenSection = { navController.navigate(Routes.section(it.route)) })
            }
            composable(Routes.Section, arguments = listOf(navArgument("section") { type = NavType.StringType })) { entry ->
                when (entry.arguments?.getString("section")) {
                    Section.Catalog.route -> CatalogScreen(database.catalogItems()) { navController.popBackStack() }
                    Section.Configurator.route -> ConfiguratorScreen(database.coilConfigurations()) { navController.popBackStack() }
                    Section.Liquids.route -> LiquidsScreen(database.liquidRecipes()) { navController.popBackStack() }
                    Section.Batteries.route -> BatteriesScreen(database.batteryProfiles()) { navController.popBackStack() }
                    Section.Settings.route -> SettingsScreen(BuildConfig.VERSION_NAME, database) { navController.popBackStack() }
                }
            }
        }
    }
}
