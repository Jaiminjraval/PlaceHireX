from fastapi import FastAPI, HTTPException, UploadFile, File
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


@app.post("/retrain")
async def retrain(file: UploadFile = File(...)):
    global model
    try:
        if not file.filename.endswith('.csv'):
             raise HTTPException(status_code=400, detail="File must be a CSV")
        
        # Read file
        import pandas as pd
        from sklearn.model_selection import train_test_split
        from sklearn.linear_model import LogisticRegression
        from sklearn.metrics import accuracy_score
        import io

        content = await file.read()
        df = pd.read_csv(io.BytesIO(content))

        # Check required columns
        required_columns = {"cgpa", "dsaRating", "projectsCount", "internship", "attendance", "aptitudeScore", "placed"}
        if not required_columns.issubset(df.columns):
             missing = required_columns - set(df.columns)
             raise HTTPException(status_code=400, detail=f"Missing columns: {missing}")

        # Preprocessing
        # 'internship' might be boolean or int in CSV. Ensure consistent handling if needed.
        # Assuming standard dataset where 'placed' is target 0/1

        X = df.drop("placed", axis=1)
        y = df["placed"]
        
        # Ensure 'internship' is numeric if it's boolean in CSV (pandas usually handles this but good to be safe)
        if X['internship'].dtype == bool:
            X['internship'] = X['internship'].astype(int)

        # Feature order enforcement for safety (must match predict endpoint)
        feature_order = ["cgpa", "dsaRating", "projectsCount", "internship", "attendance", "aptitudeScore"]
        X = X[feature_order]

        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

        new_model = LogisticRegression()
        new_model.fit(X_train, y_train)

        y_pred = new_model.predict(X_test)
        accuracy = accuracy_score(y_test, y_pred)

        # Save model
        joblib.dump(new_model, "placement_model.pkl")
        
        # Reload model
        model = new_model

        return {"message": "Model retrained successfully", "accuracy": accuracy}

    except HTTPException as he:
        raise he
    except Exception as ex:
        raise HTTPException(status_code=500, detail=f"Retraining failed: {str(ex)}") from ex
