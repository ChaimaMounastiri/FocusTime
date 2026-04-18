package com.example.studysmart.presentation.task

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.DeleteDialog
import com.example.studysmart.presentation.components.SubjectListBottomSheet
import com.example.studysmart.presentation.components.TaskCheckBox
import com.example.studysmart.presentation.components.TaskDatePicker
import androidx.compose.ui.res.stringResource
import com.example.studysmart.R
import com.example.studysmart.util.Priority
import com.example.studysmart.util.SnackbarEvent
import com.example.studysmart.util.changeMillisToDateString
import com.example.studysmart.presentation.NavAnimations
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

data class TaskScreenNavArgs(
    val taskId: Int?,
    val subjectId: Int?
)

@Destination(
    navArgsDelegate = TaskScreenNavArgs::class,
    style = NavAnimations::class
)
@Composable
fun TaskScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel: TaskViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TaskScreen(
        state = state,
        snackbarEvent = viewModel.snackbarEventFlow,
        onEvent = viewModel::onEvent,
        onBackButtonClick = { navigator.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreen(
    state: TaskState,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onEvent: (TaskEvent) -> Unit,
    onBackButtonClick: () -> Unit
) {

    val context = LocalContext.current
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    var isDatePickerDialogOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember { mutableStateOf(false) }

    var taskTitleError by rememberSaveable { mutableStateOf<Int?>(null) }
    taskTitleError = when {
        state.title.isBlank() -> R.string.enter_task_title
        state.title.length < 4 -> R.string.task_title_too_short
        state.title.length > 30 -> R.string.task_title_too_long
        else -> null
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message ?: context.getString(event.messageResId!!),
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> {
                    onBackButtonClick()
                }
            }
        }
    }

    DeleteDialog(
        isOpen = isDeleteDialogOpen,
        title = stringResource(id = R.string.delete_task_title),
        bodyText = stringResource(id = R.string.delete_task_body),
        onDismissRequest = { isDeleteDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(TaskEvent.DeleteTask)
            isDeleteDialogOpen = false
        }
    )

    TaskDatePicker(
        state = datePickerState,
        isOpen = isDatePickerDialogOpen,
        onDismissRequest = { isDatePickerDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(TaskEvent.OnDateChange(millis = datePickerState.selectedDateMillis))
            isDatePickerDialogOpen = false
        }
    )

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = state.subjects,
        onDismissRequest = { isBottomSheetOpen = false },
        onSubjectClicked = { subject ->
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(TaskEvent.OnRelatedSubjectSelect(subject))
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TaskScreenTopBar(
                isTaskExist = state.currentTaskId != null || state.routeTaskId != null,
                isComplete = state.isTaskComplete,
                checkBoxBorderColor = state.priority.color,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteDialogOpen = true },
                onCheckBoxClick = { onEvent(TaskEvent.OnIsCompleteChange) }
            )
        }
    ) { paddingValue ->
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .fillMaxSize()
                .padding(paddingValue)
                .padding(horizontal = 12.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.title,
                        onValueChange = { onEvent(TaskEvent.OnTitleChange(it)) },
                        label = { Text(text = stringResource(id = R.string.task_title)) },
                        singleLine = true,
                        isError = taskTitleError != null && state.title.isNotBlank(),
                        supportingText = { Text(text = taskTitleError?.let { stringResource(id = it) }.orEmpty()) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.description,
                        onValueChange = { onEvent(TaskEvent.OnDescriptionChange(it)) },
                        label = { Text(text = stringResource(id = R.string.description)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.due_date),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.dueDate.changeMillisToDateString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isDatePickerDialogOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(id = R.string.select_due_date)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.priority),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEach { priority ->
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        label = stringResource(id = priority.titleResId),
                        backgroundColor = priority.color,
                        borderColor = if (priority == state.priority) {
                            Color.White
                        } else Color.Transparent,
                        labelColor = if (priority == state.priority) {
                            Color.White
                        } else Color.White.copy(alpha = 0.7f),
                        isSelected = priority == state.priority,
                        onClick = { onEvent(TaskEvent.OnPriorityChange(priority)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = stringResource(id = R.string.related_to_subject),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.relatedToSubject ?: stringResource(id = R.string.select_subject),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isBottomSheetOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(id = R.string.select_subject)
                    )
                }
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300))
            ) {
                Button(
                    enabled = taskTitleError == null,
                    onClick = { onEvent(TaskEvent.SaveTask) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenTopBar(
    isTaskExist: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onCheckBoxClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.navigate_back)
                )
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.task),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            if (isTaskExist) {
                TaskCheckBox(
                    isComplete = isComplete,
                    borderColor = checkBoxBorderColor,
                    onCheckBoxClick = onCheckBoxClick
                )
                IconButton(onClick = onDeleteButtonClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_task_title)
                    )
                }
            }
        }
    )
}

@Composable
private fun PriorityButton(
    modifier: Modifier = Modifier,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    labelColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(5.dp)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = labelColor)
    }
}