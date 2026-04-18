package com.example.studysmart.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.R
import com.example.studysmart.domain.model.Session
import com.example.studysmart.domain.model.Subject
import com.example.studysmart.domain.model.Task
import com.example.studysmart.presentation.NavAnimations
import com.example.studysmart.presentation.components.AddSubjectDialog
import com.example.studysmart.presentation.components.CountCard
import com.example.studysmart.presentation.components.DeleteDialog
import com.example.studysmart.presentation.components.SubjectCard
import com.example.studysmart.presentation.components.studySessionsList
import com.example.studysmart.presentation.components.tasksList
import com.example.studysmart.presentation.destinations.SessionScreenRouteDestination
import com.example.studysmart.presentation.destinations.SubjectScreenRouteDestination
import com.example.studysmart.presentation.destinations.TaskScreenRouteDestination
import com.example.studysmart.presentation.subject.SubjectScreenNavArgs
import com.example.studysmart.presentation.task.TaskScreenNavArgs
import com.example.studysmart.presentation.theme.LocalThemeController
import com.example.studysmart.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@Destination(style = NavAnimations::class)
@Composable
fun DashboardScreenRoute(
    navigator: DestinationsNavigator
) {

    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val todayFocusedHours by viewModel.todayFocusedHours.collectAsStateWithLifecycle()
    val completedTasksCount by viewModel.completedTasksCount.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        tasks = tasks,
        upcomingTasksCount = tasks.size,
        recentSessions = recentSessions,
        todayFocusedHours = todayFocusedHours,
        completedTasksCount = completedTasksCount,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onSubjectCardClick = { subjectId ->
            subjectId?.let {
                val navArg = SubjectScreenNavArgs(subjectId = subjectId)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onStartSessionButtonClick = {
            navigator.navigate(SessionScreenRouteDestination())
        }
    )
}

@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    upcomingTasksCount: Int,
    recentSessions: List<Session>,
    todayFocusedHours: Float,
    completedTasksCount: Int,
    onEvent: (DashboardEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onSubjectCardClick: (Int?) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit
) {

    val context = LocalContext.current
    var isAddSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredSubjects = remember(state.subjects, searchQuery) {
        if (searchQuery.isBlank()) state.subjects
        else state.subjects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) tasks
        else tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowSnackbar -> {
                    val msg = event.message
                        ?: event.messageResId?.let { context.getString(it) }
                        ?: ""
                    if (msg.isNotEmpty()) {
                        snackbarHostState.showSnackbar(message = msg, duration = event.duration)
                    }
                }

                SnackbarEvent.NavigateUp -> {}
            }
        }
    }

    AddSubjectDialog(
        isOpen = isAddSubjectDialogOpen,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        selectedColors = state.subjectCardColors,
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(DashboardEvent.OnGoalStudyHoursChange(it)) },
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
        onDismissRequest = { isAddSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen = false
        }
    )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = stringResource(id = R.string.delete_session_title),
        bodyText = stringResource(id = R.string.delete_session_body),
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { DashboardScreenTopBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.search_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search_hint)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                    )
                )
            }
            item {
                TodayInsightsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    todayHours = todayFocusedHours,
                    completedTasks = completedTasksCount,
                    upcomingTasks = upcomingTasksCount
                )
            }
            item {
                CountCardsSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subjectCount = state.totalSubjectCount,
                    studiedHours = state.totalStudiedHours.toString(),
                    goalHours = state.totalGoalStudyHours.toString()
                )
            }
            item {
                SubjectCardsSection(
                    modifier = Modifier.fillMaxWidth(),
                    subjectList = filteredSubjects,
                    onAddIconClicked = { isAddSubjectDialogOpen = true },
                    onSubjectCardClick = onSubjectCardClick
                )
            }
            item {
                Button(
                    onClick = onStartSessionButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(id = R.string.start_study_session)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.start_study_session).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            tasksList(
                sectionTitle = R.string.upcoming_tasks,
                emptyListText = R.string.no_upcoming_tasks,
                tasks = filteredTasks,
                onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            studySessionsList(
                sectionTitle = R.string.recent_study_sessions,
                emptyListText = R.string.no_recent_sessions,
                sessions = recentSessions,
                onDeleteIconClick = {
                    onEvent(DashboardEvent.OnDeleteSessionButtonClick(it))
                    isDeleteSessionDialogOpen = true
                }
            )
        }
    }
}

