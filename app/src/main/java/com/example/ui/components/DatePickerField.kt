package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMedium
import java.text.SimpleDateFormat
import java.util.*

/**
 * Material Design 3 DatePicker Field Component for consistent date formatting
 * and error-free date selection in forms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Data de Administração *",
    placeholder: String = "AAAA-MM-DD",
    supportingText: String? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    testTag: String = "date_administered_picker_field"
) {
    var showDatePickerModal by remember { mutableStateOf(false) }

    // Parse current value to UTC millis for initial state
    val initialMillis = remember(value) {
        parseDateToUtcMillis(value) ?: System.currentTimeMillis()
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Read-only via keyboard; date selected via Material DatePicker
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            supportingText = supportingText?.let { { Text(it, fontSize = 11.sp) } },
            isError = isError,
            trailingIcon = {
                IconButton(
                    onClick = { showDatePickerModal = true },
                    modifier = Modifier.testTag("${testTag}_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Abrir Calendário",
                        tint = EmeraldDark
                    )
                }
            },
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = EmeraldDark,
                unfocusedBorderColor = SlateBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        // Overlay transparent click detector to trigger DatePicker on full field tap
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDatePickerModal = true }
                )
        )
    }

    if (showDatePickerModal) {
        MaterialDatePickerModal(
            initialSelectedDateMillis = initialMillis,
            title = label.replace("*", "").trim(),
            onDateSelected = { selectedDateFormatted ->
                onValueChange(selectedDateFormatted)
                showDatePickerModal = false
            },
            onDismiss = { showDatePickerModal = false }
        )
    }
}

/**
 * Material Design 3 Modal DatePickerDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePickerModal(
    initialSelectedDateMillis: Long = System.currentTimeMillis(),
    title: String = "Data de Administração",
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = formatUtcMillisToDateString(millis)
                        onDateSelected(formattedDate)
                    } ?: run {
                        // Fallback to today if none selected
                        val today = formatUtcMillisToDateString(System.currentTimeMillis())
                        onDateSelected(today)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("date_picker_confirm_button")
            ) {
                Text("Confirmar Data", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val today = formatUtcMillisToDateString(System.currentTimeMillis())
                        onDateSelected(today)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hoje", color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("date_picker_cancel_button")
                ) {
                    Text("Cancelar", color = SlateMedium)
                }
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            headline = {
                DatePickerDefaults.DatePickerHeadline(
                    selectedDateMillis = datePickerState.selectedDateMillis,
                    displayMode = datePickerState.displayMode,
                    dateFormatter = remember { DatePickerDefaults.dateFormatter() },
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                selectedDayContainerColor = EmeraldDark,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = EmeraldPrimary,
                todayContentColor = EmeraldDark,
                dayContentColor = SlateDark,
                subheadContentColor = SlateMedium,
                yearContentColor = SlateDark,
                currentYearContentColor = EmeraldDark,
                selectedYearContainerColor = EmeraldDark,
                selectedYearContentColor = Color.White
            )
        )
    }
}

/**
 * Format UTC Epoch millis to standardized ISO Date (YYYY-MM-DD)
 */
fun formatUtcMillisToDateString(utcMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(utcMillis))
}

/**
 * Parse date string (supports YYYY-MM-DD, DD/MM/YYYY, DD-MM-YYYY) to UTC millis
 */
fun parseDateToUtcMillis(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    val patterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy")
    for (pattern in patterns) {
        try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = formatter.parse(dateStr.trim())
            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {
            // try next pattern
        }
    }
    return null
}
