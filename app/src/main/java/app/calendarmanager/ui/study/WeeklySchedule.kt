package app.calendarmanager.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.CourseEntity
import app.calendarmanager.data.local.entity.StudyEventEntity
import java.time.LocalDate
import java.time.LocalTime

private val GutterWidth = 48.dp
private val HourHeight = 64.dp
private val HeaderHeight = 48.dp
private val MinDayWidth = 96.dp
private val MinBlockHeight = 28.dp

/** An event plus its slot inside a cluster of overlapping events (column index / number of columns). */
internal data class PlacedEvent(val event: StudyEventEntity, val column: Int, val columns: Int)

/**
 * Lays out one day's events side by side when they overlap. Events are grouped into clusters
 * (a cluster ends when the next event starts at/after the latest end seen so far); inside a cluster
 * each event takes the first free column and every event of the cluster shares the same column count.
 */
internal fun layoutDayEvents(events: List<StudyEventEntity>): List<PlacedEvent> {
    val sorted = events.sortedWith(compareBy({ it.startMinute }, { it.endMinute }, { it.id }))
    val result = mutableListOf<PlacedEvent>()
    var cluster = mutableListOf<Pair<StudyEventEntity, Int>>()
    var columnEnds = mutableListOf<Int>()
    var clusterEnd = -1

    fun flush() {
        val n = columnEnds.size
        cluster.forEach { (e, c) -> result += PlacedEvent(e, c, n) }
        cluster = mutableListOf()
        columnEnds = mutableListOf()
        clusterEnd = -1
    }

    for (e in sorted) {
        if (cluster.isNotEmpty() && e.startMinute >= clusterEnd) flush()
        var c = columnEnds.indexOfFirst { it <= e.startMinute }
        if (c == -1) {
            c = columnEnds.size
            columnEnds.add(e.endMinute)
        } else {
            columnEnds[c] = e.endMinute
        }
        cluster.add(e to c)
        clusterEnd = maxOf(clusterEnd, e.endMinute)
    }
    flush()
    return result
}

/**
 * Weekly timetable: sticky day header + sticky time gutter, events drawn as colored blocks positioned
 * by start/end time. Scrolls both ways (the header and the grid share one horizontal scroll state).
 * On wide screens the seven days fill the width; on phones the grid opens scrolled to today.
 */
