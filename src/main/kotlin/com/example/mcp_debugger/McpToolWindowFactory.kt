package com.example.mcp_debugger

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class McpToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Create a Compose UI panel and attach it to the IntelliJ tool window
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "🔌 MCP Inspector Lite",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { println("Connect clicked!") }) {
            Text("Connect to MCP Server")
        }
    }
}
