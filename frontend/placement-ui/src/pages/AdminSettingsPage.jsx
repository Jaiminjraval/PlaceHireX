import { useState } from "react";
import { motion } from "framer-motion";
import api from "../api/axiosInstance";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

export default function AdminSettingsPage() {
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState("");

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!file) return setError("Please select a CSV file.");
        setError("");
        setResult(null);
        setUploading(true);

        try {
            const formData = new FormData();
            formData.append("file", file);

            const { data } = await api.post(
                "/api/admin/models/upload-dataset",
                formData,
                { headers: { "Content-Type": "multipart/form-data" } }
            );

            // data may be a string or an object
            if (typeof data === "string") {
                setResult({ message: data });
            } else {
                setResult(data);
            }
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data ||
                "Upload failed. Make sure the ML service is running on port 8000."
            );
        } finally {
            setUploading(false);
        }
    };

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] overflow-hidden font-['Inter',sans-serif]">
            {/* ── Blobs ──────────────────────────────────────────── */}
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <DriftingBlob className="absolute -top-32 -left-28 h-[500px] w-[500px] rounded-full bg-black/10 blur-[120px]" duration={26} />
                <DriftingBlob className="absolute bottom-[-5%] right-[-8%] h-[450px] w-[450px] rounded-full bg-black/[0.07] blur-[130px]" duration={30} />
            </div>

            <div className="relative z-10 mx-auto max-w-3xl px-6 py-16">
                {/* Header */}
                <TextReveal delay={0.15}>
                    <h1 className="text-4xl font-extrabold tracking-tight text-black">
                        Settings
                    </h1>
                </TextReveal>
                <TextReveal delay={0.2}>
                    <p className="mt-2 text-base font-medium text-black/40">
                        Manage your ML model and dataset
                    </p>
                </TextReveal>

                {/* ── Upload Dataset ─────────────────────────────── */}
                <CardReveal delay={0.3} className="mt-10 rounded-3xl border border-black/5 bg-white/60 px-8 py-8 shadow-xl backdrop-blur-xl">
                    <h2 className="text-xl font-bold text-black">
                        Retrain ML Model
                    </h2>
                    <p className="mt-1 text-sm text-black/40">
                        Upload a CSV dataset to retrain the placement prediction model.
                    </p>

                    {/* CSV format hint */}
                    <div className="mt-4 rounded-xl bg-black/[0.03] px-4 py-3">
                        <p className="text-xs font-semibold uppercase tracking-widest text-black/30 mb-2">
                            Required CSV Format
                        </p>
                        <code className="block text-xs text-black/60 leading-relaxed">
                            cgpa,dsaRating,projectsCount,internship,attendance,aptitudeScore,placed<br />
                            8.5,4,3,1,88,80,1<br />
                            6.8,2,1,0,65,55,0
                        </code>
                    </div>

                    <form onSubmit={handleUpload} className="mt-6 space-y-4">
                        {/* File input */}
                        <label className="flex cursor-pointer items-center gap-4 rounded-xl border-2 border-dashed border-black/10 bg-white/50 px-6 py-6 transition-colors hover:border-[#A8E6CF]">
                            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#A8E6CF]/20">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-[#5cc99a]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                </svg>
                            </div>
                            <div>
                                <p className="text-sm font-semibold text-black">
                                    {file ? file.name : "Choose a CSV file"}
                                </p>
                                <p className="text-xs text-black/40">
                                    {file
                                        ? `${(file.size / 1024).toFixed(1)} KB`
                                        : "Click to browse or drag and drop"}
                                </p>
                            </div>
                            <input
                                type="file"
                                accept=".csv"
                                className="hidden"
                                onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                            />
                        </label>

                        <motion.button
                            type="submit"
                            disabled={uploading || !file}
                            whileHover={{ scale: 1.02 }}
                            whileTap={{ scale: 0.97 }}
                            className="group relative flex w-full items-center justify-center overflow-hidden rounded-full shadow-md transition-shadow duration-300 hover:shadow-lg disabled:opacity-60 disabled:pointer-events-none"
                        >
                            <span className="flex h-full w-1/2 items-center justify-center bg-black py-3.5 text-sm font-bold tracking-wide text-white transition-colors duration-300 group-hover:bg-black/85">
                                {uploading ? "Uploading & Training…" : "Upload & Retrain"}
                            </span>
                            <span className="flex h-full w-1/2 items-center justify-center bg-[#A8E6CF] py-3.5 text-sm font-bold tracking-wide text-black transition-colors duration-300 group-hover:bg-[#90dbbe]">
                                ↑
                            </span>
                        </motion.button>
                    </form>

                    {/* Error */}
                    {error && (
                        <motion.div
                            initial={{ opacity: 0, y: -8 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-600"
                        >
                            {typeof error === 'string' ? error : JSON.stringify(error)}
                        </motion.div>
                    )}

                    {/* Success result */}
                    {result && (
                        <motion.div
                            initial={{ opacity: 0, y: -8 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="mt-4 rounded-xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700"
                        >
                            <p className="font-bold">✓ Model retrained successfully</p>
                            {result.accuracy && (
                                <p className="mt-1">
                                    Accuracy: <span className="font-bold">{(result.accuracy * 100).toFixed(1)}%</span>
                                </p>
                            )}
                            {result.message && <p className="mt-1">{result.message}</p>}
                        </motion.div>
                    )}
                </CardReveal>
            </div>
        </PageTransition>
    );
}
