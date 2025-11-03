from flask import Flask, jsonify, request
app = Flask(__name__)

TOOLS = [
    {"name": "echo", "description": "Echo back input text"},
    {"name": "reverse", "description": "Reverse a given string"},
]

@app.route("/tools", methods=["GET"])
def get_tools():
    return jsonify(TOOLS)

@app.route("/invoke", methods=["POST"])
def invoke_tool():
    data = request.json
    tool = data.get("tool")
    input_text = data.get("input", "")

    if tool == "echo":
        return jsonify({"result": input_text})
    elif tool == "reverse":
        return jsonify({"result": input_text[::-1]})
    else:
        return jsonify({"error": "Unknown tool"}), 400

if __name__ == "__main__":
    app.run(port=8000)
