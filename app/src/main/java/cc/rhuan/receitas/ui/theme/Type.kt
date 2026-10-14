package cc.rhuan.receitas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import cc.rhuan.receitas.R

private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semi_bold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

val Typography = Typography(

    // Títulos principais
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 83.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
    ),

    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 65.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
    ),

    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 52.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
    ),

    // Títulos de telas
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 46.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
    ),

    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 41.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
    ),

    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 35.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
    ),

    // Títulos de componentes
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 32.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
    ),

    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
    ),

    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
    ),

    // Texto
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
    ),

    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
    ),

    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
    ),

    // Labels / botões
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
    ),

    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
    ),

    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
    ),
)
