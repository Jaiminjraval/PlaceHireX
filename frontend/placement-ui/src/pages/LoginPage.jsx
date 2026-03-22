import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { motion } from "framer-motion";
import api from "../api/axiosInstance";
import {
    PageTransition,
    DriftingBlob,
    TextReveal,
    CardReveal,
} from "../components/AnimationWrappers";

export default function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const { data } = await api.post("/api/auth/login", { email, password });

            localStorage.setItem("token", data.token);
            localStorage.setItem("role", data.role?.toUpperCase());

            // Role-based redirect
            const role = data.role?.toUpperCase();
            if (role === "ADMIN") {
                navigate("/admin/dashboard");
            } else {
                navigate("/student/dashboard");
            }
        } catch (err) {
            setError(
                err.response?.data?.message || "Invalid credentials. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <PageTransition className="relative min-h-screen w-full bg-[#f4f4f4] flex items-center justify-center overflow-hidden font-['Inter',sans-serif]">
            {/* ── Drifting smoky background blobs ──────────────────── */}
            <div className="pointer-events-none absolute inset-0 overflow-hidden">
                <DriftingBlob className="absolute -top-32 -left-40 h-[500px] w-[500px] rounded-full bg-black/10 blur-[120px]" duration={22} />
                <DriftingBlob className="absolute top-1/2 right-[-10%] h-[600px] w-[600px] rounded-full bg-black/[0.07] blur-[140px]" duration={28} />
                <DriftingBlob className="absolute bottom-[-10%] left-1/3 h-[400px] w-[400px] rounded-full bg-black/[0.08] blur-[100px]" duration={24} />
            </div>

            {/* ── Login card ─────────────────────────────────────────── */}
            <CardReveal delay={0.15} className="relative z-10 w-full max-w-md rounded-3xl border border-black/5 bg-white/60 px-10 py-14 shadow-2xl backdrop-blur-xl">
                {/* Brand */}
                <TextReveal delay={0.25}>
                    <h1 className="text-center text-4xl font-extrabold tracking-tight text-black">
                        PlaceHire<span className="text-[#A8E6CF]">X</span>
                    </h1>
                </TextReveal>
                <TextReveal delay={0.35}>
                    <p className="mt-2 text-center text-sm font-medium text-black/50">
                        Sign in to your account
                    </p>
                </TextReveal>

                {/* Error banner */}
                {error && (
                    <motion.div
                        initial={{ opacity: 0, y: -8 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="mt-6 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-600"
                    >
                        {error}
                    </motion.div>
                )}

                {/* Form */}
                <form onSubmit={handleSubmit} className="mt-8 space-y-6">
                    {/* Email ─────────────────────────────────────────────── */}
                    <TextReveal delay={0.4}>
                        <label
                            htmlFor="login-email"
                            className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40"
                        >
                            Email
                        </label>
                        <input
                            id="login-email"
                            type="email"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="you@example.com"
                            className="w-full rounded-xl border border-black/10 bg-white/80 px-4 py-3 text-sm text-black placeholder-black/30 outline-none transition-all duration-200 focus:border-[#A8E6CF] focus:ring-2 focus:ring-[#A8E6CF]/40"
                        />
                    </TextReveal>

                    {/* Password ──────────────────────────────────────────── */}
                    <TextReveal delay={0.5}>
                        <label
                            htmlFor="login-password"
                            className="mb-1.5 block text-xs font-semibold uppercase tracking-widest text-black/40"
                        >
                            Password
                        </label>
                        <input
                            id="login-password"
                            type="password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            className="w-full rounded-xl border border-black/10 bg-white/80 px-4 py-3 text-sm text-black placeholder-black/30 outline-none transition-all duration-200 focus:border-[#A8E6CF] focus:ring-2 focus:ring-[#A8E6CF]/40"
                        />
                    </TextReveal>

                    {/* Submit — pill split button ─────────────────────────── */}
                    <TextReveal delay={0.6}>
                        <motion.button
                            type="submit"
                            disabled={loading}
                            whileHover={{ scale: 1.02 }}
                            whileTap={{ scale: 0.97 }}
                            className="group relative flex w-full items-center justify-center overflow-hidden rounded-full shadow-md transition-shadow duration-300 hover:shadow-lg disabled:opacity-60 disabled:pointer-events-none"
                        >
                            {/* Dark side */}
                            <span className="flex h-full w-1/2 items-center justify-center bg-black py-3.5 text-sm font-bold tracking-wide text-white transition-colors duration-300 group-hover:bg-black/85">
                                {loading ? "Signing in…" : "Sign In"}
                            </span>
                            {/* Mint side */}
                            <span className="flex h-full w-1/2 items-center justify-center bg-[#A8E6CF] py-3.5 text-sm font-bold tracking-wide text-black transition-colors duration-300 group-hover:bg-[#90dbbe]">
                                →
                            </span>
                        </motion.button>
                    </TextReveal>
                </form>

                {/* Link to register */}
                <TextReveal delay={0.7}>
                    <p className="mt-6 text-center text-sm text-black/40">
                        Don&apos;t have an account?{" "}
                        <Link to="/register" className="font-semibold text-black hover:text-[#A8E6CF] transition-colors">
                            Register
                        </Link>
                    </p>
                </TextReveal>
            </CardReveal>
        </PageTransition>
    );
}
