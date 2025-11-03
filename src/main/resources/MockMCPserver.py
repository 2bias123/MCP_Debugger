from flask import Flask, request, jsonify
app = Flask(__name__)

tools = [{"name": "echo", "description": "Echo input text"}]

@app.get("/tools")
def get_tools():
    return jsonify(tools)

@app.post("/invoke")
def invoke_tool():
    data = request.json
    if data.get("tool") == "echo":
        return jsonify({"result": f"ECHO: {data.get('input')}"})
    return jsonify({"error": "Unknown tool"}), 400

if __name__ == "__main__":
    app.run(port=8000)
