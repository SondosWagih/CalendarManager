package app.calendarmanager.domain.model

import app.calendarmanager.data.local.entity.WorkEventKind

data class WorkGroupDraft(val id: Long? = null, val name: String, val colorArgb: Int)

/** WEEKLY uses dayOfWeek; ONE_TIME and UNEXPECTED use dateEpochDay. */
data class WorkEventDraft(
    val id: Long? = null, val groupId: Long?, val title: String, val kind: WorkEventKind,
    val dayOfWeek: Int? = null, val dateEpochDay: Long? = null,
    val startMinute: Int, val endMinute: Int,
    val location: String? = null, val notes: String? = null,
)
