package com.example.studysmart.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DeleteDialog(
    isOpen: Boolean,
    title: String,
    bodyText: String,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = androidx.compose.ui.res.stringResource(id = com.example.studysmart.R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmButtonClick) {
                    Text(text = androidx.compose.ui.res.stringResource(id = com.example.studysmart.R.string.delete))
                }
            }
        )
    }
}