package com.example.mcp_debugger.model

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val serverUrl: String) : ConnectionState()
    data class Error(val serverUrlTried: String, val message: String) : ConnectionState()
}