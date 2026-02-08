import { useState } from "react";
import axios from "axios";

export default function PredictionForm({ onResult }) {
  const [form, setForm] = useState({
    cgpa: "",
    dsaRating: "",
    projects: "",
    internship: false,
    attendance: "",
    aptitudeScore: "",
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === "checkbox" ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      cgpa: parseFloat(form.cgpa),
      dsaRating: parseInt(form.dsaRating),
      projects: parseInt(form.projects),
      internship: form.internship ? true : false,
      attendance: parseInt(form.attendance),
      aptitudeScore: parseInt(form.aptitudeScore),
    };

    const res = await axios.post(
      "http://localhost:8080/api/students/predict",
      payload,
    );

    onResult(res.data);
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Student Details</h2>

      <input name="cgpa" placeholder="CGPA" onChange={handleChange} />
      <input
        name="dsaRating"
        placeholder="DSA Rating (1–5)"
        onChange={handleChange}
      />
      <input name="projects" placeholder="Projects" onChange={handleChange} />
      <input
        name="attendance"
        placeholder="Attendance %"
        onChange={handleChange}
      />
      <input
        name="aptitudeScore"
        placeholder="Aptitude Score"
        onChange={handleChange}
      />

      <label>
        Internship
        <input type="checkbox" name="internship" onChange={handleChange} />
      </label>

      <button type="submit">Predict</button>
    </form>
  );
}
