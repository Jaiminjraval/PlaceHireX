import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
import joblib

# 1. Load dataset
data = pd.read_csv("dataset/student_data.csv")

# 2. Separate features and target
X = data.drop("placed", axis=1)
y = data["placed"]

# 3. Split into training and testing
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# 4. Train Logistic Regression model
model = LogisticRegression()
model.fit(X_train, y_train)

# 5. Test model
y_pred = model.predict(X_test)
accuracy = accuracy_score(y_test, y_pred)

print(f"Model Accuracy: {accuracy * 100:.2f}%")

# 6. Save trained model
joblib.dump(model, "placement_model.pkl")

print("Model saved as placement_model.pkl")
