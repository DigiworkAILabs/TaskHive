from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    app_name: str = "TaskHive ML Inference Server"
    app_version: str = "1.0.0"
    host: str = "0.0.0.0"
    port: int = 8000
    debug: bool = False
    model_dir: str = "./models"
    log_level: str = "INFO"

    class Config:
        env_file = ".env"

settings = Settings()
