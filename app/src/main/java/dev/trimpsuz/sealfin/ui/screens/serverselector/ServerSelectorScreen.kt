import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.trimpsuz.sealfin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectorScreen(
    serverViewModel: AuthViewModel = hiltViewModel(),
    onServerSelected: () -> Unit
) {
    val servers by serverViewModel.servers.collectAsState()
    val activeServer by serverViewModel.activeServer.collectAsState()
    var showAddServerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Server") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(servers) { server ->
                ListItem(
                    headlineContent = { Text(server.name) },
                    supportingContent = { Text(server.baseUrl) },
                    leadingContent = {
                        if (activeServer?.id == server.id) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { serverViewModel.removeServer(server.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove server")
                        }
                    },
                    modifier = Modifier.clickable {
                        serverViewModel.switchServer(server.id)
                        onServerSelected()
                    }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showAddServerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add New Server")
                }
            }
        }
    }

    if (showAddServerDialog) {
        AddServerDialog(
            onDismiss = { showAddServerDialog = false },
            onServerAdded = {
                showAddServerDialog = false
                onServerSelected()
            }
        )
    }
}
