import { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import api from "../api/axiosInstance";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

export default function StudentList() {
    const [students, setStudents] = useState([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    /* ── Fetch all student profiles ───────────────────────────── */
    useEffect(() => {
        const load = async () => {
            try {
                const res = await api.get("/api/admin/students");
                setStudents(res.data);
            } catch (err) {
                setError(
                    err.response?.data?.message || "Failed to load student list."
                );
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    /* ── Filtered list ────────────────────────────────────────── */
    const filtered = useMemo(() => {
        if (!search.trim()) return students;
        const q = search.toLowerCase();
        return students.filter(
            (s) =>
                s.user?.email?.toLowerCase().includes(q) ||
                s.user?.name?.toLowerCase().includes(q)
        );
    }, [students, search]);

    /* ── Status badge ─────────────────────────────────────────── */
    const badgeClass = (label) => {
        const l = label?.toLowerCase();
        if (l === "likely placed" || l === "placed" || l === "high")
            return "bg-[#A8E6CF]/20 text-emerald-700";
        if (l === "medium" || l === "moderate")
            return "bg-[#FDE68A]/30 text-amber-700";
        return "bg-[#FCA5A5]/20 text-red-600";
    };

    /* ── Row animation variants ───────────────────────────────── */
    const rowVariants = {
        hidden: { opacity: 0, y: 10 },
        visible: (i) => ({
            opacity: 1,
            y: 0,
            transition: { delay: 0.4 + i * 0.04, duration: 0.35, ease: "easeOut" },
        }),
    };

    /* ── Loading / Error states ───────────────────────────────── */
    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#f4f4f4] font-['Inter',sans-serif]">
                <span className="text-lg font-semibold text-black/40 animate-pulse">
                    Loading students…
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

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] font-['Inter',sans-serif] overflow-hidden">
            {/* ── Drifting smoky background blobs ─────────────────── */}
            <div className="pointer-events-none fixed inset-0">
                <DriftingBlob className="absolute -top-32 -left-24 h-[480px] w-[480px] rounded-full bg-black/10 blur-[130px]" duration={25} />
                <DriftingBlob className="absolute top-[60%] right-[-6%] h-[420px] w-[420px] rounded-full bg-black/[0.06] blur-[120px]" duration={32} />
                <DriftingBlob className="absolute bottom-[-6%] left-[45%] h-[360px] w-[360px] rounded-full bg-black/[0.07] blur-[100px]" duration={28} />
            </div>

            {/* ── Content ─────────────────────────────────────────── */}
            <div className="relative z-10 mx-auto max-w-6xl px-6 py-12">
                {/* Header */}
                <TextReveal>
                    <h1 className="text-4xl font-extrabold tracking-tight text-black">
                        Student Directory
                    </h1>
                </TextReveal>
                <TextReveal delay={0.1}>
                    <p className="mt-1 text-sm font-medium text-black/40">
                        {students.length} student{students.length !== 1 && "s"} enrolled
                    </p>
                </TextReveal>

                {/* ── Search bar ────────────────────────────────────── */}
                <TextReveal delay={0.2} className="mt-8">
                    <input
                        type="text"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Search by name or email…"
                        className="w-full max-w-md rounded-xl border border-black/10 bg-white/80 px-5 py-3 text-sm text-black placeholder-black/30 outline-none transition-all duration-200 focus:border-[#A8E6CF] focus:ring-2 focus:ring-[#A8E6CF]/40"
                    />
                </TextReveal>

                {/* ── Table ─────────────────────────────────────────── */}
                <CardReveal delay={0.3} className="mt-6 overflow-hidden rounded-3xl bg-white/60 shadow-xl backdrop-blur-xl">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                                <tr className="border-b border-black/5">
                                    <th className="px-6 py-5 text-xs font-bold uppercase tracking-widest text-black/40">
                                        Name / Email
                                    </th>
                                    <th className="px-6 py-5 text-xs font-bold uppercase tracking-widest text-black/40">
                                        CGPA
                                    </th>
                                    <th className="px-6 py-5 text-xs font-bold uppercase tracking-widest text-black/40">
                                        Readiness Score
                                    </th>
                                    <th className="px-6 py-5 text-xs font-bold uppercase tracking-widest text-black/40">
                                        Status
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.length === 0 ? (
                                    <tr>
                                        <td
                                            colSpan={4}
                                            className="px-6 py-16 text-center text-black/30"
                                        >
                                            No students match your search.
                                        </td>
                                    </tr>
                                ) : (
                                    filtered.map((s, i) => (
                                        <motion.tr
                                            key={s.id}
                                            custom={i}
                                            initial="hidden"
                                            animate="visible"
                                            variants={rowVariants}
                                            className="border-b border-black/[0.03] transition-colors duration-150 hover:bg-[#A8E6CF]/[0.06]"
                                        >
                                            {/* Name / Email */}
                                            <td className="px-6 py-4">
                                                <p className="font-semibold text-black">
                                                    {s.user?.name || "—"}
                                                </p>
                                                <p className="text-xs text-black/40">
                                                    {s.user?.email || "—"}
                                                </p>
                                            </td>

                                            {/* CGPA */}
                                            <td className="px-6 py-4 font-medium text-black">
                                                {s.cgpa ?? "—"}
                                            </td>

                                            {/* Readiness Score */}
                                            <td className="px-6 py-4 font-medium text-black">
                                                {s.readinessScore != null
                                                    ? `${Math.round(s.readinessScore * 100) / 100}%`
                                                    : "—"}
                                            </td>

                                            {/* Status badge */}
                                            <td className="px-6 py-4">
                                                <span
                                                    className={`inline-block rounded-full px-3 py-1 text-xs font-bold ${badgeClass(
                                                        s.readinessLabel
                                                    )}`}
                                                >
                                                    {s.readinessLabel || "N/A"}
                                                </span>
                                            </td>
                                        </motion.tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </CardReveal>
            </div>
        </PageTransition>
    );
}
