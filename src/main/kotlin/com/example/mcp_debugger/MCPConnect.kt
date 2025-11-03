package com.example.mcp_debugger

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.intellij.openapi.application.ApplicationManager
import org.json.JSONArray
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val http = HttpClient.newHttpClient()
private const val BASE = "http://localhost:8000"

fun connectToServer(
    isConnected: MutableState<Boolean>,
    tools: SnapshotStateList<String>
) {
    ApplicationManager.getApplication().executeOnPooledThread {
        try {
            val req = HttpRequest.newBuilder(URI.create("$BASE/tools"))
                .GET()
                .build()
            val res = http.send(req, HttpResponse.BodyHandlers.ofString())
            val names = parseToolNames(res.body())

            ApplicationManager.getApplication().invokeLater {
                isConnected.value = true
                tools.clear()
                tools.addAll(names)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun invokeToolSync(
    toolName: String,
    input: String
): String {
    return try {
        val body = """{"tool":"$toolName","input":"$input"}"""   // NOTE: key is "input" (no stray colon)
        val req = HttpRequest.newBuilder(URI.create("$BASE/invoke"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val res = http.send(req, HttpResponse.BodyHandlers.ofString())
        res.body()
    } catch (e: Exception) {
        """{"error":"${e.message}"}"""
    }
}

fun parseToolNames(json: String): List<String> {
    val array = JSONArray(json)
    val list = mutableListOf<String>()
    for (i in 0 until array.length()) {
        list.add(array.getJSONObject(i).getString("name"))
    }
    return list
}
