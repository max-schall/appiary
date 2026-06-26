package io.github.max_schall.appiary.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.max_schall.appiary.nfc.NfcController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.i18n.labelRes
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.max_schall.appiary.ui.components.QuickAddSheet
import io.github.max_schall.appiary.ui.screen.apiaries.ApiariesScreen
import io.github.max_schall.appiary.ui.screen.apiaries.ApiaryDetailScreen
import io.github.max_schall.appiary.ui.screen.hives.HiveDetailScreen
import io.github.max_schall.appiary.ui.screen.hives.HivesScreen
import io.github.max_schall.appiary.ui.screen.log.FeedingScreen
import io.github.max_schall.appiary.ui.screen.log.HarvestScreen
import io.github.max_schall.appiary.ui.screen.log.InspectionScreen
import io.github.max_schall.appiary.ui.screen.log.MiteCheckScreen
import io.github.max_schall.appiary.ui.screen.log.NoteScreen
import io.github.max_schall.appiary.ui.screen.log.TreatmentScreen
import io.github.max_schall.appiary.ui.screen.photo.CameraCaptureScreen
import io.github.max_schall.appiary.ui.screen.recordbook.RecordBookScreen
import io.github.max_schall.appiary.ui.screen.settings.SeasonalProfileScreen
import io.github.max_schall.appiary.ui.screen.settings.SettingsScreen
import io.github.max_schall.appiary.ui.screen.tasks.TasksScreen
import io.github.max_schall.appiary.ui.screen.today.TodayScreen

/**
 * Root composable. Hosts the adaptive navigation shell:
 *  - compact width  -> bottom navigation bar + corner FAB
 *  - medium/expanded -> navigation rail with a leading FAB
 *
 * The NavHost and quick-add sheet are shared across both layouts.
 */
@Composable
fun AppiaryApp(widthSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    var showQuickAdd by remember { mutableStateOf(false) }
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact

    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = TopDestination.fromRoute(backStackEntry?.destination?.route)
        ?: TopDestination.Today

    // A scanned NFC tag that matches a hive opens that hive.
    LaunchedEffect(Unit) {
        NfcController.openHiveRequests.collect { hiveId ->
            navController.navigate("hive/$hiveId")
        }
    }

    if (useRail) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(header = {
                FloatingActionButton(onClick = { showQuickAdd = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.quick_add))
                }
            }) {
                TopDestination.entries.forEach { dest ->
                    val label = stringResource(dest.labelRes())
                    NavigationRailItem(
                        selected = current == dest,
                        onClick = { navController.navigateTopLevel(dest) },
                        icon = { Icon(dest.icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
            Scaffold { padding ->
                AppNavHost(navController, Modifier.padding(padding))
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TopDestination.entries.forEach { dest ->
                        val label = stringResource(dest.labelRes())
                        NavigationBarItem(
                            selected = current == dest,
                            onClick = { navController.navigateTopLevel(dest) },
                            icon = { Icon(dest.icon, contentDescription = label) },
                            label = { Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showQuickAdd = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.fab_log)) },
                )
            },
        ) { padding ->
            AppNavHost(navController, Modifier.padding(padding))
        }
    }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onAction = { action ->
                showQuickAdd = false
                navController.navigate(LogRoutes.forAction(action))
            },
        )
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    fun openHive(id: String) = navController.navigate("hive/$id")
    fun openApiary(id: String) = navController.navigate("apiary/$id")

    NavHost(
        navController = navController,
        startDestination = TopDestination.Today.route,
        modifier = modifier,
    ) {
        composable(TopDestination.Today.route) {
            TodayScreen(
                onOpenHive = ::openHive,
                onOpenRecordBook = { apiaryId -> navController.navigate("recordbook/$apiaryId") },
                onOpenInsights = { navController.navigate("analytics") },
                onOpenSearch = { navController.navigate("search") },
            )
        }
        composable("search") {
            io.github.max_schall.appiary.ui.screen.search.SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenHive = ::openHive,
            )
        }
        composable("analytics") {
            io.github.max_schall.appiary.ui.screen.analytics.AnalyticsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(TopDestination.Apiaries.route) {
            ApiariesScreen(
                onOpenApiary = ::openApiary,
                onOpenMap = { navController.navigate("map") },
            )
        }
        composable("map") {
            io.github.max_schall.appiary.ui.screen.map.MapScreen(
                onBack = { navController.popBackStack() },
                onOpenApiary = ::openApiary,
            )
        }
        composable(
            route = "apiary/{apiaryId}",
            arguments = listOf(navArgument("apiaryId") { type = NavType.StringType }),
        ) {
            ApiaryDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenHive = ::openHive,
                onOpenRecordBook = { apiaryId -> navController.navigate("recordbook/$apiaryId") },
            )
        }
        composable(
            route = "recordbook/{apiaryId}",
            arguments = listOf(navArgument("apiaryId") { type = NavType.StringType }),
        ) {
            RecordBookScreen(onBack = { navController.popBackStack() })
        }
        composable(TopDestination.Hives.route) {
            HivesScreen(onOpenHive = ::openHive)
        }
        composable(
            route = "hive/{hiveId}",
            arguments = listOf(navArgument("hiveId") { type = NavType.StringType }),
        ) {
            HiveDetailScreen(
                onBack = { navController.popBackStack() },
                onLogAction = { action, hiveId ->
                    navController.navigate(LogRoutes.withHive(LogRoutes.forAction(action), hiveId))
                },
                onAddPhoto = { hiveId -> navController.navigate("photo/$hiveId") },
            )
        }
        composable(
            route = "photo/{hiveId}",
            arguments = listOf(navArgument("hiveId") { type = NavType.StringType }),
        ) {
            CameraCaptureScreen(onDone = { navController.popBackStack() })
        }
        composable(TopDestination.Tasks.route) {
            TasksScreen(onOpenHive = ::openHive)
        }

        // --- Logging flows (optional ?hiveId arg) ---
        LogRoutes.all.forEach { base ->
            composable(
                route = LogRoutes.pattern(base),
                arguments = listOf(
                    navArgument("hiveId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                ),
            ) {
                val done = { navController.popBackStack(); Unit }
                when (base) {
                    LogRoutes.INSPECTION -> InspectionScreen(onDone = done)
                    LogRoutes.MITE -> MiteCheckScreen(onDone = done)
                    LogRoutes.TREATMENT -> TreatmentScreen(onDone = done)
                    LogRoutes.FEEDING -> FeedingScreen(onDone = done)
                    LogRoutes.HARVEST -> HarvestScreen(onDone = done)
                    LogRoutes.NOTE -> NoteScreen(onDone = done)
                }
            }
        }
        composable(TopDestination.Settings.route) {
            SettingsScreen(
                onEditSeasonalProfile = { navController.navigate("settings/seasonal") },
                onOpenInventory = { navController.navigate("inventory") },
            )
        }
        composable("inventory") {
            io.github.max_schall.appiary.ui.screen.inventory.InventoryScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable("settings/seasonal") {
            SeasonalProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Standard top-level navigation: single-top, restore state, pop to start. */
private fun NavController.navigateTopLevel(dest: TopDestination) {
    navigate(dest.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
