import { useNavigate, useLocation, Link } from "react-router-dom";
import { motion } from "framer-motion";

/* ── Icon components (inline SVG) ──────────────────────────── */
const DashboardIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
    </svg>
);

const ProfileIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
);

const UsersIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
);

const SettingsIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
);

const LogoutIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
    </svg>
);

/* ── Sidebar ────────────────────────────────────────────────── */
export default function Sidebar({ children }) {
    const navigate = useNavigate();
    const location = useLocation();
    const role = localStorage.getItem("role");

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        navigate("/login");
    };

    const studentLinks = [
        { to: "/student/dashboard", label: "Dashboard", icon: <DashboardIcon /> },
        { to: "/student/profile", label: "Edit Profile", icon: <ProfileIcon /> },
    ];

    const adminLinks = [
        { to: "/admin/dashboard", label: "Dashboard", icon: <DashboardIcon /> },
        { to: "/admin/students", label: "Students", icon: <UsersIcon /> },
        { to: "/admin/settings", label: "Settings", icon: <SettingsIcon /> },
    ];

    const links = role === "ADMIN" ? adminLinks : studentLinks;

    return (
        <div className="flex min-h-screen">
            {/* ── Sidebar panel ────────────────────────────────── */}
            <motion.aside
                initial={{ x: -80, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
                className="fixed left-0 top-0 z-50 flex h-screen w-[220px] flex-col bg-black/[0.95] backdrop-blur-2xl"
            >
                {/* Brand */}
                <div className="flex items-center gap-2 px-6 py-7">
                    <span className="text-xl font-extrabold tracking-tight text-white">
                        PlaceHire<span className="text-[#A8E6CF]">X</span>
                    </span>
                </div>

                {/* Role badge */}
                <div className="px-6 mb-6">
                    <span className="inline-block rounded-full bg-white/10 px-3 py-1 text-[10px] font-bold uppercase tracking-widest text-white/50">
                        {role}
                    </span>
                </div>

                {/* Nav links */}
                <nav className="flex-1 space-y-1 px-3">
                    {links.map((link) => {
                        const active = location.pathname === link.to;
                        return (
                            <Link
                                key={link.to}
                                to={link.to}
                                className={`flex items-center gap-3 rounded-xl px-4 py-2.5 text-sm font-medium transition-all duration-200 ${active
                                        ? "bg-[#A8E6CF]/15 text-[#A8E6CF]"
                                        : "text-white/50 hover:bg-white/5 hover:text-white/80"
                                    }`}
                            >
                                {link.icon}
                                {link.label}
                            </Link>
                        );
                    })}
                </nav>

                {/* Logout */}
                <div className="px-3 pb-6">
                    <button
                        onClick={handleLogout}
                        className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-sm font-medium text-white/40 transition-all duration-200 hover:bg-red-500/10 hover:text-red-400"
                    >
                        <LogoutIcon />
                        Logout
                    </button>
                </div>
            </motion.aside>

            {/* ── Main content area ────────────────────────────── */}
            <main className="ml-[220px] flex-1">
                {children}
            </main>
        </div>
    );
}
