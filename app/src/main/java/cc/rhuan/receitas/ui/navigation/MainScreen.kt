package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cc.rhuan.receitas.ui.feature.home.HomeScreen
import cc.rhuan.receitas.ui.feature.medications.MedicationsScreen
import cc.rhuan.receitas.ui.feature.menu.MenuScreen
import cc.rhuan.receitas.ui.feature.notifications.NotificationsScreen
import cc.rhuan.receitas.ui.feature.prescriptions.PrescriptionDetailScreen
import cc.rhuan.receitas.ui.feature.prescriptions.PrescriptionsScreen
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.PrescriptionWizardScreen

@Composable
fun MainScreen(openedNotificationId: String? = null, onNotificationOpened: () -> Unit = {}) {
    val navController = rememberNavController()
    LaunchedEffect(openedNotificationId) {
        if (openedNotificationId != null) {
            navController.navigate(NavRoute.Notifications.route) { launchSingleTop = true }
            onNotificationOpened()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = NavRoute.bottomNavItems.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    tonalElevation = 0.dp,
                ) {
                    NavRoute.bottomNavItems.forEach { navRoute ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == navRoute.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(navRoute.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        navRoute.badgeCount?.let { count ->
                                            if (count > 0) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                                ) {
                                                    val displayCount = if (count > 99) "99+" else count.toString()
                                                    Text(text = displayCount)
                                                }
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (isSelected) navRoute.selectedIconRes else navRoute.iconRes),
                                        contentDescription = navRoute.title,
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = navRoute.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable(NavRoute.Home.route) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onCreatePrescription = { navController.navigate("prescription_wizard") { launchSingleTop = true } },
                    onCreateMedication = { navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION") { launchSingleTop = true } },
                    onPrescriptionClick = { navController.navigate("prescription_detail/" + android.net.Uri.encode(it.id)) },
                    onNotificationClick = {
                        navController.navigate(NavRoute.Notifications.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPrescriptions = {
                        navController.navigate(NavRoute.Prescriptions.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMedications = {
                        navController.navigate(NavRoute.Medications.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(NavRoute.Medications.route) {
                MedicationsScreen(
                    viewModel = hiltViewModel(),
                    onAddMedicationClick = {
                        navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION")
                    },
                )
            }
            composable(NavRoute.Prescriptions.route) {
                PrescriptionsScreen(
                    viewModel = hiltViewModel(),
                    onPrescriptionClick = { prescription ->
                        navController.navigate("prescription_detail/${prescription.id}")
                    },
                    onCreatePrescriptionClick = {
                        navController.navigate("prescription_wizard")
                    },
                )
            }
            composable(
                route = NavRoute.PrescriptionWizard.route,
                arguments = listOf(
                    navArgument("prescriptionId") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("entryMode") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
            ) {
                PrescriptionWizardScreen(
                    viewModel = hiltViewModel(),
                    onNavigateUp = { navController.popBackStack() },
                    onViewSaved = { id ->
                        if (id != null) navController.navigate("prescription_detail/" + android.net.Uri.encode(id)) {
                            popUpTo(NavRoute.PrescriptionWizard.route) { inclusive = true }
                            launchSingleTop = true
                        } else navController.popBackStack()
                    },
                )
            }
            composable(
                route = "prescription_detail/{prescriptionId}",
                arguments = listOf(navArgument("prescriptionId") { type = NavType.StringType }),
            ) {
                PrescriptionDetailScreen(
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate("prescription_wizard?prescriptionId=$id")
                    },
                )
            }
            composable(NavRoute.Menu.route) {
                MenuScreen {
                    navController.navigate(NavRoute.Notifications.route)
                }
            }
            composable(NavRoute.Notifications.route) {
                NotificationsScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToPrescription = { id ->
                        navController.navigate("prescription_detail/" + android.net.Uri.encode(id))
                    },
                ) {
                    navController.navigate(NavRoute.Medications.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}
