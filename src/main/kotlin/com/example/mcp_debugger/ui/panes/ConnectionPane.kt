package com.example.mcp_debugger.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mcp_debugger.model.*
import com.example.mcp_debugger.network.connectAndFetchTools
import com.example.mcp_debugger.network.disconnectFromServer
import kotlinx.coroutines.launch


@Composable
fun ConnectionPane(
    connectionState: MutableState<ConnectionState>,
    serverUrl: MutableState<String>,
    tools: SnapshotStateList<McpTool>,
    onDisconnect: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Connection",
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary)
        )

        OutlinedTextField(
            value = serverUrl.value,
            onValueChange = { serverUrl.value = it },
            label = { Text("MCP Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when (val state = connectionState.value) {
            ConnectionState.Disconnected -> {
                Button(
                    onClick = {
                        connectionState.value = ConnectionState.Connecting
                        scope.launch {
                            try {
                                val fetched = connectAndFetchTools(serverUrl.value)
                                tools.clear()
                                tools.addAll(fetched)
                                connectionState.value = ConnectionState.Connected(serverUrl.value)
                            } catch (e: Exception) {
                                connectionState.value = ConnectionState.Error(
                                    serverUrl.value,
                                    e.message ?: "Unknown error"
                                )
                            }
                        }
                    },
                    enabled = connectionState.value !is ConnectionState.Connecting
                ) { Text("Connect") }

                Text("Status: not connected")
            }

            is ConnectionState.Connecting -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Connecting to ${serverUrl.value}…")
                }
            }

            is ConnectionState.Connected -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Disconnect", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Text("Connected to ${serverUrl.value}", color = MaterialTheme.colorScheme.onSurface)
                }
            }


            is ConnectionState.Error -> {
                Text(
                    "Error connecting to ${state.serverUrlTried}: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { connectionState.value = ConnectionState.Disconnected }) {
                        Text("Reset")
                    }
                    Button(onClick = {
                        connectionState.value = ConnectionState.Connecting
                        scope.launch {
                            try {
                                val fetched = connectAndFetchTools(serverUrl.value)
                                tools.clear()
                                tools.addAll(fetched)
                                connectionState.value = ConnectionState.Connected(serverUrl.value)
                            } catch (e: Exception) {
                                connectionState.value = ConnectionState.Error(
                                    serverUrl.value,
                                    e.message ?: "Unknown error"
                                )
                            }
                        }
                    }) { Text("Retry") }
                }
            }
        }
    }
}

