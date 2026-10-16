package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(openedNotificationId: String? = null, onNotificationOpened: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedRoute = NavRoute.bottomNavItems.firstOrNull { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }?.route
    LaunchedEffect(openedNotificationId) {
        if (openedNotificationId != null) {
            navController.navigate(NavRoute.Notifications.route) { launchSingleTop = true }
            onNotificationOpened()
        }
    }
    MainScreenContent(
        selectedRoute = selectedRoute,
        showBottomBar = NavRoute.bottomNavItems.any { it.route == currentDestination?.route },
        onCreateRecord = when (selectedRoute) {
            NavRoute.Prescriptions.route -> ({ navController.navigate("prescription_wizard") { launchSingleTop = true } })
            NavRoute.Medications.route -> ({ navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION") { launchSingleTop = true } })
            else -> null
        },
        onSelectDestination = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
    ) { innerPadding ->
        PrescriptionsNavHost(navController, Modifier.padding(innerPadding).consumeWindowInsets(innerPadding))
    }
}
