package com.example.studysmart.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.model.Task
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.presentation.navArgs
import com.example.studysmart.util.Priority
import com.example.studysmart.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navArgs: TaskScreenNavArgs = savedStateHandle.navArgs()

    private val _state = MutableStateFlow(
        TaskState(
            subjectId = navArgs.subjectId,
            dueDate = Instant.now().toEpochMilli(),
            routeTaskId = navArgs.taskId
        )
    )
    val state = combine(
        _state,
        subjectRepository.getAllSubjects()
    ) { state, subjects ->
        val matched = navArgs.subjectId?.let { sid ->
            subjects.find { it.subjectId == sid }
        }
        val withSubjects = state.copy(subjects = subjects)
        if (withSubjects.relatedToSubject == null && matched != null) {
            withSubjects.copy(
                relatedToSubject = matched.name,
                subjectId = matched.subjectId
            )
        } else {
            withSubjects
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = TaskState(
            subjectId = navArgs.subjectId,
            dueDate = Instant.now().toEpochMilli(),
            routeTaskId = navArgs.taskId
        )
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    init {
        fetchTask()
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.OnTitleChange -> {
                _state.update {
                    it.copy(title = event.title)
                }
            }

            is TaskEvent.OnDescriptionChange -> {
                _state.update {
                    it.copy(description = event.description)
                }
            }

            is TaskEvent.OnDateChange -> {
                _state.update {
                    it.copy(dueDate = event.millis)
                }
            }

            is TaskEvent.OnPriorityChange -> {
                _state.update {
                    it.copy(priority = event.priority)
                }
            }

            TaskEvent.OnIsCompleteChange -> {
                _state.update {
                    it.copy(isTaskComplete = !_state.value.isTaskComplete)
                }
            }

            is TaskEvent.OnRelatedSubjectSelect -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }

            TaskEvent.SaveTask -> saveTask()
            TaskEvent.DeleteTask -> deleteTask()
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            try {
                val currentTaskId = state.value.currentTaskId
                if (currentTaskId != null) {
                    withContext(Dispatchers.IO) {
                        taskRepository.deleteTask(taskId = currentTaskId)
                    }
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(messageResId = com.example.studysmart.R.string.task_deleted)
                    )
                    _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
                } else {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(messageResId = com.example.studysmart.R.string.no_task_to_delete)
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "Couldn't delete task. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun saveTask() {
        viewModelScope.launch {
            val state = _state.value
            if (state.subjectId == null || state.relatedToSubject == null) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        messageResId = com.example.studysmart.R.string.select_related_subject
                    )
                )
                return@launch
            }
            try {
                taskRepository.upsertTask(
                    task = Task(
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate ?: Instant.now().toEpochMilli(),
                        relatedToSubject = state.relatedToSubject,
                        priority = state.priority.value,
                        isComplete = state.isTaskComplete,
                        taskSubjectId = state.subjectId,
                        taskId = state.currentTaskId
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(messageResId = com.example.studysmart.R.string.task_saved)
                )
                _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "Couldn't save task. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun fetchTask() {
        viewModelScope.launch {
            navArgs.taskId?.let { id ->
                taskRepository.getTaskById(id)?.let { task ->
                    _state.update {
                        it.copy(
                            title = task.title,
                            description = task.description,
                            dueDate = task.dueDate,
                            isTaskComplete = task.isComplete,
                            relatedToSubject = task.relatedToSubject,
                            priority = Priority.fromInt(task.priority),
                            subjectId = task.taskSubjectId,
                            currentTaskId = task.taskId
                        )
                    }
                }
            }
        }
    }

}