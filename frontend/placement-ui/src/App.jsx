import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import StudentDashboard from "./pages/StudentDashboard";
import StudentProfilePage from "./pages/StudentProfilePage";
import AdminDashboard from "./pages/AdminDashboard";
import StudentList from "./pages/StudentList";
import AdminSettingsPage from "./pages/AdminSettingsPage";
import CustomCursor from "./components/CustomCursor";
import Sidebar from "./components/Sidebar";

/* ── Auth guard ──────────────────────────────────────────────── */
function ProtectedRoute({ children, role }) {
  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("role");

  if (!token) return <Navigate to="/login" replace />;
  if (role && userRole !== role) return <Navigate to="/login" replace />;

  return children;
}

/* ── Sidebar-wrapped guard ───────────────────────────────────── */
function AuthenticatedLayout({ children, role }) {
  return (
    <ProtectedRoute role={role}>
      <Sidebar>{children}</Sidebar>
    </ProtectedRoute>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <CustomCursor />
      <AnimatePresence mode="wait">
        <Routes>
          {/* ── Public ──────────────────────────────────── */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* ── Student ─────────────────────────────────── */}
          <Route
            path="/student/dashboard"
            element={
              <AuthenticatedLayout role="STUDENT">
                <StudentDashboard />
              </AuthenticatedLayout>
            }
          />
          <Route
            path="/student/profile"
            element={
              <AuthenticatedLayout role="STUDENT">
                <StudentProfilePage />
              </AuthenticatedLayout>
            }
          />

          {/* ── Admin ───────────────────────────────────── */}
          <Route
            path="/admin/dashboard"
            element={
              <AuthenticatedLayout role="ADMIN">
                <AdminDashboard />
              </AuthenticatedLayout>
            }
          />
          <Route
            path="/admin/students"
            element={
              <AuthenticatedLayout role="ADMIN">
                <StudentList />
              </AuthenticatedLayout>
            }
          />
          <Route
            path="/admin/settings"
            element={
              <AuthenticatedLayout role="ADMIN">
                <AdminSettingsPage />
              </AuthenticatedLayout>
            }
          />

          {/* ── Catch-all ───────────────────────────────── */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AnimatePresence>
    </BrowserRouter>
  );
}
