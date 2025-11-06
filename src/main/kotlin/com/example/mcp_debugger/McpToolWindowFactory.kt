package com.example.mcp_debugger

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import com.example.mcp_debugger.ui.panes.DetailsPane
import com.example.mcp_debugger.ui.panes.ToolsPane
import com.example.mcp_debugger.ui.panes.ConnectionPane
import com.example.mcp_debugger.model.ConnectionState
import com.example.mcp_debugger.model.McpTool
import com.example.mcp_debugger.network.disconnectFromServer


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
            ConnectionPane(
                connectionState,
                serverUrl,
                tools,
                onDisconnect = {
                    disconnectFromServer(connectionState, tools, selectedTool, result, paramValues)
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



