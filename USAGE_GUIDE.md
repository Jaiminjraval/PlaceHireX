# PlaceHireX — Complete Usage Guide

Everything can be tested from the **browser** — no Postman needed.

---

## ⚡ Startup (3 terminals)

| # | Service | Command | Port |
|---|---------|---------|------|
| 1 | ML Service | `cd ml-service` → `uvicorn app:app --reload --port 8000` | 8000 |
| 2 | Backend | `cd backend/placement-backend` → `.\mvnw.cmd spring-boot:run` | 8080 |
| 3 | Frontend | `cd frontend/placement-ui` → `npm run dev` | 5173 |

> Start in this order. ML first, then Backend, then Frontend.

---

## 🧑‍🎓 Student Flow (step-by-step)

### 1. Register

1. Open **http://localhost:5173** → you'll see the **Login** page
2. Click **"Register"** link at the bottom
3. Fill in:
   - **Full Name** — e.g. `Jaimin Raval`
   - **Email** — e.g. `jaimin@test.com`
   - **Password** — e.g. `password123` (min 6 chars)
4. Click **Register** → auto-redirects to **Profile Setup**

### 2. Setup Profile

You'll land on the **Setup Your Profile** page. Fill in:

| Field | Example Value | Range |
|-------|---------------|-------|
| CGPA | `8.5` | 0 – 10 |
| DSA Rating | `4` | 1 – 5 |
| Projects Count | `3` | 0+ |
| Attendance % | `88` | 0 – 100 |
| Aptitude Score | `80` | 0 – 100 |
| Internship | ✅ check | on/off |

Click **Create Profile** → redirects to **Student Dashboard**

### 3. Student Dashboard

The dashboard automatically runs a **placement prediction** and shows:

| Section | What it shows |
|---------|---------------|
| **Placement Score** | Circular progress ring (green ≥ 70%, yellow 40–69%, red < 40%) |
| **Status Badge** | "Likely Placed" or "Not Likely Placed" |
| **Explanations** | Why you got this score (e.g. "Low attendance") |
| **Recommendations** | How to improve (e.g. "Build two or more quality projects") |
| **History Chart** | Score trend over time (visible after ≥ 2 predictions) |

### 4. Edit Profile & Re-predict

1. Click **"Edit Profile"** in the sidebar
2. Change values (try lowering CGPA to 6.0 and unchecking internship)
3. Click **Update Profile** → new prediction on dashboard
4. Check the **History Chart** — it now shows both predictions!

### 5. Logout

Click **Logout** in the sidebar → clears session → back to login.

---

## 🛡️ Admin Flow

### 1. Admin Account (auto-created)

The backend **automatically seeds** a default admin account on startup:

| Field | Value |
|-------|-------|
| Email | `admin@placehirex.com` |
| Password | `admin123` |

> No manual SQL needed — just start the backend.

### 2. Login as Admin

1. Open **http://localhost:5173**
2. Enter:
   - **Email:** `admin@placehirex.com`
   - **Password:** whatever you encoded
3. Click **Sign In** → redirected to **Admin Dashboard**

### 3. Admin Dashboard

Shows campus-wide analytics:

| Section | What it shows |
|---------|---------------|
| **Stat Cards** | Total Students, Ready count, Avg Score, Avg CGPA |
| **Readiness Pie Chart** | Ready vs Not Ready split |
| **Internship Bar Chart** | Internship correlation with placement readiness |
| **Score Distribution** | Student count per score range (0–20, 20–40, etc.) |

> Charts are meaningful after 3+ students have run predictions.

### 4. Student Directory

1. Click **"Students"** in the sidebar
2. See all student profiles in a **searchable table**
3. Columns: Name, Email, CGPA, Readiness Score, Status
4. Use the search bar to filter by name or email

### 5. Settings — Retrain ML Model

1. Click **"Settings"** in the sidebar
2. You'll see the **Upload Dataset** section with CSV format hint
3. Prepare a CSV file like this:

```csv
cgpa,dsaRating,projectsCount,internship,attendance,aptitudeScore,placed
8.5,4,3,1,88,80,1
6.8,2,1,0,65,55,0
9.2,5,4,1,92,90,1
7.1,3,2,0,72,60,0
```

4. Click the upload area → select your CSV file
5. Click **Upload & Retrain**
6. See the success message with new model accuracy

---

## 🎨 UI Features to Notice

- **Custom cursor** — solid black circle following your mouse
- **Drifting blobs** — smoky shapes that slowly drift across all pages
- **Glassmorphic cards** — translucent cards with blur and border
- **Staggered animations** — elements fade in one by one
- **Split pill buttons** — black + green with hover/tap effects
- **Sidebar navigation** — dark panel with active state highlighting
- **Chart animations** — pie/bar/line charts animate on load
- **Table row animations** — rows fade in sequentially

---

## ❌ Common Issues

| Issue | Fix |
|-------|-----|
| "Invalid credentials" on login | Check email/password — they're case-sensitive |
| Dashboard shows error | Make sure ML service is running (port 8000) |
| Admin dashboard is empty | Register students and run predictions first |
| 401 on API calls | Token expired — login again (tokens last 24 hours) |
| Upload dataset fails | Check ML service is running and CSV format matches |
| Student Directory shows "—" for names | Older accounts pre-date the name field — register new ones |

---

## 🧪 Quick Test Checklist

- [ ] Register a student account from the UI
- [ ] Create profile → get first prediction
- [ ] Edit profile → get second prediction → see history chart
- [ ] Logout → Login back in
- [ ] Login as admin → see dashboard charts
- [ ] Admin → Students → search a student
- [ ] Admin → Settings → upload CSV → retrain model
- [ ] Register 3+ students with different stats → see varied admin analytics
