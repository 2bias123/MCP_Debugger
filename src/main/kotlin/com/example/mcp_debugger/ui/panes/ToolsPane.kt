package com.example.mcp_debugger.ui.panes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mcp_debugger.model.*


@Composable
fun ToolsPane(
    connectionState: State<ConnectionState>,
    tools: List<McpTool>,
    selectedTool: MutableState<McpTool?>
) {
    when (connectionState.value) {
        is ConnectionState.Connected -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tools) { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTool.value = tool }
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTool.value == tool)
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) // green highlight
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selectedTool.value == tool)
                            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        else
                            null                    ) {
                        Text(
                            text = tool.name,
                            modifier = Modifier.padding(12.dp),
                            color = if (selectedTool.value == tool)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        is ConnectionState.Connecting ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Connecting…", color = MaterialTheme.colorScheme.onSurface)
            }

        is ConnectionState.Error, ConnectionState.Disconnected ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Connect to load tools.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
    }
}


