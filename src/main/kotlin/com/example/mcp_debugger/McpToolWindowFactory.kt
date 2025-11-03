package com.example.mcp_debugger

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

@Composable
fun McpInspectorUI() {
    val isConnected = remember { mutableStateOf(false) }
    val tools = remember { mutableStateListOf<String>() }
    val selectedTool = remember { mutableStateOf<String?>(null) }
    val result = remember { mutableStateOf<String?>(null) }

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
            ConnectionPane(isConnected, tools)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            ToolsPane(isConnected, tools, selectedTool)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            DetailsPane(selectedTool, result)
        }
    }
}

@Composable
fun ConnectionPane(
    isConnected: MutableState<Boolean>,
    tools: SnapshotStateList<String>
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Connection Pane",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
        )
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = {
                if (!isConnected.value) {
                    connectToServer(isConnected, tools)
                }
                      }) {
            Text(if (isConnected.value) "Connected" else "Connect to MCP server")
        }
    }
}

@Composable
fun ToolsPane(
    isConnected: State<Boolean>,
    tools: List<String>,
    selectedTool: MutableState<String?>
) {
    if (!isConnected.value) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connect first to load tools.")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
            items(tools) { tool ->
                TextButton(onClick = { selectedTool.value = tool }) {
                    Text(tool)
                }
            }
        }
    }
}

@Composable
fun DetailsPane(
    selectedTool: State<String?>,
    result: MutableState<String?>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("📄 Details & Results Pane", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (!selectedTool.value.isNullOrBlank()) {
            Text("Selected tool: ${selectedTool.value}")
            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                ApplicationManager.getApplication().executeOnPooledThread {
                    val res = invokeToolSync(selectedTool.value!!, "Hello MCP!")
                    ApplicationManager.getApplication().invokeLater {
                        result.value = res
                    }
                }
            }) {
                Text("Invoke")
            }
        } else {
            Text("Select a tool to view details.")
        }

        Spacer(Modifier.height(12.dp))

        result.value?.let {
            Text("Result: $it")
        }
    }
}

