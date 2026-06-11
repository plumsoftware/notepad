package ru.plumsoftware.notepad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Светлая тема (Material / Theme.kt) ---
val primaryLight = Color(0xFF1C7FE3)
val onPrimaryLight = Color.White
val primaryContainerLight = Color(0xFFE6F1FB)
val onPrimaryContainerLight = Color(0xFF001C38)

val backgroundLight = Color(0xFFF5F5F7)
val onBackgroundLight = Color(0xFF1A1A1E)

val surfaceLight = Color(0xFFFFFFFF)
val onSurfaceLight = Color(0xFF1A1A1E)

val surfaceVariantLight = Color(0xFFF0F0F2)
val onSurfaceVariantLight = Color(0xFF6B6B6F)

val textTertiaryLight = Color(0xFFABABAF)

val errorLight = Color(0xFFFF3B30)
val onErrorLight = Color.White

val outlineLight = Color(0xFFE0E0E4)
val outlineVariantLight = Color(0xFFEEEEF2)

val secondaryLight = Color(0xFF6B6B6F)
val onSecondaryLight = Color.White
val secondaryContainerLight = Color(0xFFF0F0F2)
val onSecondaryContainerLight = Color(0xFF1A1A1E)

val tertiaryLight = Color(0xFF5856D6)
val onTertiaryLight = Color.White
val tertiaryContainerLight = Color(0xFFE0E0FF)
val onTertiaryContainerLight = Color(0xFF24244A)

val errorContainerLight = Color(0xFFFFE5E5)
val onErrorContainerLight = Color(0xFF410002)

val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2F3036)
val inverseOnSurfaceLight = Color(0xFFF0F0F7)
val inversePrimaryLight = Color(0xFFAECBFA)
val surfaceDimLight = Color(0xFFD9D9E0)
val surfaceBrightLight = Color(0xFFF9FAFF)

val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = backgroundLight
val surfaceContainerLight = surfaceLight
val surfaceContainerHighLight = surfaceVariantLight
val surfaceContainerHighestLight = outlineVariantLight

// --- Тёмная тема ---
val primaryDark = Color(0xFF4AA3F5)
val onPrimaryDark = Color.Black
val primaryContainerDark = Color(0xFF1C3A5F)
val onPrimaryContainerDark = Color.White

val backgroundDark = Color(0xFF1C1C1E)
val onBackgroundDark = Color(0xFFF5F5F7)

val surfaceDark = Color(0xFF2C2C2E)
val onSurfaceDark = Color(0xFFF5F5F7)

val surfaceVariantDark = Color(0xFF3A3A3C)
val onSurfaceVariantDark = Color(0xFF9A9A9F)

val textTertiaryDark = Color(0xFF5A5A60)

val errorDark = Color(0xFFFF453A)
val onErrorDark = Color.Black

val outlineDark = Color(0xFF3A3A3C)
val outlineVariantDark = Color(0xFF2C2C2E)

val secondaryDark = Color(0xFF9A9A9F)
val onSecondaryDark = Color.White
val secondaryContainerDark = Color(0xFF3A3A3C)
val onSecondaryContainerDark = Color(0xFFF0F0F2)

val tertiaryDark = Color(0xFF5E5CE6)
val onTertiaryDark = Color.White
val tertiaryContainerDark = Color(0xFF3F3F70)
val onTertiaryContainerDark = Color(0xFFE0E0FF)

val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE2E2E6)
val inverseOnSurfaceDark = Color(0xFF2F3036)
val inversePrimaryDark = primaryLight
val surfaceDimDark = Color(0xFF111318)
val surfaceBrightDark = Color(0xFF37393E)

val surfaceContainerLowestDark = Color(0xFF000000)
val surfaceContainerLowDark = surfaceDark
val surfaceContainerDark = surfaceVariantDark
val surfaceContainerHighDark = Color(0xFF3A3A3C)
val surfaceContainerHighestDark = Color(0xFF48484A)

// --- Бренд / утилиты ---
val AppleBlueLight = primaryLight
val AppleBlueDark = primaryDark
val deleteColor = Color(0xFFEF5350)

// --- Палитра заметок ---
val NoteYellow = Color(0xFFFFFDE7)
val NoteBlue = Color(0xFFE6F1FB)
val NoteGreen = Color(0xFFEDF7EE)
val NotePink = Color(0xFFFBEAF0)
val NotePurple = Color(0xFFEEEDFE)
val NoteOrange = Color(0xFFFFF3E0)
val NoteGray = Color(0xFFF1EFE8)

val NoteYellowDark = Color(0xFF2C2810)
val NoteBlueDark = Color(0xFF0D2440)
val NoteGreenDark = Color(0xFF0A2210)
val NotePinkDark = Color(0xFF2C0E1A)
val NotePurpleDark = Color(0xFF1A1835)
val NoteOrangeDark = Color(0xFF2C1A08)
val NoteGrayDark = Color(0xFF222220)

val NoteOrangeLight = NoteOrange
val NoteBlueLight = NoteBlue
val NoteGreenLight = NoteGreen
val NotePinkLight = NotePink
val NoteYellowLight = NoteYellow
val NotePurpleLight = NotePurple
val NoteGrayLight = NoteGray

data class NoteColorOption(val color: Color?, val label: String)

@Composable
fun noteColorOptions(): List<NoteColorOption> {
    val isDark = isSystemInDarkTheme()
    return listOf(
        NoteColorOption(null, "Нет"),
        NoteColorOption(if (isDark) NoteYellowDark else NoteYellow, "Жёлтый"),
        NoteColorOption(if (isDark) NoteBlueDark else NoteBlue, "Синий"),
        NoteColorOption(if (isDark) NoteGreenDark else NoteGreen, "Зелёный"),
        NoteColorOption(if (isDark) NotePinkDark else NotePink, "Розовый"),
        NoteColorOption(if (isDark) NotePurpleDark else NotePurple, "Фиолетовый"),
        NoteColorOption(if (isDark) NoteOrangeDark else NoteOrange, "Оранжевый"),
        NoteColorOption(if (isDark) NoteGrayDark else NoteGray, "Серый"),
    )
}

@Composable
fun resolveNoteColor(colorLong: Long): Color {
    if (colorLong == 0L) return MaterialTheme.colorScheme.surface
    val color = Color(colorLong.toULong())
    if (colorLong == 0xFFFFFFFFL && isSystemInDarkTheme()) {
        return surfaceDark
    }
    return color
}
