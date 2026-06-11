package ru.plumsoftware.notepad.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.ui.theme.Dimens

/** Ширина как у карточек заметок в списке (с учётом contentPadding списка). */
fun Modifier.nativeAdListWidth(): Modifier = fillMaxWidth()

/** На всю ширину сетки: компенсирует horizontal contentPadding у LazyVerticalStaggeredGrid. */
fun Modifier.nativeAdGridWidth(): Modifier = fillMaxWidth()
    .padding(horizontal = -Dimens.screenPaddingHorizontal)
