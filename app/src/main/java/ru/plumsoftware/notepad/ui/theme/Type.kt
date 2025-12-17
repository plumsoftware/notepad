package ru.plumsoftware.notepad.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.R

//val provider = GoogleFont.Provider(
//    providerAuthority = "com.google.android.gms.fonts",
//    providerPackage = "com.google.android.gms",
//    certificates = R.array.com_google_android_gms_fonts_certs
//)
//
//val bodyFontFamily = FontFamily(
//    Font(
//        googleFont = GoogleFont("Roboto"),
//        fontProvider = provider,
//    )
//)
//
//val displayFontFamily = FontFamily(
//    Font(
//        googleFont = GoogleFont("Roboto Serif"),
//        fontProvider = provider,
//    )
//)

val bodyFontFamily = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.Normal),
    Font(R.font.sf_pro_display_medium, FontWeight.Medium),
    Font(R.font.sf_pro_display_bold, FontWeight.Bold),
    Font(R.font.sf_pro_display_black, FontWeight.Black),
)

val baseline = Typography()

val typography = Typography(
    // Заголовки: Жирные и крупные для характера
    displayLarge = baseline.displayLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Black),
    displayMedium = baseline.displayMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold),

    // Подзаголовки: Средняя жирность
    headlineLarge = baseline.headlineLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),

    // Названия в списках: Читаемые, средней жирности
    titleLarge = baseline.titleLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),

    // Основной текст: Обычный вес для легкости чтения
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal, lineHeight = 24.sp), // Чуть больше воздуха
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal),

    // Кнопки и подписи
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium),
)