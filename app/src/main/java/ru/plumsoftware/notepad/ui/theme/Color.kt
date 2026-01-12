package ru.plumsoftware.notepad.ui.theme

import androidx.compose.ui.graphics.Color

// --- ОБЩИЕ АКЦЕНТЫ (Ваш бренд) ---
// Твой синий для светлой темы
val AppleBlueLight = Color(0xFF1E73FC)
// Для темной темы iOS делает цвет чуть светлее, но СОХРАНЯЕТ насыщенность
// Твой текущий pastel blue (AECBFA) слишком бледный для iOS
val AppleBlueDark = Color(0xFF0A84FF)

// --- Light Theme Colors (iOS Style) ---
val primaryLight = AppleBlueLight
val onPrimaryLight = Color.White
val primaryContainerLight = Color(0xFFD3E4FF) // Можно оставить для легких подложек
val onPrimaryContainerLight = Color(0xFF001C38)

// В iOS фон "grouped" экранов (как настройки) - светло-серый
val backgroundLight = Color(0xFFF2F2F7) // System Gray 6
val onBackgroundLight = Color.Black

// Карточки, ячейки, диалоги - чисто белые
val surfaceLight = Color(0xFFFFFFFF)
val onSurfaceLight = Color.Black

// Вторичные поверхности (например, для search bar)
val surfaceVariantLight = Color(0xFFE5E5EA) // System Gray 5
val onSurfaceVariantLight = Color(0xFF8E8E93) // System Gray

// Ошибки
val errorLight = Color(0xFFFF3B30) // Apple Red
val onErrorLight = Color.White

// Разделители
val outlineLight = Color(0xFFC6C6C8) // System Gray 3
val outlineVariantLight = Color(0xFFD1D1D6) // System Gray 4

// --- Secondary (Вторичный) - Используем System Gray для нейтральности ---
val secondaryLight = Color(0xFF8E8E93) // System Gray
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE5E5EA) // System Gray 5 (светлая подложка)
val onSecondaryContainerLight = Color(0xFF1C1C1E)

// --- Tertiary (Третичный) - Используем System Indigo для акцентов ---
val tertiaryLight = Color(0xFF5856D6) // System Indigo
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFE0E0FF)
val onTertiaryContainerLight = Color(0xFF24244A)

// --- Error Containers (Контейнеры ошибок) ---
val errorContainerLight = Color(0xFFFFE5E5) // Очень светлый красный
val onErrorContainerLight = Color(0xFF410002) // Темно-красный текст

val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2F3036)
val inverseOnSurfaceLight = Color(0xFFF0F0F7)
val inversePrimaryLight = Color(0xFFAECBFA)
val surfaceDimLight = Color(0xFFD9D9E0)
val surfaceBrightLight = Color(0xFFF9FAFF)


// --- Dark Theme Colors (iOS Style) ---
// В iOS акцент остается ярким!
val primaryDark = AppleBlueDark
val onPrimaryDark = Color.White // Apple использует белый текст на синих кнопках даже в dark mode
val primaryContainerDark = Color(0xFF0040DD)
val onPrimaryContainerDark = Color.White

// В iOS фон темной темы - ИДЕАЛЬНО ЧЕРНЫЙ (для OLED)
val backgroundDark = Color(0xFF000000)
val onBackgroundDark = Color.White

// Карточки в темной теме - темно-серые, а не черные
val surfaceDark = Color(0xFF1C1C1E) // System Gray 6 Dark
val onSurfaceDark = Color.White

// Вторичные поверхности (search bar dark)
val surfaceVariantDark = Color(0xFF2C2C2E) // System Gray 5 Dark
val onSurfaceVariantDark = Color(0xFF8E8E93)

// Ошибки
val errorDark = Color(0xFFFF453A) // Apple Red Dark
val onErrorDark = Color.Black

// Разделители
val outlineDark = Color(0xFF38383A)
val outlineVariantDark = Color(0xFF48484A)

// --- Цвета контейнеров (для совместимости с Material 3 компонентами) ---
// В iOS они не используются так активно, но для M3 нужны
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF2F2F7)
val surfaceContainerLight = Color(0xFFFFFFFF) // Карточки
val surfaceContainerHighLight = Color(0xFFE5E5EA)
val surfaceContainerHighestLight = Color(0xFFD1D1D6)

val surfaceContainerLowestDark = Color(0xFF000000)
val surfaceContainerLowDark = Color(0xFF1C1C1E) // Карточки
val surfaceContainerDark = Color(0xFF2C2C2E)
val surfaceContainerHighDark = Color(0xFF3A3A3C)
val surfaceContainerHighestDark = Color(0xFF48484A)

// --- Secondary (Dark) ---
val secondaryDark = Color(0xFF8E8E93) // System Gray (он универсален)
val onSecondaryDark = Color(0xFFFFFFFF)
val secondaryContainerDark = Color(0xFF2C2C2E) // System Gray 5 Dark
val onSecondaryContainerDark = Color(0xFFE5E5EA)

// --- Tertiary (Dark) ---
val tertiaryDark = Color(0xFF5E5CE6) // System Indigo Dark
val onTertiaryDark = Color(0xFFFFFFFF)
val tertiaryContainerDark = Color(0xFF3F3F70)
val onTertiaryContainerDark = Color(0xFFE0E0FF)

// --- Error Containers (Dark) ---
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE2E2E6)
val inverseOnSurfaceDark = Color(0xFF2F3036)
val inversePrimaryDark = Color(0xFF1E73FC)
val surfaceDimDark = Color(0xFF111318)
val surfaceBrightDark = Color(0xFF37393E)


val deleteColor = Color(0xFFEF5350)

// --- Pastel Note Colors (Палитра заметок) ---

// 1. Оранжевый
val NoteOrangeLight = Color(0xFFFFE0B2)
val NoteOrangeDark = Color(0xFF5D4037) // Чуть светлее коричневый для лучшей читаемости

// 2. Голубой
val NoteBlueLight = Color(0xFFD1E4FF)
val NoteBlueDark = Color(0xFF173868)

// 3. Зеленый
val NoteGreenLight = Color(0xFFC8E6C9)
val NoteGreenDark = Color(0xFF1B5E20)

// 4. Розовый
val NotePinkLight = Color(0xFFF8BBD0)
val NotePinkDark = Color(0xFF880E4F)

// 5. Желтый
val NoteYellowLight = Color(0xFFFFF9C4)
val NoteYellowDark = Color(0xFFF57F17) // Темно-желтый ближе к оранжевому

// 6. Фиолетовый
val NotePurpleLight = Color(0xFFE1BEE7)
val NotePurpleDark = Color(0xFF4A148C)

// 7. Нейтральный (Серый) - если понадобится
val NoteGrayLight = Color(0xFFECEFF1)
val NoteGrayDark = Color(0xFF37474F)