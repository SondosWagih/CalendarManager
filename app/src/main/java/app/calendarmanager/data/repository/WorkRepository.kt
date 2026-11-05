package app.calendarmanager.data.repository

import androidx.room.withTransaction
import app.calendarmanager.data.local.db.AppDatabase
import app.calendarmanager.data.local.entity.WorkEventEntity
import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.data.local.entity.WorkGroupEntity
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft
import app.calendarmanager.domain.validation.ValidationError
import app.calendarmanager.domain.validation.WorkValidator
import kotlinx.coroutines.flow.Flow

/** Work groups are fully independent: each owns its weekly schedule and its one-off events. */
class WorkRepository(
    private val db: AppDatabase,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    val groups: Flow<List<WorkGroupEntity>> = db.workGroupDao().observeActive()
    val events: Flow<List<WorkEventEntity>> = db.workEventDao().observeActive()

    private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private suspend fun groupExists(id: Long): Boolean {
        val g = db.workGroupDao().get(id)
        return g != null && g.deletedAt == null
    }

    suspend fun saveGroup(d: WorkGroupDraft): ValidationError? {
        WorkValidator.group(d)?.let { return it }
        val t = now()
        val id = d.id
        if (id == null) {
            db.workGroupDao().insert(WorkGroupEntity(name = d.name.trim(), colorArgb = d.colorArgb, createdAt = t, updatedAt = t))
        } else {
            val old = db.workGroupDao().get(id)?.takeIf { it.deletedAt == null } ?: return ValidationError.NOT_FOUND
            db.workGroupDao().update(old.copy(name = d.name.trim(), colorArgb = d.colorArgb, updatedAt = t))
        }
        return null
    }

    suspend fun saveEvent(d: WorkEventDraft): ValidationError? {
        WorkValidator.event(d)?.let { return it }
        val groupId = d.groupId ?: return ValidationError.GROUP_REQUIRED
        if (!groupExists(groupId)) return ValidationError.GROUP_REQUIRED
        val weekly = d.kind == WorkEventKind.WEEKLY
        // Normalise so a weekly event never carries a stale date and a dated event never carries a weekday.
        val day = if (weekly) d.dayOfWeek else null
        val date = if (weekly) null else d.dateEpochDay
        val t = now()
        val id = d.id
        if (id == null) {
            db.workEventDao().insert(
                WorkEventEntity(groupId = groupId, title = d.title.trim(), kind = d.kind, dayOfWeek = day,
                    dateEpochDay = date, startMinute = d.startMinute, endMinute = d.endMinute,
                    location = d.location.clean(), notes = d.notes.clean(), createdAt = t, updatedAt = t)
            )
        } else {
            val old = db.workEventDao().get(id)?.takeIf { it.deletedAt == null } ?: return ValidationError.NOT_FOUND
            db.workEventDao().update(old.copy(groupId = groupId, title = d.title.trim(), kind = d.kind,
                dayOfWeek = day, dateEpochDay = date, startMinute = d.startMinute, endMinute = d.endMinute,
                location = d.location.clean(), notes = d.notes.clean(), updatedAt = t))
        }
        return null
    }

    suspend fun deleteEvent(id: Long) = db.workEventDao().softDelete(id, now())

    /** Atomic: the group and all its events are hidden together, or nothing changes. */
    suspend fun deleteGroup(id: Long) {
        db.withTransaction {
            val t = now()
            db.workEventDao().softDeleteByGroup(id, t)
            db.workGroupDao().softDelete(id, t)
        }
    }
}
