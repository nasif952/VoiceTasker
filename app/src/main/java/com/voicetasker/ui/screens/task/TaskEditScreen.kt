package com.voicetasker.ui.screens.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.voicetasker.core.model.TaskPriority
import com.voicetasker.features.task.presentation.TaskEditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    viewModel: TaskEditViewModel = hiltViewModel(),
    navController: NavHostController
) {
    // State for date picker dialog
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Observe success state and navigate back
    if (viewModel.isUpdateSuccess.value) {
        viewModel.resetUpdateSuccess()
        navController.popBackStack()
    }

    // Date picker dialog
    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            viewModel.dueDate.value = formatter.format(Date(millis))
                        }
                        showDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!viewModel.isTaskLoaded.value && viewModel.isLoading.value) {
            // Show loading while task is being fetched
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading task...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                // Error message
                if (viewModel.error.value != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = viewModel.error.value ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Title field
                OutlinedTextField(
                    value = viewModel.title.value,
                    onValueChange = { viewModel.title.value = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading.value,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field
                OutlinedTextField(
                    value = viewModel.description.value,
                    onValueChange = { viewModel.description.value = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    enabled = !viewModel.isLoading.value,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Priority selection
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { priority ->
                        val isSelected = viewModel.priority.value == priority
                        val buttonColors = when (priority) {
                            TaskPriority.HIGH -> ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error
                            )
                            TaskPriority.MEDIUM -> ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                            TaskPriority.LOW -> ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Button(
                            onClick = { viewModel.priority.value = priority },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            enabled = !viewModel.isLoading.value,
                            colors = buttonColors
                        ) {
                            Text(priority.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due date field with date picker
                OutlinedTextField(
                    value = viewModel.dueDate.value,
                    onValueChange = { },
                    label = { Text("Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !viewModel.isLoading.value) {
                            showDatePicker.value = true
                        },
                    enabled = false,
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker.value = true },
                            enabled = !viewModel.isLoading.value
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    placeholder = { Text("Tap to select date") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = { viewModel.updateTask() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading.value
                ) {
                    if (viewModel.isLoading.value) {
                        Text("Saving...")
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
