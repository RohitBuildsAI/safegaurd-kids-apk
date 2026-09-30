package com.example.ui.screens.parent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (name: String, age: Int, emoji: String, device: String, limitMinutes: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("10") }
    var emoji by remember { mutableStateOf("👦") }
    var deviceModel by remember { mutableStateOf("Samsung Galaxy S22") }
    var limitMinutes by remember { mutableIntStateOf(120) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val age = ageStr.toIntOrNull() ?: 10
                        onConfirmAdd(name.trim(), age, emoji, deviceModel.trim(), limitMinutes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("button_confirm_add_child")
            ) {
                Text("Create Profile")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Add Child Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Child Name (e.g. Noah)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_new_child_name")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Avatar Emoji") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = deviceModel,
                    onValueChange = { deviceModel = it },
                    label = { Text("Child Device Model") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Initial Daily Limit: ${limitMinutes / 60}h ${limitMinutes % 60}m")
                Slider(
                    value = limitMinutes.toFloat(),
                    onValueChange = { limitMinutes = it.toInt() },
                    valueRange = 30f..300f,
                    steps = 8
                )
            }
        }
    )
}
