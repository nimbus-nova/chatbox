from fastapi import FastAPI
from fastapi.responses import RedirectResponse
from typing import Optional
from pydantic import BaseModel

class CommandRequest(BaseModel):
    cmd: Optional[str] = None

app = FastAPI()

@app.post("/cmd")
def on_cmd_received(request: CommandRequest):
    if request.cmd is None:
        return {"status": "failed"}
    return {"status": "success", "request": f"{request.model_dump_json()}"}

@app.get("/ping")
def on_ping():
    return {"status": "success", "message": "pong"}

@app.get("/")
def redirect_to_docs():
    return RedirectResponse(url="/docs")


# main file to get the server started
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)