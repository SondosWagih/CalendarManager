package app.calendarmanager

import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft
import app.calendarmanager.domain.validation.ValidationError.*
import app.calendarmanager.domain.validation.WorkValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkValidatorTest {
    private fun ev(
        group: Long? = 1, title: String = "Shift", kind: WorkEventKind = WorkEventKind.WEEKLY,
        day: Int? = 1, date: Long? = null, start: Int = 540, end: Int = 600,
    ) = WorkEventDraft(groupId = group, title = title, kind = kind, dayOfWeek = day, dateEpochDay = date, startMinute = start, endMinute = end)

    @Test fun groupNeedsName() {
        assertEquals(NAME_REQUIRED, WorkValidator.group(WorkGroupDraft(name = "", colorArgb = 0)))
        assertNull(WorkValidator.group(WorkGroupDraft(name = "Cafe", colorArgb = 0)))
    }

    @Test fun weeklyNeedsADayAndOthersNeedADate() {
        assertNull(WorkValidator.event(ev()))
        assertEquals(DAY_INVALID, WorkValidator.event(ev(day = null)))
        assertEquals(DAY_INVALID, WorkValidator.event(ev(day = 9)))
        assertEquals(DATE_REQUIRED, WorkValidator.event(ev(kind = WorkEventKind.ONE_TIME, day = null)))
        assertNull(WorkValidator.event(ev(kind = WorkEventKind.UNEXPECTED, day = null, date = 20000)))
    }

    @Test fun commonRules() {
        assertEquals(GROUP_REQUIRED, WorkValidator.event(ev(group = null)))
        assertEquals(TITLE_REQUIRED, WorkValidator.event(ev(title = " ")))
        assertEquals(TIME_INVALID, WorkValidator.event(ev(end = 1440)))
        assertEquals(END_BEFORE_START, WorkValidator.event(ev(start = 600, end = 600)))
    }
}
