package ru.plumsoftware.notepad.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
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

// Убедись, что шрифт SF Pro (твоя переменная bodyFontFamily) подключена корректно
val typography = Typography(
    // Large Title (Заголовки экранов, например "Настройки")
    displayLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = 0.37.sp
    ),
    // Title 1
    displayMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.36.sp
    ),
    // Title 2 (Используем для заголовков секций)
    titleLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.SemiBold, // В iOS заголовки чаще SemiBold
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.35.sp
    ),
    // Title 3 (Заголовок карточки заметки)
    titleMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, // Чуть крупнее, чем дефолтный Android
        lineHeight = 24.sp,
        letterSpacing = 0.38.sp
    ),
    // Body (Основной текст заметки) - Золотой стандарт iOS
    bodyLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp, // ЭТО ВАЖНО: В iOS стандарт 17pt, а не 16sp
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp // Отрицательный трекинг для SF Pro обязателен
    ),
    // Callout (Вторичный текст, задачи)
    bodyMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.32).sp
    ),
    // Subhead
    bodySmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    ),
    // Caption 1 (Дата, подписи)
    labelMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // Caption 2 (Мелкие подписи)
    labelSmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.06.sp
    )
)