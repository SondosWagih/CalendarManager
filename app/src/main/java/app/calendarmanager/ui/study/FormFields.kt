package app.calendarmanager.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.CourseEntity
import app.calendarmanager.domain.validation.ValidationError

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * 24 course colors, ordered around the color wheel (reds -> oranges -> yellows -> greens -> teals -> blues
 * -> purples -> pinks -> neutrals) so the grid reads as a gradient. The first-generation 8 colors are kept
 * unchanged, so courses saved earlier still show their selection ring. Anything outside this list is a
 * custom color (see ColorRow).
 */
val CoursePalette: List<Int> = listOf(
    0xFFC62828, 0xFFD0453D, 0xFFFF7043, 0xFFF4511E, 0xFFE08A1E, 0xFFF9A825,
    0xFFAFB42B, 0xFF7CB342, 0xFF2E9E6B, 0xFF2E7D32, 0xFF00897B, 0xFF00695C,
    0xFF00ACC1, 0xFF039BE5, 0xFF3F6FD8, 0xFF3949AB, 0xFF5E35B1, 0xFF8A55C7,
    0xFFAB47BC, 0xFF6A1B9A, 0xFFC2185B, 0xFFEC407A, 0xFF8D6E63, 0xFF546E7A,
).map { it.toInt() }

/** First palette color not used by another course; falls back to the first color when all are taken. */
fun nextFreeColor(used: Set<Int>): Int = CoursePalette.firstOrNull { it !in used } ?: CoursePalette.first()

@Composable
fun FormDialog(
    title: String,
    error: ValidationError?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
                if (error != null) Text(stringResource(error.message()), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun TextInput(
    label: String,
    value: String,
    singleLine: Boolean = true,
    numeric: Boolean = false,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun <T> ChoiceRow(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(optionLabel(o)) })
            }
        }
    }
}

/** Course picker. allowNone adds a "None" option (used by study sessions). */
@Composable
fun CourseChoice(courses: List<CourseEntity>, selected: Long?, allowNone: Boolean, onSelect: (Long?) -> Unit) {
    val ids: List<Long?> = courses.map { it.id }
    val options: List<Long?> = if (allowNone) listOf<Long?>(null) + ids else ids
    ChoiceRow(stringResource(R.string.form_course), options, selected, onSelect) { id ->
        if (id == null) stringResource(R.string.none) else courses.firstOrNull { it.id == id }?.name ?: "—"
    }
}

@Composable
private fun ColorDot(color: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(5.dp)
    ) {
        Box(
            Modifier.fillMaxSize().clip(CircleShape).background(Color(color))
                .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
        )
    }
}

/** Palette grid (all colors visible, no sideways scrolling) + an optional custom hue/lightness picker. */
@Composable
fun ColorRow(label: String, selected: Int, onSelect: (Int) -> Unit) {
    var showCustom by remember { mutableStateOf(selected !in CoursePalette) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        CoursePalette.chunked(6).forEach { rowColors ->
            Row {
                rowColors.forEach { c -> ColorDot(c, c == selected) { onSelect(c) } }
            }
        }
        TextButton(onClick = { showCustom = !showCustom }) { Text(stringResource(R.string.color_custom)) }
        if (showCustom) {
            val initialHue = remember {
                FloatArray(3).also { android.graphics.Color.colorToHSV(selected, it) }[0]
            }
            var hue by remember { mutableFloatStateOf(initialHue) }
            var light by remember { mutableFloatStateOf(0.45f) }
            fun pick() = onSelect(Color.hsl(hue, 0.65f, light).toArgb())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Color(selected)))
                Column(Modifier.weight(1f)) {
                    Slider(value = hue, onValueChange = { hue = it; pick() }, valueRange = 0f..360f)
                    Slider(value = light, onValueChange = { light = it; pick() }, valueRange = 0.25f..0.75f)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, minute: Int?, onPick: (Int) -> Unit, onClear: (() -> Unit)? = null) {
    var show by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { show = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
            Text("$label: ${minute?.let { formatMinute(it) } ?: "—"}")
        }
        if (onClear != null && minute != null) {
            TextButton(onClick = onClear) { Text(stringResource(R.string.clear)) }
        }
    }
    if (show) {
        val initial = minute ?: 540
        val state = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = { onPick(state.hour * 60 + state.minute); show = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text(stringResource(R.string.cancel)) } },
            text = { TimePicker(state = state) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, epochDay: Long, onPick: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text("$label: ${formatDate(epochDay)}")
    }
    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = epochDay * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onPick(Math.floorDiv(it, MILLIS_PER_DAY)) }
                    show = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = state) }
    }
}
