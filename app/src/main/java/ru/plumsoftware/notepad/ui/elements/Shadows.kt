package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Синеватая тень в стиле нового дизайна.
 * Использует цвет [MaterialTheme.colorScheme.primary] для spot/ambient,
 * благодаря чему тень получает лёгкий синий оттенок как на макете.
 *
 * Применяется к кнопкам создания заметки, создания папки, кнопке «Все»,
 * кнопке «Готово» и точкам ввода кода-пароля.
 */
@Composable
fun Modifier.blueShadow(
    elevation: androidx.compose.ui.unit.Dp = 10.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    clip: Boolean = false
): Modifier {
    val primary = MaterialTheme.colorScheme.primary
    return this.shadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = primary,
        spotColor = primary
    )
}
