package com.example.studysmart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.example.studysmart.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studysmart.domain.model.Subject

@Composable
fun AddSubjectDialog(
    isOpen: Boolean,
    title: String = stringResource(id = R.string.add_update_subject),
    selectedColors: List<Color>,
    subjectName: String,
    goalHours: String,
    onColorChange: (List<Color>) -> Unit,
    onSubjectNameChange: (String) -> Unit,
    onGoalHoursChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit
) {
    var subjectNameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var goalHoursError by rememberSaveable { mutableStateOf<Int?>(null) }

    subjectNameError = when {
        subjectName.isBlank() -> R.string.enter_subject_name
        subjectName.length < 2 -> R.string.subject_name_too_short
        subjectName.length > 20 -> R.string.subject_name_too_long
        else -> null
    }
    goalHoursError = when {
        goalHours.isBlank() -> R.string.enter_goal_hours
        goalHours.toFloatOrNull() == null -> R.string.invalid_number
        goalHours.toFloat() < 1f -> R.string.min_goal_hours
        goalHours.toFloat() > 1000f -> R.string.max_goal_hours
        else -> null
    }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Subject.subjectCardColors.forEach { colors ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = if (colors == selectedColors) Color.Black
                                        else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .background(brush = Brush.verticalGradient(colors))
                                    .clickable { onColorChange(colors) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = stringResource(id = R.string.subject_name)) },
                        singleLine = true,
                        isError = subjectNameError != null && subjectName.isNotBlank(),
                        supportingText = { Text(text = subjectNameError?.let { stringResource(id = it) }.orEmpty())}
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalHours,
                        onValueChange = onGoalHoursChange,
                        label = { Text(text = stringResource(id = R.string.goal_study_hours)) },
                        singleLine = true,
                        isError = goalHoursError != null && goalHours.isNotBlank(),
                        supportingText = { Text(text = goalHoursError?.let { stringResource(id = it) }.orEmpty())},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClick,
                    enabled = subjectNameError == null && goalHoursError == null
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        )
    }
}