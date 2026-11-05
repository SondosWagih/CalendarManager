package app.calendarmanager.ui.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.calendarmanager.data.local.entity.WorkEventEntity
import app.calendarmanager.data.local.entity.WorkGroupEntity
import app.calendarmanager.data.repository.WorkRepository
import app.calendarmanager.domain.model.WorkEventDraft
import app.calendarmanager.domain.model.WorkGroupDraft
import app.calendarmanager.domain.validation.ValidationError
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkUiState(
    val groups: List<WorkGroupEntity> = emptyList(),
    val events: List<WorkEventEntity> = emptyList(),
)

class WorkViewModel(private val repo: WorkRepository) : ViewModel() {
    val state: StateFlow<WorkUiState> = combine(repo.groups, repo.events) { g, e -> WorkUiState(g, e) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WorkUiState())

    fun saveGroup(d: WorkGroupDraft, onResult: (ValidationError?) -> Unit) { viewModelScope.launch { onResult(repo.saveGroup(d)) } }
    fun saveEvent(d: WorkEventDraft, onResult: (ValidationError?) -> Unit) { viewModelScope.launch { onResult(repo.saveEvent(d)) } }
    fun deleteGroup(id: Long) { viewModelScope.launch { repo.deleteGroup(id) } }
    fun deleteEvent(id: Long) { viewModelScope.launch { repo.deleteEvent(id) } }

    companion object {
        fun factory(repo: WorkRepository) = viewModelFactory { initializer { WorkViewModel(repo) } }
    }
}
