package ru.plumsoftware.notepad.ui.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.theme_saver.ThemeState
import ru.plumsoftware.notepad.data.theme_saver.saveDarkThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    activity: Activity,
    navController: NavController,
    themeState: ThemeState
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier,
                        enabled = false,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.Transparent,
                            containerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        onClick = {}) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.Transparent
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(top = 18.dp, start = 14.dp, end = 14.dp)
        ) {
            // Строка с переключателем темы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.dark_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // iOS-style Switch
                IOSSwitch(
                    checked = themeState.isDarkTheme,
                    onCheckedChange = { checked ->
                        themeState.isDarkTheme = checked
                        saveDarkThemePreference(checked, context)
                        // Применяем тему динамически
                        if (checked) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                    }
                )
            }
        }
    }
}

// Кастомный iOS-style Switch
@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animationDuration = 200
    val thumbSize = 28.dp  // Увеличил тумблер
    val trackHeight = 32.dp // Увеличил высоту трека
    val trackWidth = 52.dp  // Увеличил ширину трека

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - 2.dp else 2.dp,
        animationSpec = tween(durationMillis = animationDuration),
        label = "thumb_animation"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else Color(0xFFE9E9EA),
        animationSpec = tween(durationMillis = animationDuration),
        label = "track_color_animation"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color.White,
        animationSpec = tween(durationMillis = animationDuration),
        label = "thumb_color_animation"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
    ) {
        // Трек (фон)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(50)
                )
        )

        // Тумблер (ползунок)
        Box(
            modifier = Modifier
                .size(thumbSize)
                .offset(x = thumbOffset, y = 2.dp)
                .background(
                    color = thumbColor,
                    shape = CircleShape
                )
        )
    }
}