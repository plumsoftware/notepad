package ru.plumsoftware.notepad.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.ui.Screen

@Composable
fun GroupList(groups: List<Group>) {
    FlowColumn(
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Start),
        maxLines = 2,
        maxItemsInEachColumn = 1,
        itemHorizontalAlignment = Alignment.Start
    ) {
        GroupItem(
            isAdd = true,
            onClick = {

            },
            group = null
        )

        groups.forEach { group ->
            GroupItem(
                onClick = {

                },
                group = group
            )
        }
    }
}

@Composable
fun GroupItem(isAdd: Boolean = false, onClick: () -> Unit, group: Group?) {
    Button(
        modifier = Modifier.width(width = 44.dp),
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAdd) MaterialTheme.colorScheme.primary else Color(
                (group?.color ?: 0L).toULong()
            ),
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(4.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                tint = LocalContentColor.current
            )
        }
    }
}