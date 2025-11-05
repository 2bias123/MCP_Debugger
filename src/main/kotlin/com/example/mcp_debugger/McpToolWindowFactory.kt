package com.example.mcp_debugger

import ai.grazie.utils.json.JSONObject
import androidx.compose.foundation.border
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class McpToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val composePanel = ComposePanel().apply {
            setContent {
                MaterialTheme {
                    McpInspectorUI()
                }
            }
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(composePanel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val serverUrl: String) : ConnectionState()
    data class Error(val serverUrlTried: String, val message: String) : ConnectionState()
}

@Composable
fun McpInspectorUI() {
    val connectionState = remember { mutableStateOf<ConnectionState>(ConnectionState.Disconnected) }
    val serverUrl = remember { mutableStateOf("http://localhost:8000") } // user-editable
    val tools = remember { mutableStateListOf<McpTool>() }
    val selectedTool = remember { mutableStateOf<McpTool?>(null) }
    val result = remember { mutableStateOf<String?>(null) }
    val paramValues = remember { mutableStateMapOf<String, String>() }

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            ConnectionPane(connectionState, serverUrl, tools, onDisconnect = {
                tools.clear()
                selectedTool.value = null
                result.value = null
                connectionState.value = ConnectionState.Disconnected
            }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            ToolsPane(connectionState, tools, selectedTool)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            DetailsPane(connectionState, selectedTool, result, paramValues)
        }
    }
}

@Composable
fun ConnectionPane(
    connectionState: MutableState<ConnectionState>,
    serverUrl: MutableState<String>,
    tools: SnapshotStateList<McpTool>,
    onDisconnect: () -> Unit
) {
   val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Connection", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = serverUrl.value,
            onValueChange = { serverUrl.value = it },
            label = { Text("MCP Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when (val state = connectionState.value) {
            ConnectionState.Disconnected -> {
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
                }) { Text("Connect") }
                Text("Status: not connected")
            }

            is ConnectionState.Connecting -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Connecting to server ${serverUrl.value}..")
                }
            }

            is ConnectionState.Connected -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDisconnect) { Text("Disconnect") }
                    Text("Connected to ${serverUrl.value}")
                }
            }

            is ConnectionState.Error -> {
                Text(
                    "Error connecting to ${state.serverUrlTried}: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button( onClick = {
                        connectionState.value = ConnectionState.Disconnected
                    }) { Text("Reset") }
                    Button( onClick = {
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

@Composable
fun ToolsPane(
    connectionState: State<ConnectionState>,
    tools: List<McpTool>,
    selectedTool: MutableState<McpTool?>
) {
    when (connectionState.value) {
        is ConnectionState.Connected -> {
            LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                items(tools) { tool ->
                    TextButton(onClick = { selectedTool.value = tool }) {
                        Text(tool.name)
                    }
                }
            }
        }
        is ConnectionState.Connecting -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Connecting…") }
        is ConnectionState.Error, ConnectionState.Disconnected -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Connect to load tools.") }
    }
}

@Composable
fun DetailsPane(
    connectionState: State<ConnectionState>,
    selectedTool: State<McpTool?>,
    result: MutableState<String?>,
    paramValues: MutableMap<String, String>
) {
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("📄 Details & Results", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val tool = selectedTool.value
        if (tool == null) {
            Text("Select a tool to view details.")
            return
        }

        Text("Selected: ${tool.name}")
        tool.description?.let { Text(it) }
        Spacer(Modifier.height(8.dp))

        tool.parameters.forEach { param ->
            OutlinedTextField(
                value = paramValues[param.name] ?: "",
                onValueChange = { paramValues[param.name] = it },
                label = { Text("${param.name}${if (param.required) " *" else ""}") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        val connected = connectionState.value is ConnectionState.Connected
        Button(
            enabled = connected,
            onClick = {
                val server = (connectionState.value as ConnectionState.Connected).serverUrl
                scope.launch {
                    val server = (connectionState.value as ConnectionState.Connected).serverUrl
                    val inputJson = org.json.JSONObject(paramValues as Map<*, *>).toString()
                    result.value = invokeToolSuspend(server, tool.name, inputJson)
                }
            }
        ) { Text(if (connected) "Invoke" else "Connect first") }

        Spacer(Modifier.height(12.dp))
        result.value?.let { Text("Result: $it") }
    }
}


