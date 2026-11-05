package app.calendarmanager.ui.work

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.WorkEventEntity
import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.data.local.entity.WorkGroupEntity
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft
import app.calendarmanager.domain.validation.ValidationError
import app.calendarmanager.ui.study.*
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun WorkGroupForm(initial: WorkGroupEntity?, onDismiss: () -> Unit, onSave: SaveHandler<WorkGroupDraft>) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableIntStateOf(initial?.colorArgb ?: CoursePalette[1]) }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_group), error, onDismiss, onSave = {
        onSave(WorkGroupDraft(initial?.id, name, color)) { e -> if (e == null) onDismiss() else error = e }
    }) {
        TextInput(stringResource(R.string.field_name), name) { name = it }
        ColorRow(stringResource(R.string.field_color), color) { color = it }
    }
}

@Composable
fun WorkEventForm(
    initial: WorkEventEntity?,
    groups: List<WorkGroupEntity>,
    defaultGroupId: Long?,
    defaultKind: WorkEventKind,
    onDismiss: () -> Unit,
    onSave: SaveHandler<WorkEventDraft>,
) {
    val nowMinute = LocalTime.now().let { it.hour * 60 + it.minute }
    val defStart = if (initial == null && defaultKind == WorkEventKind.UNEXPECTED) nowMinute.coerceAtMost(1438) else 540
    val defEnd = (defStart + 60).coerceAtMost(1439)

    var groupId by remember { mutableStateOf(initial?.groupId ?: defaultGroupId) }
    var kind by remember { mutableStateOf(initial?.kind ?: defaultKind) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var day by remember { mutableIntStateOf(initial?.dayOfWeek ?: LocalDate.now().dayOfWeek.value) }
    var date by remember { mutableLongStateOf(initial?.dateEpochDay ?: todayEpochDay()) }
    var start by remember { mutableIntStateOf(initial?.startMinute ?: defStart) }
    var end by remember { mutableIntStateOf(initial?.endMinute ?: defEnd) }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var error by remember { mutableStateOf<ValidationError?>(null) }

    FormDialog(stringResource(R.string.form_work_event), error, onDismiss, onSave = {
        val weekly = kind == WorkEventKind.WEEKLY
        onSave(
            WorkEventDraft(initial?.id, groupId, title, kind, if (weekly) day else null,
                if (weekly) null else date, start, end, location, notes)
        ) { e -> if (e == null) onDismiss() else error = e }
    }) {
        val ids: List<Long?> = groups.map { it.id }
        ChoiceRow(stringResource(R.string.field_group), ids, groupId, { groupId = it }) { id ->
            groups.firstOrNull { it.id == id }?.name ?: "—"
        }
        ChoiceRow(stringResource(R.string.field_kind), WorkEventKind.entries.toList(), kind, { kind = it }) { stringResource(it.label()) }
        TextInput(stringResource(R.string.field_title), title) { title = it }
        if (kind == WorkEventKind.WEEKLY) {
            ChoiceRow(stringResource(R.string.field_day), (1..7).toList(), day, { day = it }) { dayName(it) }
        } else {
            DateField(stringResource(R.string.field_date), date) { date = it }
        }
        TimeField(stringResource(R.string.field_start), start, { start = it })
        TimeField(stringResource(R.string.field_end), end, { end = it })
        TextInput(stringResource(R.string.field_location), location) { location = it }
        TextInput(stringResource(R.string.field_notes), notes, singleLine = false) { notes = it }
    }
}
