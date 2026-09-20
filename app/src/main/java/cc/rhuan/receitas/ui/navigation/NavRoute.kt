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
        title = "Hoje",
        iconRes = R.drawable.ic_home,
        selectedIconRes = R.drawable.ic_bold_home,
    )

    data object Medications : NavRoute(
        route = "medications",
        title = "Medicamentos",
        iconRes = R.drawable.ic_pills,
        selectedIconRes = R.drawable.ic_bold_pills,
    )

    data object Prescriptions : NavRoute(
        route = "prescriptions",
        title = "Receitas",
        iconRes = R.drawable.ic_document_medicine,
        selectedIconRes = R.drawable.ic_bold_document_medicine,
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
        badgeCount = 2,
    )

    companion object {
        val bottomNavItems = listOf(
            Home,
            Medications,
            Prescriptions,
            Menu,
        )
    }
}
