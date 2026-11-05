package app.calendarmanager

import app.calendarmanager.data.local.entity.AttendanceFormat
import app.calendarmanager.data.local.entity.StudyEventEntity
import app.calendarmanager.data.local.entity.StudyEventType
import app.calendarmanager.ui.study.CoursePalette
import app.calendarmanager.ui.study.layoutDayEvents
import app.calendarmanager.ui.study.nextFreeColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleLayoutTest {
    private fun ev(id: Long, start: Int, end: Int) = StudyEventEntity(
        id = id, courseId = 1, type = StudyEventType.LECTURE, attendance = AttendanceFormat.PHYSICAL,
        dayOfWeek = 1, startMinute = start, endMinute = end, createdAt = 0, updatedAt = 0,
    )

    @Test fun nonOverlappingEventsUseTheFullWidth() {
        val placed = layoutDayEvents(listOf(ev(1, 540, 600), ev(2, 600, 660)))
        assertTrue(placed.all { it.columns == 1 && it.column == 0 })
    }

    @Test fun overlappingEventsSplitIntoColumns() {
        val placed = layoutDayEvents(listOf(ev(1, 540, 660), ev(2, 600, 700)))
        assertEquals(listOf(0, 1), placed.map { it.column })
        assertTrue(placed.all { it.columns == 2 })
    }

    @Test fun laterEventReusesAFreeColumnAndNewClusterResets() {
        val placed = layoutDayEvents(listOf(ev(1, 540, 600), ev(2, 560, 640), ev(3, 600, 660), ev(4, 800, 860)))
        val byId = placed.associateBy { it.event.id }
        assertEquals(0, byId.getValue(3).column) // takes the column freed by event 1
        assertEquals(2, byId.getValue(3).columns)
        assertEquals(1, byId.getValue(4).columns) // separate cluster -> full width again
    }

    @Test fun paletteIsLargeUniqueAndNextFreeColorSkipsUsed() {
        assertTrue(CoursePalette.size >= 24)
        assertEquals(CoursePalette.size, CoursePalette.toSet().size)
        assertEquals(CoursePalette[1], nextFreeColor(setOf(CoursePalette[0])))
        assertEquals(CoursePalette[0], nextFreeColor(CoursePalette.toSet()))
    }
}
