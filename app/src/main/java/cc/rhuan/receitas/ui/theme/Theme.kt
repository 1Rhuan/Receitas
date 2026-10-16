package cc.rhuan.receitas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DesignDark else DesignLight,
        typography = DesignTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(DesignTokens.small),
            small = RoundedCornerShape(DesignTokens.gap),
            medium = RoundedCornerShape(DesignTokens.padding),
            large = RoundedCornerShape(DesignTokens.cardRadius),
            extraLarge = RoundedCornerShape(DesignTokens.fieldRadius),
        ),
        content = content,
    )
}
