package com.example.mcp_debugger

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import org.json.JSONArray

val client = HttpClient(CIO)

fun connectToServer(
    isConnected: MutableState<Boolean>,
    tools: SnapshotStateList<String>
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response: String = client.get("http://localhost:8000/tools").bodyAsText()
            val toolNames = parseToolNames(response)

            withContext(Dispatchers.Default) {
                isConnected.value = true
                tools.clear()
                tools.addAll(toolNames)
            }
        } catch (e: Exception) {
            println("Connection failed: ${e.message}")
        }
    }
}


fun parseToolNames(json: String): List<String> {
    val array = JSONArray(json)
    val list = mutableListOf<String>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(obj.getString("name"))
    }
    return list
}