@Composable
private fun TodayInsightsCard(
    modifier: Modifier = Modifier,
    todayHours: Float,
    completedTasks: Int,
    upcomingTasks: Int
) {
    val hoursStr = remember(todayHours) {
        String.format(Locale.US, "%.2f", todayHours)
    }
    val scheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(20.dp)
    ElevatedCard(
        modifier = modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(scheme.primary, scheme.secondary, scheme.tertiary)
            ),
            shape = cardShape
        ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = scheme.primaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.insights_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InsightBlock(
                    modifier = Modifier.weight(1f),
                    label = stringResource(id = R.string.insight_today_focus),
                    value = "$hoursStr ${stringResource(id = R.string.hours_unit)}",
                    accent = scheme.primary,
                    labelOnAccent = scheme.onPrimaryContainer
                )
                InsightBlock(
                    modifier = Modifier.weight(1f),
                    label = stringResource(id = R.string.insight_tasks_done),
                    value = completedTasks.toString(),
                    accent = scheme.secondary,
                    labelOnAccent = scheme.onSecondaryContainer
                )
                InsightBlock(
                    modifier = Modifier.weight(1f),
                    label = stringResource(id = R.string.insight_upcoming_count),
                    value = upcomingTasks.toString(),
                    accent = scheme.tertiary,
                    labelOnAccent = scheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun InsightBlock(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
    labelOnAccent: Color
) {
    Column(
        modifier = modifier
            .background(
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelOnAccent.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBar() {
    val theme = LocalThemeController.current
    var menuOpen by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.theme_menu)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.theme_system)) },
                    onClick = {
                        theme.setMode("system")
                        menuOpen = false
                    },
                    leadingIcon = {
                        if (theme.mode == "system") {
                            Icon(Icons.Default.Check, contentDescription = null)
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.theme_light)) },
                    onClick = {
                        theme.setMode("light")
                        menuOpen = false
                    },
                    leadingIcon = {
                        if (theme.mode == "light") {
                            Icon(Icons.Default.Check, contentDescription = null)
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.theme_dark)) },
                    onClick = {
                        theme.setMode("dark")
                        menuOpen = false
                    },
                    leadingIcon = {
                        if (theme.mode == "dark") {
                            Icon(Icons.Default.Check, contentDescription = null)
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                )
                }
            }
        }
    )
}

@Composable
private fun CountCardsSection(
    modifier: Modifier,
    subjectCount: Int,
    studiedHours: String,
    goalHours: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier) {
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = stringResource(id = R.string.subject_count),
            count = "$subjectCount",
            containerColor = scheme.primaryContainer,
            headingColor = scheme.onPrimaryContainer,
            valueColor = scheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = stringResource(id = R.string.studied_hours),
            count = studiedHours,
            containerColor = scheme.secondaryContainer,
            headingColor = scheme.onSecondaryContainer,
            valueColor = scheme.secondary
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = stringResource(id = R.string.goal_study_hours),
            count = goalHours,
            containerColor = scheme.tertiaryContainer,
            headingColor = scheme.onTertiaryContainer,
            valueColor = scheme.tertiary
        )
    }
}

@Composable
private fun SubjectCardsSection(
    modifier: Modifier,
    subjectList: List<Subject>,
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(22.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.subjects),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAddIconClicked) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_subject)
                )
            }
        }
        if (subjectList.isEmpty()) {
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.img_books),
                contentDescription = stringResource(id = R.string.no_subjects)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.no_subjects),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(subjectList) { subject ->
                SubjectCard(
                    subjectName = subject.name,
                    gradientColors = subject.colors.map { Color(it) },
                    onClick = { onSubjectCardClick(subject.subjectId) }
                )
            }
        }
    }
}
