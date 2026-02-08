from flask import Flask, request, jsonify
import joblib
import numpy as np

# Initialize Flask app
app = Flask(__name__)

# Load trained model
model = joblib.load("placement_model.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    # Extract input values
    cgpa = data["cgpa"]
    dsa_rating = data["dsa_rating"]
    projects = data["projects"]
    internship = data["internship"]
    attendance = data["attendance"]
    aptitude_score = data["aptitude_score"]

    # Convert input to numpy array
    input_data = np.array([[cgpa, dsa_rating, projects,
                             internship, attendance, aptitude_score]])

    # Predict probability
    probability = model.predict_proba(input_data)[0][1]
    percentage = round(probability * 100, 2)

    # Decide status
    status = "Ready" if percentage >= 70 else "Needs Improvement"

    # Rule-based explanations (simple heuristics)
    explanations = []

    if aptitude_score is not None and aptitude_score < 60:
        explanations.append("Low aptitude score reduced readiness")

    if dsa_rating is not None and dsa_rating < 3:
        explanations.append("Low DSA rating reduced readiness")

    if cgpa is not None and cgpa < 6.5:
        explanations.append("Low CGPA reduced readiness")

    if projects is not None and projects < 1:
        explanations.append("Few projects reduced readiness")

    if internship is not None and internship == 1:
        explanations.append("Internship improved readiness")

    if attendance is not None and attendance < 75:
        explanations.append("Low attendance reduced readiness")

    # Fallback explanation
    if not explanations:
        explanations.append("No major weaknesses detected; maintain current efforts")

    return jsonify({
        "score": percentage,
        "status": status,
        "explanations": explanations
    })

if __name__ == "__main__":
    app.run(debug=True)
