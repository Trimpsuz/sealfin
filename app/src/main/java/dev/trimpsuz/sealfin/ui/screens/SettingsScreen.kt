package dev.trimpsuz.sealfin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.trimpsuz.sealfin.ui.theme.AppTheme
import dev.trimpsuz.sealfin.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onServerSelectorRequested: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val currentTheme by themeViewModel.theme.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    fun selectTheme(theme: AppTheme) {
        themeViewModel.setTheme(theme)
        showThemeDialog = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                ListItem(
                    headlineContent = { Text("Display", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text(currentTheme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.clickable { showThemeDialog = true },
                    leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Servers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Select Server") },
                    modifier = Modifier.clickable { onServerSelectorRequested() },
                    leadingContent = { Icon(Icons.Outlined.AccountTree, contentDescription = null) }
                )
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectTheme(theme) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentTheme == theme, onClick = { selectTheme(theme) })
                            Text(text = theme.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }
}
