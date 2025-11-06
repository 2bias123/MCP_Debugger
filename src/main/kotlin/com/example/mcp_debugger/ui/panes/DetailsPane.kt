package com.example.mcp_debugger.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mcp_debugger.network.invokeToolSuspend
import kotlinx.coroutines.launch
import com.example.mcp_debugger.model.*


@Composable
fun DetailsPane(
    connectionState: State<ConnectionState>,
    selectedTool: State<McpTool?>,
    result: MutableState<String?>,
    paramValues: MutableMap<String, String>
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val connected = connectionState.value is ConnectionState.Connected
    val isInvoking = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "📄 Details & Results",
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
        )

        val tool = selectedTool.value
        if (tool == null) {
            Text("Select a tool to view details.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }

        Text("Selected: ${tool.name}", color = MaterialTheme.colorScheme.onSurface)
        tool.description?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Dynamic input fields
        tool.parameters.forEach { param ->
            OutlinedTextField(
                value = paramValues[param.name] ?: "",
                onValueChange = { paramValues[param.name] = it },
                label = { Text("${param.name}${if (param.required) " *" else ""}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            enabled = connected && !isInvoking.value,
            onClick = {
                isInvoking.value = true
                scope.launch {
                    val server = (connectionState.value as ConnectionState.Connected).serverUrl
                    val inputJson = org.json.JSONObject(paramValues as Map<*, *>).toString()
                    result.value = invokeToolSuspend(server, tool.name, inputJson)
                    isInvoking.value = false
                }
            }
        ) {
            if (isInvoking.value) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text("Invoking...")
            } else {
                Text(if (connected) "Invoke" else "Connect first")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        result.value?.let {
            Text(
                "Result:",
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.secondary)
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}