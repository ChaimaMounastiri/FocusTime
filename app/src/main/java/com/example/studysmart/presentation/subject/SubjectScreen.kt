package com.example.studysmart.presentation.subject

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.AddSubjectDialog
import com.example.studysmart.presentation.components.CountCard
import com.example.studysmart.presentation.components.DeleteDialog
import com.example.studysmart.presentation.components.studySessionsList
import com.example.studysmart.presentation.components.tasksList
import com.example.studysmart.presentation.destinations.TaskScreenRouteDestination
import com.example.studysmart.presentation.task.TaskScreenNavArgs
import androidx.compose.ui.res.stringResource
import com.example.studysmart.R
import com.example.studysmart.util.SnackbarEvent
import com.example.studysmart.presentation.NavAnimations
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

data class SubjectScreenNavArgs(
    val subjectId: Int
)

@Destination(
    navArgsDelegate = SubjectScreenNavArgs::class,
    style = NavAnimations::class
)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel: SubjectViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubjectScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = { navigator.navigateUp() },
        onAddTaskButtonClick = {
            val navArg = TaskScreenNavArgs(taskId = null, subjectId = state.currentSubjectId)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state: SubjectState,
    onEvent: (SubjectEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onBackButtonClick: () -> Unit,
    onAddTaskButtonClick: () -> Unit,
    onTaskCardClick: (Int?) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val isFABExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    var isEditSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var isDeleteSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(key1 = state.studiedHours, key2 = state.goalStudyHours) {
        onEvent(SubjectEvent.UpdateProgress)
    }

    AddSubjectDialog(
        isOpen = isEditSubjectDialogOpen,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        onSubjectNameChange = { onEvent(SubjectEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(SubjectEvent.OnGoalStudyHoursChange(it)) },
        selectedColors = state.subjectCardColors,
        onColorChange = { onEvent(SubjectEvent.OnSubjectCardColorChange(it)) },
        onDismissRequest = { isEditSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.UpdateSubject)
            isEditSubjectDialogOpen = false
        }
    )

    DeleteDialog(
        isOpen = isDeleteSubjectDialogOpen,
        title = stringResource(id = R.string.delete_subject_title),
        bodyText = stringResource(id = R.string.delete_subject_body),
        onDismissRequest = { isDeleteSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSubject)
            isDeleteSubjectDialogOpen = false
        }
    )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = stringResource(id = R.string.delete_session_title),
        bodyText = stringResource(id = R.string.delete_session_body),
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubjectScreenTopBar(
                title = state.subjectName,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteSubjectDialogOpen = true },
                onEditButtonClick = { isEditSubjectDialogOpen = true },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskButtonClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.add)) },
                text = { Text(text = stringResource(id = R.string.add_task)) },
                expanded = isFABExpanded
            )
        }
    ) { paddingValue ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            item {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 200)) + 
                            slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(1000, delayMillis = 200))
                ) {
                    SubjectOverviewSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        studiedHours = state.studiedHours.toString(),
                        goalHours = state.goalStudyHours,
                        progress = state.progress
                    )
                }
            }
            tasksList(
                sectionTitle = R.string.upcoming_tasks,
                emptyListText = R.string.no_upcoming_tasks,
                tasks = state.upcomingTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            tasksList(
                sectionTitle = R.string.completed_tasks,
                emptyListText = R.string.no_completed_tasks,
                tasks = state.completedTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            studySessionsList(
                sectionTitle = R.string.recent_study_sessions,
                emptyListText = R.string.no_recent_sessions,
                sessions = state.recentSessions,
                onDeleteIconClick = {
                    isDeleteSessionDialogOpen = true
                    onEvent(SubjectEvent.OnDeleteSessionButtonClick(it))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreenTopBar(
    title: String,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
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
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            IconButton(onClick = onDeleteButtonClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_subject_title)
                )
            }
            IconButton(onClick = onEditButtonClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.add_update_subject)
                )
            }
        }
    )
}

@Composable
private fun SubjectOverviewSection(
    modifier: Modifier,
    studiedHours: String,
    goalHours: String,
    progress: Float
) {
    val percentageProgress = remember(key1 = progress) {
        (progress * 100).toInt().coerceIn(0, 100)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = stringResource(id = R.string.goal_study_hours),
            count = goalHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = stringResource(id = R.string.studied_hours),
            count = studiedHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "progressAnimation"
            )
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = 1f,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = animatedProgress,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            Text(text = "$percentageProgress%")
        }
    }
}