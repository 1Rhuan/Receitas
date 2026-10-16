package cc.rhuan.receitas.ui.navigation

import androidx.annotation.DrawableRes
import cc.rhuan.receitas.R

sealed class NavRoute(
    val route: String,
    val titleRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val selectedIconRes: Int,
    val badgeCount: Int? = null,
) {
    data object Home : NavRoute(
        route = "home",
        titleRes = R.string.nav_home,
        iconRes = R.drawable.ic_nav_home,
        selectedIconRes = R.drawable.ic_bold_home,
    )

    data object Prescriptions : NavRoute(
        route = "prescriptions",
        titleRes = R.string.nav_prescriptions,
        iconRes = R.drawable.ic_nav_prescriptions,
        selectedIconRes = R.drawable.ic_bold_document_medicine,
    )

    data object Medications : NavRoute(
        route = "medications",
        titleRes = R.string.nav_medications,
        iconRes = R.drawable.ic_nav_medications,
        selectedIconRes = R.drawable.ic_bold_pills,
    )

    data object Notifications : NavRoute(
        route = "notifications",
        titleRes = R.string.nav_notifications,
        iconRes = R.drawable.ic_bell_bing,
        selectedIconRes = R.drawable.ic_bold_bell_bing,
        badgeCount = 0,
    )

    data object PrescriptionWizard : NavRoute(
        route = "prescription_wizard?prescriptionId={prescriptionId}&entryMode={entryMode}&renewalOf={renewalOf}&medicationId={medicationId}&addToPrescriptionId={addToPrescriptionId}",
        titleRes = R.string.nav_new_prescription,
        iconRes = R.drawable.ic_document_medicine,
        selectedIconRes = R.drawable.ic_bold_document_medicine,
    )

    companion object {
        val bottomNavItems get() = listOf(
            Home,
            Prescriptions,
            Medications,
        )
    }
}
