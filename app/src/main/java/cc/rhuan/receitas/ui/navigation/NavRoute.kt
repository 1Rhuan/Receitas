package cc.rhuan.receitas.ui.navigation

import androidx.annotation.DrawableRes
import cc.rhuan.receitas.R

sealed class NavRoute(
    val route: String,
    val title: String,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val selectedIconRes: Int,
    val badgeCount: Int? = null,
) {
    data object Home : NavRoute(
        route = "home",
        title = "Início",
        iconRes = R.drawable.ic_nav_home,
        selectedIconRes = R.drawable.ic_bold_home,
    )

    data object Prescriptions : NavRoute(
        route = "prescriptions",
        title = "Receitas",
        iconRes = R.drawable.ic_nav_prescriptions,
        selectedIconRes = R.drawable.ic_bold_document_medicine,
    )

    data object Medications : NavRoute(
        route = "medications",
        title = "Medicamentos",
        iconRes = R.drawable.ic_nav_medications,
        selectedIconRes = R.drawable.ic_bold_pills,
    )

    data object Menu : NavRoute(
        route = "menu",
        title = "Menu",
        iconRes = R.drawable.ic_hamburger_menu,
        selectedIconRes = R.drawable.ic_hamburger_menu,
    )

    data object Notifications : NavRoute(
        route = "notifications",
        title = "Notificações",
        iconRes = R.drawable.ic_bell_bing,
        selectedIconRes = R.drawable.ic_bold_bell_bing,
        badgeCount = 0,
    )

    data object PrescriptionWizard : NavRoute(
        route = "prescription_wizard?prescriptionId={prescriptionId}&entryMode={entryMode}&renewalOf={renewalOf}&medicationId={medicationId}&addToPrescriptionId={addToPrescriptionId}",
        title = "Nova receita",
        iconRes = R.drawable.ic_document_medicine,
        selectedIconRes = R.drawable.ic_bold_document_medicine,
    )

    companion object {
        val bottomNavItems get() = listOf(
            Home,
            Prescriptions,
            Medications,
            Menu,
        )
    }
}
