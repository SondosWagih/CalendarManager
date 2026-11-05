package app.calendarmanager.ui.work

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.WorkEventEntity
import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.data.local.entity.WorkGroupEntity
import app.calendarmanager.ui.study.EmptyText
import app.calendarmanager.ui.study.ItemCard
import app.calendarmanager.ui.study.dayName
import app.calendarmanager.ui.study.formatDate
import app.calendarmanager.ui.study.formatMinute
import java.time.temporal.WeekFields
import java.util.Locale

private sealed interface WorkDialog {
    data class Group(val item: WorkGroupEntity?) : WorkDialog
    data class Event(val item: WorkEventEntity?, val kind: WorkEventKind) : WorkDialog
}

private class WorkDelete(@StringRes val message: Int, val onConfirm: () -> Unit)

@Composable
fun WorkScreen(viewModel: WorkViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dialog by remember { mutableStateOf<WorkDialog?>(null) }
    var pendingDelete by remember { mutableStateOf<WorkDelete?>(null) }

    val group = ui.groups.firstOrNull { it.id == selectedId } ?: ui.groups.firstOrNull()
    val groupEvents = ui.events.filter { it.groupId == group?.id }
    val weekly = groupEvents.filter { it.kind == WorkEventKind.WEEKLY }
    val others = groupEvents.filter { it.kind != WorkEventKind.WEEKLY }
        .sortedWith(compareBy<WorkEventEntity>({ it.dateEpochDay ?: 0L }, { it.startMinute }))
    val tint = group?.let { Color(it.colorArgb) } ?: Color.Gray

    Box(modifier.fillMaxSize().testTag("screen_work")) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ui.groups.forEach { g ->
                    FilterChip(
                        selected = g.id == group?.id,
                        onClick = { selectedId = g.id },
                        label = { Text(g.name) },
                        leadingIcon = { Box(Modifier.size(12.dp).clip(CircleShape).background(Color(g.colorArgb))) },
                    )
                }
                AssistChip(onClick = { dialog = WorkDialog.Group(null) }, label = { Text(stringResource(R.string.add_group)) })
            }

            if (group == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.add_group_first), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
                    TextButton(onClick = { dialog = WorkDialog.Group(group) }) { Text(stringResource(R.string.edit_group)) }
                    TextButton(onClick = { dialog = WorkDialog.Event(null, WorkEventKind.UNEXPECTED) }) {
                        Text(stringResource(R.string.add_unexpected))
                    }
                    TextButton(onClick = {
                        pendingDelete = WorkDelete(R.string.delete_group_msg) { viewModel.deleteGroup(group.id) }
                    }) { Text(stringResource(R.string.delete_group), color = MaterialTheme.colorScheme.error) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "h_weekly") {
                        Text(stringResource(R.string.work_weekly_schedule), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (weekly.isEmpty()) item(key = "e_weekly") { EmptyText() }
                    val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
                    (0..6).map { (firstDay - 1 + it) % 7 + 1 }.forEach { d ->
                        val dayEvents = weekly.filter { it.dayOfWeek == d }.sortedBy { it.startMinute }
                        if (dayEvents.isNotEmpty()) {
                            item(key = "day_$d") { Text(dayName(d), style = MaterialTheme.typography.titleSmall) }
                            items(dayEvents, key = { "w_${it.id}" }) { e ->
                                EventCard(e, tint, showDate = false, onEdit = { dialog = WorkDialog.Event(e, e.kind) },
                                    onDelete = { pendingDelete = WorkDelete(R.string.delete_item_msg) { viewModel.deleteEvent(e.id) } })
                            }
                        }
                    }
                    item(key = "h_other") {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.work_other_events), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (others.isEmpty()) item(key = "e_other") { EmptyText() }
                    items(others, key = { "o_${it.id}" }) { e ->
                        EventCard(e, tint, showDate = true, onEdit = { dialog = WorkDialog.Event(e, e.kind) },
                            onDelete = { pendingDelete = WorkDelete(R.string.delete_item_msg) { viewModel.deleteEvent(e.id) } })
                    }
                }
            }
        }
        if (group != null) {
            FloatingActionButton(
                onClick = { dialog = WorkDialog.Event(null, WorkEventKind.WEEKLY) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add)) }
        }
    }

    when (val d = dialog) {
        null -> Unit
        is WorkDialog.Group -> WorkGroupForm(d.item, { dialog = null }, viewModel::saveGroup)
        is WorkDialog.Event -> WorkEventForm(d.item, ui.groups, group?.id, d.kind, { dialog = null }, viewModel::saveEvent)
    }

    pendingDelete?.let { req ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(req.message)) },
            confirmButton = { TextButton(onClick = { req.onConfirm(); pendingDelete = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun EventCard(e: WorkEventEntity, tint: Color, showDate: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    ItemCard(
        tint = tint,
        title = e.title,
        lines = listOfNotNull(
            (if (showDate) e.dateEpochDay?.let { formatDate(it) + " • " } ?: "" else "") +
                "${formatMinute(e.startMinute)}–${formatMinute(e.endMinute)}",
            if (e.kind == WorkEventKind.WEEKLY) null else stringResource(e.kind.label()),
            e.location,
        ),
        onClick = onEdit,
        onDelete = onDelete,
    )
}
