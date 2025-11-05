from flask import Flask, jsonify, request

app = Flask(__name__)

# --- Mock MCP tool definitions ---
TOOLS = [
    {
        "name": "echo",
        "description": "Echo back input text.",
        "parameters": [
            {"name": "text", "type": "string", "required": True}
        ],
    },
    {
        "name": "reverse",
        "description": "Reverse a given string.",
        "parameters": [
            {"name": "text", "type": "string", "required": True}
        ],
    },
    {
        "name": "combine",
        "description": "Combine two strings with a separator.",
        "parameters": [
            {"name": "first", "type": "string", "required": True},
            {"name": "second", "type": "string", "required": True},
            {"name": "separator", "type": "string", "required": False},
        ],
    },
]

# --- API endpoints ---
@app.route("/tools", methods=["GET"])
def get_tools():
    """Return all available tools and their parameter metadata."""
    return jsonify(TOOLS)

@app.route("/invoke", methods=["POST"])
def invoke_tool():
    """Invoke a tool with provided parameters."""
    data = request.get_json(force=True)
    tool_name = data.get("tool")
    input_params = data.get("input", {})

    # Defensive fallback
    if not tool_name:
        return jsonify({"error": "Missing tool name"}), 400

    # Find the tool
    tool = next((t for t in TOOLS if t["name"] == tool_name), None)
    if not tool:
        return jsonify({"error": f"Unknown tool '{tool_name}'"}), 400

    # Execute mock logic
    if tool_name == "echo":
        text = input_params.get("text", "")
        return jsonify({"result": text})

    elif tool_name == "reverse":
        text = input_params.get("text", "")
        return jsonify({"result": text[::-1]})

    elif tool_name == "combine":
        first = input_params.get("first", "")
        second = input_params.get("second", "")
        sep = input_params.get("separator", " ")
        return jsonify({"result": f"{first}{sep}{second}"})

    else:
        return jsonify({"error": "No handler for this tool"}), 400


if __name__ == "__main__":
    app.run(port=8000, debug=True)
