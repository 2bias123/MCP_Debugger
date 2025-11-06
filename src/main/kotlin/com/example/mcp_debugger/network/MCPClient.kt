package com.example.mcp_debugger.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.example.mcp_debugger.model.*

private val http = HttpClient.newHttpClient()

suspend fun connectAndFetchTools(base: String): List<McpTool> = withContext(Dispatchers.IO) {
    val req = HttpRequest.newBuilder(URI.create("$base/tools")).GET().build()
    val res = http.send(req, HttpResponse.BodyHandlers.ofString())
    if (res.statusCode() !in 200..299) {
        throw Exception("HTTP ${res.statusCode()} from $base/tools")
    }
    parseTools(res.body())
}

suspend fun invokeToolSuspend(base: String, toolName: String, input: String): String = withContext(Dispatchers.IO) {
    val body = """{"tool":"$toolName","input":$input}"""  // 👈 no quotes around $input
    val req = HttpRequest.newBuilder(URI.create("$base/invoke"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()
    val res = http.send(req, HttpResponse.BodyHandlers.ofString())
    if (res.statusCode() !in 200..299) {
        """{"error":"HTTP ${res.statusCode()}"}"""
    } else res.body()
}

fun parseTools(json: String): List<McpTool> {
    val array = JSONArray(json)
    val list = mutableListOf<McpTool>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val name = obj.getString("name")
        val description = obj.optString("description", null)
        val parameters = mutableListOf<McpParameter>()

        val paramsArray = obj.optJSONArray("parameters") ?: JSONArray()
        for (j in 0 until paramsArray.length()) {
            val p = paramsArray.getJSONObject(j)
            parameters.add(
                McpParameter(
                    name = p.getString("name"),
                    type = p.optString("type", "string"),
                    required = p.optBoolean("required", false)
                )
            )
        }

        list.add(McpTool(name, description, parameters))
    }
    return list
}
