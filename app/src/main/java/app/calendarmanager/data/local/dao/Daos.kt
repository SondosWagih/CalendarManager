package app.calendarmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.calendarmanager.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface BaseDao<T> {
    @Insert suspend fun insert(item: T): Long
    @Update suspend fun update(item: T)
}

@Dao
interface CourseDao : BaseDao<CourseEntity> {
    @Query("SELECT * FROM courses WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<CourseEntity>>

    @Query("UPDATE courses SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface StudyEventDao : BaseDao<StudyEventEntity> {
    @Query("SELECT * FROM study_events WHERE deletedAt IS NULL ORDER BY dayOfWeek, startMinute")
    fun observeActive(): Flow<List<StudyEventEntity>>

    @Query("SELECT * FROM study_events WHERE id = :id")
    suspend fun get(id: Long): StudyEventEntity?

    @Query("UPDATE study_events SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface StudySessionDao : BaseDao<StudySessionEntity> {
    @Query("SELECT * FROM study_sessions WHERE deletedAt IS NULL AND dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay, startMinute")
    fun observeRange(from: Long, to: Long): Flow<List<StudySessionEntity>>

    @Query("UPDATE study_sessions SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface ExamDao : BaseDao<ExamEntity> {
    @Query("SELECT * FROM exams WHERE deletedAt IS NULL AND dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay, startMinute")
    fun observeRange(from: Long, to: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun get(id: Long): ExamEntity?

    @Query("UPDATE exams SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface AssignmentDao : BaseDao<AssignmentEntity> {
    @Query("SELECT * FROM assignments WHERE deletedAt IS NULL AND dueEpochDay BETWEEN :from AND :to ORDER BY dueEpochDay, dueMinute")
    fun observeRange(from: Long, to: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE deletedAt IS NULL AND completed = 0 ORDER BY dueEpochDay")
    fun observePending(): Flow<List<AssignmentEntity>>

    @Query("UPDATE assignments SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface WorkGroupDao : BaseDao<WorkGroupEntity> {
    @Query("SELECT * FROM work_groups WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<WorkGroupEntity>>

    @Query("UPDATE work_groups SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface WorkEventDao : BaseDao<WorkEventEntity> {
    @Query("SELECT * FROM work_events WHERE deletedAt IS NULL")
    fun observeActive(): Flow<List<WorkEventEntity>>

    @Query("SELECT * FROM work_events WHERE id = :id")
    suspend fun get(id: Long): WorkEventEntity?

    @Query("UPDATE work_events SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface TaskGroupDao : BaseDao<TaskGroupEntity> {
    @Query("SELECT * FROM task_groups WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<TaskGroupEntity>>

    @Query("UPDATE task_groups SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface TaskDao : BaseDao<TaskEntity> {
    @Query("SELECT * FROM tasks WHERE deletedAt IS NULL AND dueEpochDay BETWEEN :from AND :to ORDER BY dueEpochDay, dueMinute")
    fun observeRange(from: Long, to: Long): Flow<List<TaskEntity>>

    /** Overdue tasks stay visible until completed or rescheduled. */
    @Query("SELECT * FROM tasks WHERE deletedAt IS NULL AND completed = 0 AND dueEpochDay < :todayEpochDay ORDER BY dueEpochDay")
    fun observeOverdue(todayEpochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE deletedAt IS NULL AND completed = 0 AND dueEpochDay < :todayEpochDay")
    suspend fun countOverdue(todayEpochDay: Long): Int

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): TaskEntity?

    @Query("UPDATE tasks SET completed = 1, completedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun markCompleted(id: Long, now: Long)

    @Query("UPDATE tasks SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}

@Dao
interface NotificationRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: NotificationRecordEntity): Long

    @Query("SELECT * FROM notification_records WHERE ownerType = :type AND ownerId = :ownerId")
    suspend fun forOwner(type: NotificationOwnerType, ownerId: Long): List<NotificationRecordEntity>

    @Query("SELECT * FROM notification_records WHERE status = 'SCHEDULED'")
    suspend fun allScheduled(): List<NotificationRecordEntity>

    @Query("UPDATE notification_records SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun setStatus(id: Long, status: NotificationStatus, now: Long)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(setting: AppSettingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun putIfAbsent(setting: AppSettingEntity): Long
}
