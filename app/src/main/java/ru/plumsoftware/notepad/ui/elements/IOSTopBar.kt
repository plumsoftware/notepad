package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.R

@Composable
fun IOSTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onFilterClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    listType: Int, // 0 - List, 1 - Grid
    modifier: Modifier = Modifier
) {
    // Анимация цвета фона поля поиска
    val searchBarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    // Focus Requester для управления клавиатурой
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. ЛЕВАЯ КНОПКА (МЕНЮ / НАСТРОЙКИ)
        AnimatedVisibility(
            visible = !isSearchFocused,
            enter = fadeIn() + slideInHorizontally { -it },
            exit = fadeOut() + slideOutHorizontally { -it }
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    // "Меню"
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.primary, // Синий цвет как в iOS
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSettingsClick)
                )
            }
        }

        if (!isSearchFocused) Spacer(modifier = Modifier.width(8.dp))

        // 2. ПОЛЕ ПОИСКА (РАСТЯГИВАЕТСЯ)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp) // Стандартная высота в iOS
                .clip(RoundedCornerShape(10.dp))
                .background(searchBarColor)
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        // Лупа
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    // "Поиск заметок"
                                    text = stringResource(R.string.note_search),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }

                        // Кнопка очистки (крестик внутри поля)
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                // "Очистить"
                                contentDescription = stringResource(R.string.clear),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onSearchQueryChange("") }
                            )
                        }
                    }
                }
            )
        }

        // 3. ПРАВЫЕ КНОПКИ (ФИЛЬТР И ЛЭИАУТ) - ИСЧЕЗАЮТ ПРИ ПОИСКЕ
        AnimatedVisibility(
            visible = !isSearchFocused,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                // Фильтр
                Box(
                    modifier = Modifier
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.filter),
                        // "Сортировка" (используем заголовок диалога как описание)
                        contentDescription = stringResource(R.string.filter_dialog_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onFilterClick,
                                enabled = true,
                                role = Role.Button,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Переключение списка (Grid/List)
                Box(
                    modifier = Modifier
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Рисуем иконку программно или используем ресурс
                    if (listType == 0) { // Сейчас СПИСОК -> Показать иконку СЕТКИ
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onLayoutToggle),
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Grid",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else { // Сейчас СЕТКА -> Показать иконку СПИСКА
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onLayoutToggle),
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "List",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 4. КНОПКА "ОТМЕНА" (ПОЯВЛЯЕТСЯ ПРИ ПОИСКЕ)
        AnimatedVisibility(
            visible = isSearchFocused,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            Text(
                // "Отмена"
                text = stringResource(R.string.cancel),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onSearchQueryChange("")
                        onFocusChange(false)
                        focusManager.clearFocus() // Скрыть клавиатуру
                    }
                    .padding(4.dp)
            )
        }
    }
}