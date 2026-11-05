package app.calendarmanager.domain.validation

import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft

object WorkValidator {
    private fun minuteOk(m: Int) = m in 0..1439

    fun group(d: WorkGroupDraft): ValidationError? =
        if (d.name.isBlank()) ValidationError.NAME_REQUIRED else null

    fun event(d: WorkEventDraft): ValidationError? {
        if (d.groupId == null) return ValidationError.GROUP_REQUIRED
        if (d.title.isBlank()) return ValidationError.TITLE_REQUIRED
        if (d.kind == WorkEventKind.WEEKLY) {
            val day = d.dayOfWeek
            if (day == null || day !in 1..7) return ValidationError.DAY_INVALID
        } else if (d.dateEpochDay == null) {
            return ValidationError.DATE_REQUIRED
        }
        if (!minuteOk(d.startMinute) || !minuteOk(d.endMinute)) return ValidationError.TIME_INVALID
        if (d.endMinute <= d.startMinute) return ValidationError.END_BEFORE_START
        return null
    }
}
