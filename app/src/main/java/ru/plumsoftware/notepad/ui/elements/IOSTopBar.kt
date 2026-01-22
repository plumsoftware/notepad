package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
    onSettingsClick: () -> Unit, // Если вдруг решишь вернуть настройки слева
    onFilterClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    listType: Int,
    modifier: Modifier = Modifier
) {
    val searchBarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 1. ЛЕВЫЙ ОТСТУП (Spacer)
        // Мы оборачиваем его в AnimatedVisibility, чтобы он исчезал ПЛАВНО (сжимаясь),
        // а не исчезал рывком.
        AnimatedVisibility(
            visible = !isSearchFocused,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Spacer(modifier = Modifier.width(0.dp)) // Если нужен отступ, поставь 8.dp, но сейчас у нас padding у Row
        }

        // 2. ПОЛЕ ПОИСКА
        // Оно имеет weight(1f), поэтому оно будет плавно занимать место,
        // которое освобождают соседние элементы благодаря их анимации shrinkHorizontally.
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp) // Чуть компактнее, как в iOS
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
                                    text = stringResource(R.string.note_search),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
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

        // 3. ПРАВЫЕ КНОПКИ (Фильтр и Вид)
        // Используем shrinkHorizontally, чтобы они "схлопывались", освобождая место для поиска
        AnimatedVisibility(
            visible = !isSearchFocused,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp) // Отступ от поиска
            ) {
                // Фильтр
                Box(
                    modifier = Modifier
                        .size(36.dp) // Увеличиваем область клика
                        .clip(CircleShape)
                        .clickable(onClick = onFilterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.filter),
                        contentDescription = stringResource(R.string.filter_dialog_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Переключение вида
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onLayoutToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (listType == 0) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
                        contentDescription = "View Toggle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 4. КНОПКА "ОТМЕНА"
        // Используем expandHorizontally, чтобы она "расталкивала" место для себя
        AnimatedVisibility(
            visible = isSearchFocused,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Убираем ripple эффект как в iOS
                    ) {
                        onSearchQueryChange("")
                        onFocusChange(false)
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp) // Увеличиваем высоту клика
                )
            }
        }
    }
}