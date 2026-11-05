package app.calendarmanager

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.calendarmanager.data.local.db.AppDatabase
import app.calendarmanager.data.local.entity.WorkEventKind
import app.calendarmanager.data.repository.WorkRepository
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft
import app.calendarmanager.domain.validation.ValidationError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: WorkRepository

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        repo = WorkRepository(db) { 1_700_000_000_000L }
    }
    @After fun tearDown() = db.close()

    private suspend fun newGroup(name: String): Long {
        assertNull(repo.saveGroup(WorkGroupDraft(name = name, colorArgb = 0xFF2E9E6B.toInt())))
        return repo.groups.first().first { it.name == name }.id
    }

    private fun weekly(group: Long, title: String = "Shift") =
        WorkEventDraft(groupId = group, title = title, kind = WorkEventKind.WEEKLY, dayOfWeek = 3, startMinute = 540, endMinute = 840)

    @Test fun supportsManyIndependentGroups() = runBlocking {
        repeat(25) { newGroup("Group $it") }
        assertEquals(25, repo.groups.first().size)
    }

    @Test fun weeklyAndUnexpectedEventsAreStoredNormalised() = runBlocking {
        val g = newGroup("Cafe")
        // A weekly draft carrying a stray date must not keep it.
        assertNull(repo.saveEvent(weekly(g).copy(dateEpochDay = 123)))
        assertNull(repo.saveEvent(WorkEventDraft(groupId = g, title = "Urgent call", kind = WorkEventKind.UNEXPECTED,
            dayOfWeek = 5, dateEpochDay = 20000, startMinute = 700, endMinute = 760)))
        val events = repo.events.first()
        val w = events.first { it.kind == WorkEventKind.WEEKLY }
        val u = events.first { it.kind == WorkEventKind.UNEXPECTED }
        assertEquals(3, w.dayOfWeek); assertNull(w.dateEpochDay)
        assertNull(u.dayOfWeek); assertEquals(20000L, u.dateEpochDay)
    }

    @Test fun invalidEventIsRejectedAndNotStored() = runBlocking {
        val g = newGroup("Cafe")
        assertEquals(ValidationError.END_BEFORE_START, repo.saveEvent(weekly(g).copy(startMinute = 900, endMinute = 800)))
        assertEquals(ValidationError.GROUP_REQUIRED, repo.saveEvent(weekly(999)))
        assertTrue(repo.events.first().isEmpty())
    }

    @Test fun editingUpdatesTheSameRow() = runBlocking {
        val g = newGroup("Cafe")
        repo.saveEvent(weekly(g))
        val saved = repo.events.first().single()
        assertNull(repo.saveEvent(weekly(g, title = "Late shift").copy(id = saved.id)))
        val after = repo.events.first().single()
        assertEquals(saved.id, after.id); assertEquals("Late shift", after.title)
    }

    @Test fun deletingOneGroupLeavesOthersUntouched() = runBlocking {
        val a = newGroup("A"); val b = newGroup("B")
        repo.saveEvent(weekly(a, "a1")); repo.saveEvent(weekly(b, "b1"))
        repo.deleteGroup(a)
        assertEquals(listOf("B"), repo.groups.first().map { it.name })
        assertEquals(listOf("b1"), repo.events.first().map { it.title })
    }
}
