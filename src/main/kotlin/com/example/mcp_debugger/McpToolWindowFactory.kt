package com.example.mcp_debugger

import androidx.compose.foundation.border
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
            Text( "Pane 1", style = MaterialTheme.typography.headlineSmall )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            Text( "Pane 2", style = MaterialTheme.typography.headlineSmall )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        ) {
            Text( "Pane 3", style = MaterialTheme.typography.headlineSmall )
        }
    }
}
