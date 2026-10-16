package cc.rhuan.receitas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cc.rhuan.receitas.ui.feature.home.HomeScreen
import cc.rhuan.receitas.ui.feature.medications.MedicationsScreen
import cc.rhuan.receitas.ui.feature.notifications.NotificationsScreen
import cc.rhuan.receitas.ui.feature.prescriptions.PrescriptionDetailScreen
import cc.rhuan.receitas.ui.feature.prescriptions.PrescriptionsScreen
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.PrescriptionWizardScreen

import androidx.navigation.NavHostController

@Composable
fun PrescriptionsNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.route,
        modifier = modifier,
    ) {
        composable(NavRoute.Home.route) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onCreatePrescription = { navController.navigate("prescription_wizard") { launchSingleTop = true } },
                onCreateMedication = { navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION") { launchSingleTop = true } },
                onPrescriptionClick = { navController.navigate("prescription_detail/" + android.net.Uri.encode(it.id)) },
                onMedicationClick = { navController.navigate("medication_detail/" + android.net.Uri.encode(it.id)) },
                onRenewPrescription = { navController.navigate("prescription_wizard?renewalOf=" + android.net.Uri.encode(it)) },
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
                onMedicationClick = { navController.navigate("medication_detail/" + android.net.Uri.encode(it.id)) },
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
                navArgument("renewalOf") { type = NavType.StringType; nullable = true },
                navArgument("medicationId") { type = NavType.StringType; nullable = true },
                navArgument("addToPrescriptionId") { type = NavType.StringType; nullable = true },
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
                onAddSavedMedication = { id ->
                    navController.navigate("prescription_detail/" + android.net.Uri.encode(id)) {
                        popUpTo(NavRoute.PrescriptionWizard.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION&addToPrescriptionId=" + android.net.Uri.encode(id))
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
                onMedicationClick = { id -> navController.navigate("medication_detail/" + android.net.Uri.encode(id)) },
                onAddMedicationClick = { id -> navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION&addToPrescriptionId=" + android.net.Uri.encode(id)) },
                onRenewClick = { id -> navController.navigate("prescription_wizard?renewalOf=" + android.net.Uri.encode(id)) },
                onEditClick = { id ->
                    navController.navigate("prescription_wizard?prescriptionId=$id")
                },
            )
        }
        composable("medication_detail/{medicationId}", arguments = listOf(navArgument("medicationId") { type = NavType.StringType })) {
            cc.rhuan.receitas.ui.feature.medications.MedicationDetailScreen(viewModel = hiltViewModel(), onBack = { navController.popBackStack() }, onEdit = { id -> navController.navigate("prescription_wizard?entryMode=STANDALONE_MEDICATION&medicationId=" + android.net.Uri.encode(id)) },
                onPrescriptionsClick = { id -> navController.navigate("prescriptions_for_medication/" + android.net.Uri.encode(id)) })
        }
        composable("prescriptions_for_medication/{medicationId}", arguments = listOf(navArgument("medicationId") { type = NavType.StringType })) { entry ->
            PrescriptionsScreen(viewModel = hiltViewModel(), medicationFilterId = entry.arguments?.getString("medicationId"),
                onBack = { navController.popBackStack() },
                onPrescriptionClick = { navController.navigate("prescription_detail/" + android.net.Uri.encode(it.id)) },
                onCreatePrescriptionClick = { navController.navigate("prescription_wizard") })
        }
        composable(NavRoute.Notifications.route) {
            NotificationsScreen(
                viewModel = hiltViewModel(),
                onRenewPrescription = { id -> navController.navigate("prescription_wizard?renewalOf=" + android.net.Uri.encode(id)) },
                onNavigateToPrescription = { id ->
                    navController.navigate("prescription_detail/" + android.net.Uri.encode(id))
                },
                onNavigateToMedication = { id -> navController.navigate("medication_detail/" + android.net.Uri.encode(id)) },
            )
        }
    }
}
