from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import numpy as np


app = FastAPI(title="Placement Prediction API")


class PredictionInput(BaseModel):
    cgpa: float
    dsaRating: int
    projectsCount: int
    internship: bool
    attendance: float
    aptitudeScore: float


class PredictionOutput(BaseModel):
    probability: float
    label: str


try:
    model = joblib.load("placement_model.pkl")
except Exception as ex:
    raise RuntimeError("Failed to load model file: placement_model.pkl") from ex


@app.post("/predict", response_model=PredictionOutput)
def predict(payload: PredictionInput) -> PredictionOutput:
    try:
        internship_value = 1 if payload.internship else 0
        features = np.array([[
            payload.cgpa,
            payload.dsaRating,
            payload.projectsCount,
            internship_value,
            payload.attendance,
            payload.aptitudeScore
        ]])

        probability = float(model.predict_proba(features)[0][1])
        label = "Ready" if probability >= 0.5 else "Needs Improvement"

        return PredictionOutput(probability=probability, label=label)
    except Exception as ex:
        raise HTTPException(status_code=500, detail="Prediction failed") from ex