@Composable
fun WeeklySchedule(
    events: List<StudyEventEntity>,
    courseById: Map<Long, CourseEntity>,
    firstDay: Int,
    onEventClick: (StudyEventEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember(firstDay) { (0..6).map { (firstDay - 1 + it) % 7 + 1 } }
    val todayDow = remember { LocalDate.now().dayOfWeek.value }
    val nowMinute = remember { LocalTime.now().let { it.hour * 60 + it.minute } }

    val startHour = remember(events) {
        ((events.minOfOrNull { it.startMinute } ?: 480) / 60).coerceIn(0, 8)
    }
    val endHour = remember(events) {
        maxOf(17, ((events.maxOfOrNull { it.endMinute } ?: 0) + 59) / 60).coerceAtMost(24)
    }
    val hours = endHour - startHour
    val gridStart = startHour * 60
    val byDay = remember(events, days) { days.associateWith { d -> layoutDayEvents(events.filter { it.dayOfWeek == d }) } }

    BoxWithConstraints(modifier) {
        val dayWidth = maxOf(MinDayWidth, (maxWidth - GutterWidth) / 7)
        val hScroll = rememberScrollState()
        val vScroll = rememberScrollState()
        val density = LocalDensity.current

        LaunchedEffect(dayWidth, startHour) {
            val idx = days.indexOf(todayDow).coerceAtLeast(0)
            hScroll.scrollTo(with(density) { (dayWidth * idx).roundToPx() })
            val y = ((nowMinute - gridStart - 60).coerceAtLeast(0) / 60f) * with(density) { HourHeight.toPx() }
            vScroll.scrollTo(y.toInt())
        }

        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(HeaderHeight)) {
                Spacer(Modifier.width(GutterWidth))
                Row(Modifier.weight(1f).horizontalScroll(hScroll)) {
                    days.forEach { d -> DayHeader(d, d == todayDow, Modifier.width(dayWidth)) }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

            Column(Modifier.weight(1f).verticalScroll(vScroll)) {
                Row {
                    Column(Modifier.width(GutterWidth).height(HourHeight * hours)) {
                        for (h in startHour until endHour) {
                            Box(Modifier.fillMaxWidth().height(HourHeight), contentAlignment = Alignment.TopCenter) {
                                Text(
                                    formatMinute(h * 60),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                    Row(Modifier.weight(1f).horizontalScroll(hScroll)) {
                        days.forEach { d ->
                            DayColumn(
                                placed = byDay[d].orEmpty(),
                                hours = hours,
                                gridStart = gridStart,
                                width = dayWidth,
                                isToday = d == todayDow,
                                nowMinute = if (d == todayDow) nowMinute else null,
                                courseById = courseById,
                                onEventClick = onEventClick,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(96.dp)) // keeps the last hours clear of the floating add button
            }
        }

        if (events.isEmpty()) {
            Text(
                stringResource(R.string.empty_state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun DayHeader(day: Int, isToday: Boolean, modifier: Modifier) {
    Box(modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        Text(
            dayName(day),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DayColumn(
    placed: List<PlacedEvent>,
    hours: Int,
    gridStart: Int,
    width: Dp,
    isToday: Boolean,
    nowMinute: Int?,
    courseById: Map<Long, CourseEntity>,
    onEventClick: (StudyEventEntity) -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val nowColor = MaterialTheme.colorScheme.error
    val todayTint = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else Color.Transparent

    Box(
        Modifier
            .width(width)
            .height(HourHeight * hours)
            .background(todayTint)
            .drawBehind {
                val hp = HourHeight.toPx()
                val stroke = 1.dp.toPx()
                for (i in 0..hours) {
                    drawLine(lineColor, Offset(0f, i * hp), Offset(size.width, i * hp), stroke)
                }
                drawLine(lineColor, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
                if (nowMinute != null && nowMinute >= gridStart && nowMinute <= gridStart + hours * 60) {
                    val y = (nowMinute - gridStart) / 60f * hp
                    drawLine(nowColor, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
                    drawCircle(nowColor, radius = 4.dp.toPx(), center = Offset(4.dp.toPx(), y))
                }
            },
    ) {
        val columnGap = 2.dp
        placed.forEach { p ->
            val e = p.event
            val top = HourHeight * ((e.startMinute - gridStart) / 60f)
            val blockHeight = (HourHeight * ((e.endMinute - e.startMinute) / 60f)).coerceAtLeast(MinBlockHeight)
            val slotWidth = (width - columnGap * 2) / p.columns
            EventBlock(
                event = e,
                course = courseById[e.courseId],
                blockHeight = blockHeight - 2.dp,
                onClick = { onEventClick(e) },
                modifier = Modifier
                    .offset(x = columnGap + slotWidth * p.column, y = top + 1.dp)
                    .width(slotWidth - 2.dp)
                    .height(blockHeight - 2.dp),
            )
        }
    }
}

@Composable
private fun EventBlock(
    event: StudyEventEntity,
    course: CourseEntity?,
    blockHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val accent = course?.let { Color(it.colorArgb) } ?: Color.Gray
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.22f).compositeOver(surface))
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(accent))
        Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(
                course?.name ?: "—",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                maxLines = if (blockHeight >= 56.dp) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (blockHeight >= 44.dp) {
                Text(
                    "${formatMinute(event.startMinute)}–${formatMinute(event.endMinute)}",
                    fontSize = 10.sp, color = muted, maxLines = 1,
                )
            }
            if (blockHeight >= 64.dp) {
                Text(
                    "${stringResource(event.type.label())} • ${stringResource(event.attendance.label())}",
                    fontSize = 10.sp, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            if (blockHeight >= 82.dp && !event.location.isNullOrBlank()) {
                Text(event.location, fontSize = 10.sp, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
