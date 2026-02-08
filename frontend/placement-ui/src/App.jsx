import { useState } from "react";
import PredictionForm from "./components/PredictionForm";
import ResultCard from "./components/ResultCard";

function App() {
  const [result, setResult] = useState(null);

  return (
    <div style={{ display: "flex", gap: "40px", padding: "40px" }}>
      <PredictionForm onResult={setResult} />
      <ResultCard result={result} />
    </div>
  );
}

export default App;
