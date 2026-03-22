import { useEffect, useState } from "react";
import {
    PieChart,
    Pie,
    Cell,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
} from "recharts";
import api from "../api/axiosInstance";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

/* ── Colour palette ─────────────────────────────────────────── */
const MINT = "#A8E6CF";
const BLACK = "#1a1a1a";
const PIE_COLORS = [MINT, BLACK];

export default function AdminDashboard() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                const res = await api.get("/api/admin/analytics");
                setData(res.data);
            } catch (err) {
                setError(
                    err.response?.data?.message || "Failed to load analytics data."
                );
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    /* ── Loading / Error states ───────────────────────────────── */
    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#f4f4f4] font-['Inter',sans-serif]">
                <span className="text-lg font-semibold text-black/40 animate-pulse">
                    Loading analytics…
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

    /* ── Derived chart data ───────────────────────────────────── */
    const pieData = [
        { name: "Ready", value: data.readyStudentsCount },
        { name: "Not Ready", value: data.notReadyStudentsCount },
    ];

    const barData = [
        { name: "Internship", ready: data.internshipReadyCount },
        { name: "No Internship", ready: data.nonInternshipReadyCount },
    ];

    const avgScore = Math.round(data.averageReadinessScore * 100) / 100;
    const avgCGPA = Math.round(data.averageCGPA * 100) / 100;

    /* ── Custom pie label ─────────────────────────────────────── */
    const renderPieLabel = ({ name, percent }) =>
        `${name} ${(percent * 100).toFixed(0)}%`;

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] font-['Inter',sans-serif] overflow-hidden">
            {/* ── Drifting smoky background blobs ─────────────────── */}
            <div className="pointer-events-none fixed inset-0">
                <DriftingBlob className="absolute -top-36 -right-28 h-[500px] w-[500px] rounded-full bg-black/10 blur-[140px]" duration={26} />
                <DriftingBlob className="absolute top-[55%] -left-20 h-[460px] w-[460px] rounded-full bg-black/[0.06] blur-[120px]" duration={30} />
                <DriftingBlob className="absolute bottom-[-8%] right-[30%] h-[380px] w-[380px] rounded-full bg-black/[0.08] blur-[110px]" duration={22} />
            </div>

            {/* ── Content ─────────────────────────────────────────── */}
            <div className="relative z-10 mx-auto max-w-6xl px-6 py-12">
                {/* Header */}
                <TextReveal>
                    <h1 className="text-4xl font-extrabold tracking-tight text-black">
                        Admin Analytics
                    </h1>
                </TextReveal>
                <TextReveal delay={0.1}>
                    <p className="mt-1 text-sm font-medium text-black/40">
                        Campus-wide placement readiness at a glance
                    </p>
                </TextReveal>

                {/* ── Stat cards row ────────────────────────────────── */}
                <div className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
                    <StatCard label="Total Students" value={data.totalStudents} delay={0.15} />
                    <StatCard label="Ready" value={data.readyStudentsCount} accent delay={0.22} />
                    <StatCard label="Avg Score" value={`${avgScore}%`} delay={0.29} />
                    <StatCard label="Avg CGPA" value={avgCGPA} delay={0.36} />
                </div>

                {/* ── Charts row ────────────────────────────────────── */}
                <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
                    {/* Pie Chart – Ready vs Not Ready */}
                    <CardReveal delay={0.4} className="rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-6 text-sm font-bold uppercase tracking-widest text-black/40">
                            Readiness Split
                        </h2>
                        <ResponsiveContainer width="100%" height={280}>
                            <PieChart>
                                <Pie
                                    data={pieData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={65}
                                    outerRadius={105}
                                    paddingAngle={4}
                                    dataKey="value"
                                    label={renderPieLabel}
                                    stroke="none"
                                    isAnimationActive={true}
                                    animationDuration={1000}
                                    animationEasing="ease-out"
                                >
                                    {pieData.map((_, i) => (
                                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{
                                        borderRadius: "12px",
                                        border: "none",
                                        boxShadow: "0 8px 30px rgba(0,0,0,.08)",
                                        fontSize: "13px",
                                    }}
                                />
                                <Legend
                                    iconType="circle"
                                    wrapperStyle={{ fontSize: "13px", color: "#6b7280" }}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    </CardReveal>

                    {/* Bar Chart – Internship Ready vs Non-Internship Ready */}
                    <CardReveal delay={0.5} className="rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-6 text-sm font-bold uppercase tracking-widest text-black/40">
                            Internship vs Readiness
                        </h2>
                        <ResponsiveContainer width="100%" height={280}>
                            <BarChart data={barData} barSize={48}>
                                <CartesianGrid
                                    strokeDasharray="4 4"
                                    stroke="#e5e7eb"
                                    vertical={false}
                                />
                                <XAxis
                                    dataKey="name"
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                    width={36}
                                    allowDecimals={false}
                                />
                                <Tooltip
                                    contentStyle={{
                                        borderRadius: "12px",
                                        border: "none",
                                        boxShadow: "0 8px 30px rgba(0,0,0,.08)",
                                        fontSize: "13px",
                                    }}
                                    formatter={(value) => [value, "Ready"]}
                                />
                                <Bar
                                    dataKey="ready"
                                    radius={[8, 8, 0, 0]}
                                    isAnimationActive={true}
                                    animationDuration={1000}
                                    animationEasing="ease-out"
                                >
                                    <Cell fill={MINT} />
                                    <Cell fill={BLACK} />
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </CardReveal>
                </div>

                {/* ── Readiness Distribution ────────────────────────── */}
                {data.readinessDistribution && (
                    <CardReveal delay={0.6} className="mt-6 rounded-3xl bg-white/60 p-8 shadow-xl backdrop-blur-xl">
                        <h2 className="mb-6 text-sm font-bold uppercase tracking-widest text-black/40">
                            Score Distribution
                        </h2>
                        <ResponsiveContainer width="100%" height={260}>
                            <BarChart
                                data={Object.entries(data.readinessDistribution).map(
                                    ([range, count]) => ({ range, count })
                                )}
                                barSize={52}
                            >
                                <CartesianGrid
                                    strokeDasharray="4 4"
                                    stroke="#e5e7eb"
                                    vertical={false}
                                />
                                <XAxis
                                    dataKey="range"
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    tick={{ fontSize: 12, fill: "#9ca3af" }}
                                    axisLine={false}
                                    tickLine={false}
                                    width={36}
                                    allowDecimals={false}
                                />
                                <Tooltip
                                    contentStyle={{
                                        borderRadius: "12px",
                                        border: "none",
                                        boxShadow: "0 8px 30px rgba(0,0,0,.08)",
                                        fontSize: "13px",
                                    }}
                                    formatter={(value) => [value, "Students"]}
                                />
                                <Bar
                                    dataKey="count"
                                    fill={MINT}
                                    radius={[8, 8, 0, 0]}
                                    isAnimationActive={true}
                                    animationDuration={1200}
                                    animationEasing="ease-out"
                                />
                            </BarChart>
                        </ResponsiveContainer>
                    </CardReveal>
                )}
            </div>
        </PageTransition>
    );
}

/* ── Reusable stat card (local to this file) ────────────────── */
function StatCard({ label, value, accent = false, delay = 0 }) {
    return (
        <CardReveal delay={delay} className="rounded-3xl bg-white/60 p-6 shadow-xl backdrop-blur-xl">
            <p className="text-xs font-semibold uppercase tracking-widest text-black/40">
                {label}
            </p>
            <p
                className={`mt-2 text-3xl font-extrabold tracking-tight ${accent ? "text-emerald-600" : "text-black"
                    }`}
            >
                {value}
            </p>
        </CardReveal>
    );
}
