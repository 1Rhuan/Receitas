package cc.rhuan.receitas.ui.navigation

import androidx.annotation.DrawableRes

data class NavItem(
    val title: String,
    @param:DrawableRes val iconRes: Int,
    val route: String,
    val badgeCount: Int? = null
)