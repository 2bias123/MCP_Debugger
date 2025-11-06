package com.example.mcp_debugger

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class McpToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val composePanel = ComposePanel().apply {
            setContent {
                val darkColors = darkColorScheme(
                    background = Color(0xFF1E1E1E),
                    surface = Color(0xFF2C2C2C),
                    onBackground = Color(0xFFE0E0E0),
                    onSurface = Color(0xFFE0E0E0),
                    primary = Color(0xFF64B5F6),
                    secondary = Color(0xFF81C784)
                )

                MaterialTheme(colorScheme = darkColors) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        McpInspectorUI()
                    }
                }
            }
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(composePanel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}