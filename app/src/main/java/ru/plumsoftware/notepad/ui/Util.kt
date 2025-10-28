package ru.plumsoftware.notepad.ui

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.edit

// Горизонтальные анимации (как в стандартных навигациях)
@OptIn(ExperimentalAnimationApi::class)
fun horizontalSlideInEnter() = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300)
)

@OptIn(ExperimentalAnimationApi::class)
fun horizontalSlideInExit() = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300)
)

@OptIn(ExperimentalAnimationApi::class)
fun horizontalSlideOutEnter() = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300)
)

@OptIn(ExperimentalAnimationApi::class)
fun horizontalSlideOutExit() = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300)
)

// Вертикальные анимации
@OptIn(ExperimentalAnimationApi::class)
fun verticalSlideInEnter() = slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(300)
)

@OptIn(ExperimentalAnimationApi::class)
fun verticalSlideInExit() = slideOutVertically(
    targetOffsetY = { fullHeight -> -fullHeight },
    animationSpec = tween(300)
)

// Fade анимации
fun fadeInEnter() = fadeIn(
    animationSpec = tween(300)
)

fun fadeOutExit() = fadeOut(
    animationSpec = tween(300)
)

// Комбинированные анимации
@OptIn(ExperimentalAnimationApi::class)
fun slideInWithFade() = slideInHorizontally(
    initialOffsetX = { it / 2 },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

@OptIn(ExperimentalAnimationApi::class)
fun slideOutWithFade() = slideOutHorizontally(
    targetOffsetX = { -it / 2 },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

// Вспомогательные функции (можно вынести в отдельный Utils.kt)
fun Context.getNeedToShowRateDialogFromPreferences(): Boolean {
    return this.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getBoolean("need_to_show_rate_dialog", true)
}

fun Context.saveNeedToShowRateDialogToPreferences(value: Boolean) {
    this.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .edit {
            putBoolean("need_to_show_rate_dialog", value)
        }
}