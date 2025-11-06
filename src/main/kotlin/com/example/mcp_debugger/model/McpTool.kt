package com.example.mcp_debugger.model

data class McpTool(
    val name: String,
    val description: String? = null,
    val parameters: List<McpParameter> = emptyList(),
    var tempSingleInput: String? = null
)

data class McpParameter(
    val name: String,
    val type: String,
    val required: Boolean
)