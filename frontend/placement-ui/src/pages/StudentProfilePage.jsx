import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import api from "../api/axiosInstance";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

export default function StudentProfilePage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [isNew, setIsNew] = useState(true);

    const [form, setForm] = useState({
        cgpa: "",
        dsaRating: "",
        projectsCount: "",
        internship: false,
        attendance: "",
        aptitudeScore: "",
    });

    /* ── Load existing profile on mount ──────────────────────── */
    useEffect(() => {
        const load = async () => {
            try {
                const { data } = await api.get("/api/students/profile");
                setForm({
                    cgpa: data.cgpa ?? "",
                    dsaRating: data.dsaRating ?? "",
                    projectsCount: data.projectsCount ?? "",
                    internship: data.internship ?? false,
                    attendance: data.attendance ?? "",
                    aptitudeScore: data.aptitudeScore ?? "",
                });
                setIsNew(false);
            } catch {
                // 404 or other → new profile, leave defaults
                setIsNew(true);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");
        setSaving(true);

        try {
            await api.post("/api/students/profile", {
                cgpa: parseFloat(form.cgpa),
                dsaRating: parseInt(form.dsaRating),
                projectsCount: parseInt(form.projectsCount),
                internship: form.internship,
                attendance: parseFloat(form.attendance),
                aptitudeScore: parseFloat(form.aptitudeScore),
            });

            setSuccess("Profile saved! Redirecting to dashboard…");
            setTimeout(() => navigate("/student/dashboard"), 1200);
        } catch (err) {
            setError(
                err.response?.data?.message || "Failed to save profile."
            );
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#f4f4f4] font-['Inter',sans-serif]">
                <span className="text-lg font-semibold text-black/40 animate-pulse">
                    Loading profile…
                </span>
            </div>
        );
    }

    /* ── Input helper ────────────────────────────────────────── */
    const inputClass =
        "w-full rounded-xl border border-black/10 bg-white/80 px-4 py-3 text-sm text-black placeholder-black/30 outline-none transition-all duration-200 focus:border-[#A8E6CF] focus:ring-2 focus:ring-[#A8E6CF]/40";

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] flex items-center justify-center overflow-hidden font-['Inter',sans-serif]">
            {/* ── Blobs ──────────────────────────────────────────── */}
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <DriftingBlob className="absolute -top-32 -right-28 h-[500px] w-[500px] rounded-full bg-black/10 blur-[120px]" duration={24} />
                <DriftingBlob className="absolute top-[60%] -left-20 h-[460px] w-[460px] rounded-full bg-black/[0.06] blur-[120px]" duration={30} />
                <DriftingBlob className="absolute bottom-[-8%] right-[35%] h-[380px] w-[380px] rounded-full bg-black/[0.08] blur-[110px]" duration={22} />
            </div>

            {/* ── Profile card ──────────────────────────────────── */}
            <CardReveal delay={0.15} className="relative z-10 w-full max-w-lg rounded-3xl border border-black/5 bg-white/60 px-10 py-12 shadow-2xl backdrop-blur-xl">
                <TextReveal delay={0.25}>
                    <h1 className="text-center text-3xl font-extrabold tracking-tight text-black">
                        {isNew ? "Setup Your Profile" : "Edit Profile"}
                    </h1>
                </TextReveal>
                <TextReveal delay={0.3}>
                    <p className="mt-2 text-center text-sm font-medium text-black/40">
                        {isNew
                            ? "Fill in your academic details to get your first prediction"
                            : "Update your details and get a new prediction"}
                    </p>
                </TextReveal>

                {/* Banners */}
                {error && (
                    <motion.div
                        initial={{ opacity: 0, y: -8 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="mt-5 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-600"
                    >
                        {error}
                    </motion.div>
                )}
                {success && (
                    <motion.div
                        initial={{ opacity: 0, y: -8 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="mt-5 rounded-xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700"
                    >
                        {success}
                    </motion.div>
                )}

                {/* Form */}
                <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                    {/* Row 1: CGPA + DSA */}
                    <TextReveal delay={0.35}>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label htmlFor="prof-cgpa" className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40">
                                    CGPA
                                </label>
                                <input
                                    id="prof-cgpa"
                                    name="cgpa"
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    max="10"
                                    required
                                    value={form.cgpa}
                                    onChange={handleChange}
                                    placeholder="8.5"
                                    className={inputClass}
                                />
                            </div>
                            <div>
                                <label htmlFor="prof-dsa" className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40">
                                    DSA Rating (1–5)
                                </label>
                                <input
                                    id="prof-dsa"
                                    name="dsaRating"
                                    type="number"
                                    min="1"
                                    max="5"
                                    required
                                    value={form.dsaRating}
                                    onChange={handleChange}
                                    placeholder="4"
                                    className={inputClass}
                                />
                            </div>
                        </div>
                    </TextReveal>

                    {/* Row 2: Projects + Attendance */}
                    <TextReveal delay={0.4}>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label htmlFor="prof-projects" className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40">
                                    Projects Count
                                </label>
                                <input
                                    id="prof-projects"
                                    name="projectsCount"
                                    type="number"
                                    min="0"
                                    required
                                    value={form.projectsCount}
                                    onChange={handleChange}
                                    placeholder="3"
                                    className={inputClass}
                                />
                            </div>
                            <div>
                                <label htmlFor="prof-attendance" className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40">
                                    Attendance %
                                </label>
                                <input
                                    id="prof-attendance"
                                    name="attendance"
                                    type="number"
                                    min="0"
                                    max="100"
                                    required
                                    value={form.attendance}
                                    onChange={handleChange}
                                    placeholder="88"
                                    className={inputClass}
                                />
                            </div>
                        </div>
                    </TextReveal>

                    {/* Row 3: Aptitude + Internship */}
                    <TextReveal delay={0.45}>
                        <div className="grid grid-cols-2 gap-4 items-end">
                            <div>
                                <label htmlFor="prof-aptitude" className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40">
                                    Aptitude Score
                                </label>
                                <input
                                    id="prof-aptitude"
                                    name="aptitudeScore"
                                    type="number"
                                    min="0"
                                    max="100"
                                    required
                                    value={form.aptitudeScore}
                                    onChange={handleChange}
                                    placeholder="80"
                                    className={inputClass}
                                />
                            </div>
                            <div className="flex items-center gap-3 rounded-xl border border-black/10 bg-white/80 px-4 py-3">
                                <input
                                    id="prof-internship"
                                    name="internship"
                                    type="checkbox"
                                    checked={form.internship}
                                    onChange={handleChange}
                                    className="h-4 w-4 accent-[#A8E6CF]"
                                />
                                <label htmlFor="prof-internship" className="text-sm font-medium text-black/70">
                                    Internship
                                </label>
                            </div>
                        </div>
                    </TextReveal>

                    {/* Submit */}
                    <TextReveal delay={0.5}>
                        <motion.button
                            type="submit"
                            disabled={saving}
                            whileHover={{ scale: 1.02 }}
                            whileTap={{ scale: 0.97 }}
                            className="group relative flex w-full items-center justify-center overflow-hidden rounded-full shadow-md transition-shadow duration-300 hover:shadow-lg disabled:opacity-60 disabled:pointer-events-none"
                        >
                            <span className="flex h-full w-1/2 items-center justify-center bg-black py-3.5 text-sm font-bold tracking-wide text-white transition-colors duration-300 group-hover:bg-black/85">
                                {saving ? "Saving…" : isNew ? "Create Profile" : "Update Profile"}
                            </span>
                            <span className="flex h-full w-1/2 items-center justify-center bg-[#A8E6CF] py-3.5 text-sm font-bold tracking-wide text-black transition-colors duration-300 group-hover:bg-[#90dbbe]">
                                →
                            </span>
                        </motion.button>
                    </TextReveal>
                </form>
            </CardReveal>
        </PageTransition>
    );
}
