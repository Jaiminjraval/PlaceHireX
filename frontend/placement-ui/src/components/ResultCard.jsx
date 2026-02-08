export default function ResultCard({ result }) {
  if (!result) {
    return <h3>No prediction yet</h3>;
  }

  return (
    <div>
      <h2>Prediction Result</h2>
      <p>
        <b>Score:</b> {result.predictionScore}%
      </p>
      <p>
        <b>Status:</b>{" "}
        <span style={{ color: result.status === "Ready" ? "green" : "red" }}>
          {result.status}
        </span>
      </p>
      {result.explanations && result.explanations.length > 0 && (
        <div>
          <h3>Explanation</h3>
          <ul>
            {result.explanations.map((e, i) => (
              <li key={i}>{e}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
