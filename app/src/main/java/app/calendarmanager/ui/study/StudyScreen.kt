package app.calendarmanager.ui.study

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.*
import java.time.temporal.WeekFields
import java.util.Locale

private enum class StudyTab(@StringRes val label: Int) {
    SCHEDULE(R.string.tab_schedule), COURSES(R.string.tab_courses), EXAMS(R.string.tab_exams),
    ASSIGNMENTS(R.string.tab_assignments), PLANNER(R.string.tab_planner),
}

private sealed interface StudyDialog {
    data class Course(val item: CourseEntity?) : StudyDialog
    data class Event(val item: StudyEventEntity?) : StudyDialog
    data class Exam(val item: ExamEntity?) : StudyDialog
    data class Assignment(val item: AssignmentEntity?) : StudyDialog
    data class Session(val item: StudySessionEntity?) : StudyDialog
}

private class DeleteRequest(@StringRes val message: Int, val onConfirm: () -> Unit)

@Composable
fun StudyScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    var dialog by remember { mutableStateOf<StudyDialog?>(null) }
    var pendingDelete by remember { mutableStateOf<DeleteRequest?>(null) }

    val tab = StudyTab.entries[tabIndex]
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }
    val needsCourse = (tab == StudyTab.SCHEDULE || tab == StudyTab.EXAMS || tab == StudyTab.ASSIGNMENTS) && ui.courses.isEmpty()
    val today = todayEpochDay()

    fun tintOf(courseId: Long?): Color = courseById[courseId]?.let { Color(it.colorArgb) } ?: Color.Gray
    fun nameOf(courseId: Long?): String = courseById[courseId]?.name ?: "—"

    Box(modifier.fillMaxSize().testTag("screen_study")) {
        Column(Modifier.fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 8.dp) {
                StudyTab.entries.forEachIndexed { i, t ->
                    Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(stringResource(t.label)) })
                }
            }
            if (needsCourse) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.add_course_first), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (tab) {
                        StudyTab.SCHEDULE -> {
                            val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
                            val order = (0..6).map { (firstDay - 1 + it) % 7 + 1 }
                            if (ui.events.isEmpty()) item { EmptyText() }
                            order.forEach { d ->
                                val dayEvents = ui.events.filter { it.dayOfWeek == d }
                                if (dayEvents.isNotEmpty()) {
                                    item(key = "day_$d") {
                                        Text(dayName(d), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    items(dayEvents, key = { "event_${it.id}" }) { e ->
                                        ItemCard(
                                            tint = tintOf(e.courseId),
                                            title = nameOf(e.courseId),
                                            lines = listOfNotNull(
                                                "${formatMinute(e.startMinute)}–${formatMinute(e.endMinute)}",
                                                "${stringResource(e.type.label())} • ${stringResource(e.attendance.label())}",
                                                e.location,
                                                e.onlineLink,
                                            ),
                                            onClick = { dialog = StudyDialog.Event(e) },
                                            onDelete = { pendingDelete = DeleteRequest(R.string.delete_item_msg) { viewModel.deleteEvent(e.id) } },
                                        )
                                    }
                                }
                            }
                        }
                        StudyTab.COURSES -> {
                            if (ui.courses.isEmpty()) item { EmptyText() }
                            items(ui.courses, key = { "course_${it.id}" }) { c ->
                                ItemCard(
                                    tint = Color(c.colorArgb),
                                    title = c.name,
                                    lines = listOfNotNull(c.code, c.instructor),
                                    onClick = { dialog = StudyDialog.Course(c) },
                                    onDelete = { pendingDelete = DeleteRequest(R.string.delete_course_msg) { viewModel.deleteCourse(c.id) } },
                                )
                            }
                        }
                        StudyTab.EXAMS -> {
                            if (ui.exams.isEmpty()) item { EmptyText() }
                            items(ui.exams, key = { "exam_${it.id}" }) { x ->
                                ItemCard(
                                    tint = LocalExamColor(),
                                    title = x.title,
                                    lines = listOfNotNull(
                                        "${nameOf(x.courseId)} • ${stringResource(x.kind.label())}",
                                        "${formatDate(x.dateEpochDay)} • ${formatMinute(x.startMinute)} (${stringResource(R.string.duration_min, x.durationMinutes)})",
                                        x.location,
                                    ),
                                    onClick = { dialog = StudyDialog.Exam(x) },
                                    onDelete = { pendingDelete = DeleteRequest(R.string.delete_item_msg) { viewModel.deleteExam(x.id) } },
                                )
                            }
                        }
                        StudyTab.ASSIGNMENTS -> {
                            if (ui.assignments.isEmpty()) item { EmptyText() }
                            items(ui.assignments, key = { "assignment_${it.id}" }) { a ->
                                val overdue = !a.completed && a.dueEpochDay < today
                                ItemCard(
                                    tint = tintOf(a.courseId),
                                    title = a.title,
                                    lines = listOfNotNull(
                                        "${nameOf(a.courseId)} • ${stringResource(a.priority.label())}",
                                        formatDate(a.dueEpochDay) + (a.dueMinute?.let { " • ${formatMinute(it)}" } ?: ""),
                                        if (overdue) stringResource(R.string.overdue) else null,
                                    ),
                                    onClick = { dialog = StudyDialog.Assignment(a) },
                                    onDelete = { pendingDelete = DeleteRequest(R.string.delete_item_msg) { viewModel.deleteAssignment(a.id) } },
                                    leading = { Checkbox(checked = a.completed, onCheckedChange = { viewModel.setAssignmentCompleted(a.id, it) }) },
                                )
                            }
                        }
                        StudyTab.PLANNER -> {
                            if (ui.sessions.isEmpty()) item { EmptyText() }
                            items(ui.sessions, key = { "session_${it.id}" }) { s ->
                                ItemCard(
                                    tint = tintOf(s.courseId),
                                    title = s.title,
                                    lines = listOfNotNull(
                                        s.courseId?.let { nameOf(it) },
                                        "${formatDate(s.dateEpochDay)} • ${formatMinute(s.startMinute)} (${stringResource(R.string.duration_min, s.durationMinutes)})",
                                    ),
                                    onClick = { dialog = StudyDialog.Session(s) },
                                    onDelete = { pendingDelete = DeleteRequest(R.string.delete_item_msg) { viewModel.deleteSession(s.id) } },
                                    leading = { Checkbox(checked = s.completed, onCheckedChange = { viewModel.setSessionCompleted(s.id, it) }) },
                                )
                            }
                        }
                    }
                }
            }
        }
        if (!needsCourse) {
            FloatingActionButton(
                onClick = {
                    dialog = when (tab) {
                        StudyTab.SCHEDULE -> StudyDialog.Event(null)
                        StudyTab.COURSES -> StudyDialog.Course(null)
                        StudyTab.EXAMS -> StudyDialog.Exam(null)
                        StudyTab.ASSIGNMENTS -> StudyDialog.Assignment(null)
                        StudyTab.PLANNER -> StudyDialog.Session(null)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add)) }
        }
    }

    when (val d = dialog) {
        null -> Unit
        is StudyDialog.Course -> CourseForm(d.item, { dialog = null }, viewModel::saveCourse)
        is StudyDialog.Event -> EventForm(d.item, ui.courses, { dialog = null }, viewModel::saveEvent)
        is StudyDialog.Exam -> ExamForm(d.item, ui.courses, { dialog = null }, viewModel::saveExam)
        is StudyDialog.Assignment -> AssignmentForm(d.item, ui.courses, { dialog = null }, viewModel::saveAssignment)
        is StudyDialog.Session -> SessionForm(d.item, ui.courses, { dialog = null }, viewModel::saveSession)
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
private fun LocalExamColor(): Color = app.calendarmanager.ui.theme.LocalCategoryColors.current.exam

@Composable
internal fun EmptyText() {
    Text(stringResource(R.string.empty_state), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 24.dp))
}

@Composable
internal fun ItemCard(
    tint: Color,
    title: String,
    lines: List<String>,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(tint))
            leading?.invoke()
            Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                lines.forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}
