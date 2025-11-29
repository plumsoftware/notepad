package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.Screen

@Composable
fun BottomBar(
    navController: NavController,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    var selected by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = selected) {
        when (selected) {
            0 -> onHomeClick()
            1 -> onCalendarClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 24.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Полоска сверху
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Основной контент с иконками
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая кнопка - дом
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selected = 0 }
                    )
                    .background(
                        color = if (selected == 0) MaterialTheme.colorScheme.surfaceContainer
                        else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.house_fill),
                    contentDescription = stringResource(R.string.menu),
                    tint = if (selected == 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            // Центральная кнопка - плюс
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(
                        enabled = true,
                        role = Role.Button
                    ) {
                        navController.navigate(Screen.AddNote.route)
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.plus),
                    contentDescription = stringResource(R.string.add),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Правая кнопка - календарь
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selected = 1 }
                    )
                    .background(
                        color = if (selected == 1) MaterialTheme.colorScheme.surfaceContainer
                        else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.calendar2_week_fill),
                    contentDescription = stringResource(R.string.daily_planner),
                    tint = if (selected == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Preview функция
@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    // Создаем mock NavController для Preview
    val mockNavController = rememberNavController()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            BottomBar(
                navController = mockNavController,
                onHomeClick = { println("Home clicked") },
                onCalendarClick = { println("Calendar clicked") }
            )
        }
    }
}
