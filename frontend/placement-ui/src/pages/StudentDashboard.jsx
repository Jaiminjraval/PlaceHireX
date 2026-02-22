import { useEffect, useState } from "react";
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import api from "../api/axiosInstance";
import CircularProgress from "../components/CircularProgress";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

export default function StudentDashboard() {
    const [profile, setProfile] = useState(null);
    const [prediction, setPrediction] = useState(null);
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    /* ── Fetch data on mount ──────────────────────────────────── */
    useEffect(() => {
        const load = async () => {
            try {
                const [profileRes, historyRes] = await Promise.all([
                    api.get("/api/students/profile"),
                    api.get("/api/students/history"),
                ]);
                setProfile(profileRes.data);
                setHistory(historyRes.data);

                // Run a fresh prediction
                const predRes = await api.post("/api/students/predict");
                setPrediction(predRes.data);
            } catch (err) {
                setError(
                    err.response?.data?.message ||
                    "Failed to load dashboard data. Please complete your profile first."
                );
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    /* ── Chart data (chronological) ───────────────────────────── */
    const chartData = [...history]
        .reverse()
        .map((h) => ({
            date: new Date(h.timestamp).toLocaleDateString("en-IN", {
                day: "2-digit",
                month: "short",
            }),
            score: Math.round(h.predictionScore * 100),
        }));

    /* ── Status badge colour ──────────────────────────────────── */
    const badgeClass = (label) => {
        const l = label?.toLowerCase();
        if (l === "placed" || l === "high")
            return "bg-[#A8E6CF]/20 text-emerald-700";
        if (l === "medium" || l === "moderate")
            return "bg-[#FDE68A]/30 text-amber-700";
        return "bg-[#FCA5A5]/20 text-red-600";
    };

    /* ── Skeleton / error states ──────────────────────────────── */
    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#f4f4f4] font-['Inter',sans-serif]">
                <span className="text-lg font-semibold text-black/40 animate-pulse">
                    Loading dashboard…
                </span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#f4f4f4] font-['Inter',sans-serif]">
                <div className="max-w-md rounded-2xl bg-white/60 px-8 py-10 text-center shadow-lg backdrop-blur-xl">
                    <p className="text-base font-medium text-red-600">{error}</p>
                </div>
            </div>
        );
    }

    const score = prediction ? Math.round(prediction.probability * 100) : 0;

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] font-['Inter',sans-serif] overflow-hidden">
            {/* ── Drifting smoky background blobs ─────────────────── */}
            <div className="pointer-events-none fixed inset-0">
                <DriftingBlob className="absolute -top-40 -left-32 h-[520px] w-[520px] rounded-full bg-black/10 blur-[140px]" duration={24} />
                <DriftingBlob className="absolute top-1/3 right-[-8%] h-[480px] w-[480px] rounded-full bg-black/[0.06] blur-[120px]" duration={30} />
                <DriftingBlob className="absolute bottom-[-5%] left-[40%] h-[400px] w-[400px] rounded-full bg-black/[0.08] blur-[110px]" duration={26} />
            </div>

            {/* ── Content wrapper ──────────────────────────────────── */}
            <div className="relative z-10 mx-auto max-w-6xl px-6 py-12">
                {/* Header */}
                <TextReveal>
                    <h1 className="text-4xl font-extrabold tracking-tight text-black">
                        Dashboard
                    </h1>
                </TextReveal>
                <TextReveal delay={0.1}>
                    <p className="mt-1 text-sm font-medium text-black/40">
                        Welcome back,{" "}
                        <span className="text-black/70">{profile?.user?.email ?? "Student"}</span>
                    </p>
                </TextReveal>

                {/* ────────────────── Top row ────────────────────────── */}
                <div className="mt-10 grid grid-cols-1 gap-6 md:grid-cols-3">
                    {/* Card – Placement Score */}
                    <CardReveal delay={0.15} className="flex flex-col items-center justify-center rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <CircularProgress percentage={score} />
                        <p className="mt-4 text-xs font-semibold uppercase tracking-widest text-black/40">
                            Placement Score
                        </p>
                    </CardReveal>

                    {/* Card – Status & Label */}
                    <CardReveal delay={0.25} className="flex flex-col items-center justify-center gap-4 rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <span
                            className={`rounded-full px-5 py-2 text-sm font-bold ${badgeClass(
                                prediction?.label
                            )}`}
                        >
                            {prediction?.label ?? "N/A"}
                        </span>
                        <p className="text-center text-xs font-medium text-black/40">
                            Current prediction status
                        </p>
                    </CardReveal>

                    {/* Card – Profile snapshot */}
                    <CardReveal delay={0.35} className="rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-black/40">
                            Profile Snapshot
                        </h2>
                        <ul className="space-y-2 text-sm text-black/70">
                            <li className="flex justify-between">
                                <span className="text-black/40">CGPA</span>
                                <span className="font-semibold text-black">{profile?.cgpa ?? "—"}</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-black/40">DSA Rating</span>
                                <span className="font-semibold text-black">{profile?.dsaRating ?? "—"}</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-black/40">Projects</span>
                                <span className="font-semibold text-black">{profile?.projectsCount ?? "—"}</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-black/40">Internship</span>
                                <span className="font-semibold text-black">{profile?.internship ? "Yes" : "No"}</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-black/40">Attendance</span>
                                <span className="font-semibold text-black">{profile?.attendance ?? "—"}%</span>
                            </li>
                            <li className="flex justify-between">
                                <span className="text-black/40">Aptitude</span>
                                <span className="font-semibold text-black">{profile?.aptitudeScore ?? "—"}</span>
                            </li>
                        </ul>
                    </CardReveal>
                </div>

                {/* ────────────────── Middle row ─────────────────────── */}
                <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
                    {/* Card – Explanations */}
                    <CardReveal delay={0.4} className="rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-black/40">
                            Explanations
                        </h2>
                        {prediction?.explanations?.length ? (
                            <ul className="space-y-3">
                                {prediction.explanations.map((text, i) => (
                                    <li
                                        key={i}
                                        className="flex items-start gap-3 text-sm text-black/70"
                                    >
                                        <span className="mt-1 inline-block h-2 w-2 flex-shrink-0 rounded-full bg-[#A8E6CF]" />
                                        {text}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-sm text-black/30">No explanations available.</p>
                        )}
                    </CardReveal>

                    {/* Card – Recommendations */}
                    <CardReveal delay={0.5} className="rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-black/40">
                            Recommendations
                        </h2>
                        {prediction?.recommendations?.length ? (
                            <ol className="list-inside list-decimal space-y-3">
                                {prediction.recommendations.map((text, i) => (
                                    <li key={i} className="text-sm text-black/70">
                                        {text}
                                    </li>
                                ))}
                            </ol>
                        ) : (
                            <p className="text-sm text-black/30">No recommendations yet.</p>
                        )}
                    </CardReveal>
                </div>

                {/* ────────────────── Chart row ──────────────────────── */}
                <CardReveal delay={0.55} className="mt-6 rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                    <h2 className="mb-6 text-sm font-bold uppercase tracking-widest text-black/40">
                        Prediction History
                    </h2>

                    {chartData.length > 1 ? (
                        <ResponsiveContainer width="100%" height={280}>
                            <LineChart data={chartData}>
                                <CartesianGrid
                                    strokeDasharray="4 4"
                                    stroke="#e5e7eb"
                                    vertical={false}
                                />
                                <XAxis
                                    dataKey="date"
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    domain={[0, 100]}
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                    width={36}
                                />
                                <Tooltip
                                    contentStyle={{
                                        borderRadius: "12px",
                                        border: "none",
                                        boxShadow: "0 8px 30px rgba(0,0,0,.08)",
                                        fontSize: "13px",
                                    }}
                                    formatter={(value) => [`${value}%`, "Score"]}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="score"
                                    stroke="#A8E6CF"
                                    strokeWidth={3}
                                    dot={{ r: 4, fill: "#A8E6CF", strokeWidth: 0 }}
                                    activeDot={{ r: 6, fill: "#6dd4a8", strokeWidth: 0 }}
                                    isAnimationActive={true}
                                    animationDuration={1200}
                                    animationEasing="ease-out"
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    ) : (
                        <p className="py-16 text-center text-sm text-black/30">
                            Not enough data to show a trend. Run more predictions!
                        </p>
                    )}
                </CardReveal>
            </div>
        </PageTransition>
    );
}
