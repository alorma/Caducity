package com.alorma.caducity.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSwitch
import com.alorma.compose.settings.ui.SettingsSlider

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ThemeColors {
    SYSTEM, APP
}

@Composable
fun SettingsScreen() {
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var themeColors by remember { mutableStateOf(ThemeColors.SYSTEM) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationAdvanceTime by remember { mutableFloatStateOf(1f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Appearance Section
        SettingsGroup(
            title = { Text("Appearance") }
        ) {
            // Theme Mode Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { themeMode = ThemeMode.LIGHT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Light")
                    }
                    SegmentedButton(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { themeMode = ThemeMode.DARK },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Dark")
                    }
                    SegmentedButton(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { themeMode = ThemeMode.SYSTEM },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("System")
                    }
                }
            }
            
            HorizontalDivider()
            
            // Theme Colors Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme Colors",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = themeColors == ThemeColors.SYSTEM,
                        onClick = { themeColors = ThemeColors.SYSTEM },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("System colors")
                    }
                    SegmentedButton(
                        selected = themeColors == ThemeColors.APP,
                        onClick = { themeColors = ThemeColors.APP },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("App colors")
                    }
                }
            }
        }
        
        // Notifications Section
        SettingsGroup(
            title = { Text("Notifications") }
        ) {
            SettingsSwitch(
                state = notificationsEnabled,
                title = { Text("Enable notifications") },
                onCheckedChange = { notificationsEnabled = it }
            )
            
            HorizontalDivider()
            
            SettingsSlider(
                enabled = notificationsEnabled,
                value = notificationAdvanceTime,
                valueRange = 0f..3f,
                steps = 2,
                title = { Text("In advance notification time") },
                subtitle = {
                    Text(
                        when (notificationAdvanceTime.toInt()) {
                            0 -> "3 days"
                            1 -> "1 week"
                            2 -> "2 weeks"
                            3 -> "1 month"
                            else -> "3 days"
                        }
                    )
                },
                onValueChange = { notificationAdvanceTime = it }
            )
        }
    }
}
