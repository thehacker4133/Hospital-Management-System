# CarePoint: Hospital and Clinic Management System

React (Vite) + Spring Boot 3 + MySQL, with JWT login and role-based access. A college / learning project.

> **Not a certified medical system.** Do not store real patient data without a proper security review (see "Before real-world use").

## What is included

| Area | What it does |
|---|---|
| Authentication | Registration (patients only), login with BCrypt-hashed passwords, JWT bearer tokens, sign-in lockout after repeated wrong passwords |
| Roles | `PATIENT`, `DOCTOR`, `ADMIN`. Permissions are enforced on the server, not just hidden in the UI |
| Doctors | Public doctor directory; admin adds a doctor by the email of a registered user; admin can remove a doctor |
| Weekly availability | A doctor sets their own weekly hours; bookings must fall inside them (no hours set = no restriction) |
| Appointments | Patients book 10-minute start-time slots and cancel their own; doctors confirm / reject / complete; occupied times are rejected with available ranges; status changes follow a fixed lifecycle |
| Departments | Admin creates and deletes departments |
| Medical records / prescriptions | Doctors write them only for patients with a confirmed or completed appointment; patients read only their own; admins have no access; every read and write is logged under `AUDIT` |
| Database | Replit uses managed PostgreSQL; local development uses MySQL through JPA |

## Requirements

Java 17+, Maven 3.9+, Node.js 20+, MySQL 8+ (or Docker, to run MySQL).

## 1. Database

Either use the included Docker file:

```bash
docker compose up -d        # creates MySQL with the hospital_management database
```

or create it in your own MySQL:

```sql
CREATE DATABASE hospital_management;
```

## 2. Backend

Set these environment variables, then start the API.

| Variable | Required | Purpose |
|---|---|---|
| `DB_PASSWORD` | if your MySQL has a password | MySQL password (empty by default). With the Docker file the default is `changeme` |
| `DB_URL`, `DB_USERNAME` | no | Default `jdbc:mysql://localhost:3306/hospital_management` and `root` |
| `JWT_SECRET` | recommended | 32+ characters. If missing, a random one is used and everyone is signed out on each restart |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | first run | Creates the first ADMIN account if that email is not registered yet (password min 8 chars) |
| `CORS_ORIGINS` | no | Comma-separated allowed frontend origins. Default allows localhost |
| `MAX_LOGIN_FAILURES`, `LOCKOUT_MINUTES` | no | Defaults 5 and 15. See "Sign-in lockout" |

```bash
export DB_PASSWORD=changeme
export JWT_SECRET=$(openssl rand -hex 32)
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD=ChangeMe123
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. Run the tests with `mvn test`.

## 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open the address Vite prints (normally `http://localhost:5173`). If the API is somewhere else, copy `.env.example` to `.env` and set `VITE_API_URL`.

## First steps after starting

1. Sign in with the `ADMIN_EMAIL` / `ADMIN_PASSWORD` you set.
2. Create a department (Departments page).
3. Have the doctor register an account on the Register page, then add them on the Doctors page using that email.
4. The doctor signs in and sets their weekly availability on the Doctors page.
5. Patients register, then book appointments. The doctor confirms them, and can then add records and prescriptions.

## Roles

- **PATIENT** (what the Register page creates): book and cancel own appointments, view own records and prescriptions.
- **DOCTOR**: manage weekly hours; confirm, reject or complete own appointments; add records and prescriptions for their own patients.
- **ADMIN**: manage departments and doctors; see all appointments. Cannot read medical records or prescriptions.

## API overview

All routes need `Authorization: Bearer <token>` except `/api/auth/**`. Errors are always `{"message": "..."}`.

| Method and path | Who | Purpose |
|---|---|---|
| `POST /api/auth/register`, `POST /api/auth/login` | anyone | Create a patient account / sign in |
| `GET /api/doctors`, `GET /api/doctors/{id}` | signed in | Doctor directory |
| `GET /api/doctors/availability` | signed in | Weekly hours of all doctors |
| `PUT /api/doctors/me/availability` | doctor | Replace own weekly hours: `{"slots":[{"day":"MONDAY","start":"09:00","end":"13:00"}]}` |
| `POST /api/doctors`, `DELETE /api/doctors/{id}` | admin | Add / remove a doctor |
| `GET/POST/DELETE /api/departments` | read: signed in; write: admin | Departments |
| `GET /api/appointments` | signed in | Own appointments (admin: all) |
| `POST /api/appointments` | patient | Book a slot |
| `PATCH /api/appointments/{id}/status` | patient (cancel only), doctor, admin | Change status |
| `GET/POST /api/medical-records`, `GET/POST /api/prescriptions` | read: patient, doctor; write: doctor | Clinical data |

## Appointment rules

- Appointment start times use 10-minute increments (10:00, 10:10, 10:20).
- A time is reserved as soon as its request is created, including while pending. A second request for that doctor/date/time is rejected and the response lists remaining free start-time ranges for that date.
- If the doctor has published weekly hours, the slot must fall inside them (the end of a block is exclusive).
- Lifecycle: `PENDING` to `CONFIRMED` / `REJECTED` / `CANCELLED`; `CONFIRMED` to `COMPLETED` / `CANCELLED`. Cancelled and rejected appointments free the slot again.

## Sign-in lockout

After `MAX_LOGIN_FAILURES` wrong passwords from the same address for the same email, further attempts get HTTP 429 for `LOCKOUT_MINUTES`. A successful sign-in resets the counter. The counters live in memory, so they reset when the server restarts and are not shared between several backend instances. Behind a reverse proxy, every client appears to come from the proxy's address unless you configure forwarded headers deliberately.

## Security notes

- Passwords are hashed with BCrypt. Login returns a JWT that the frontend keeps in `sessionStorage` (cleared when the tab closes).
- Every route except `/api/auth/**` needs a valid token. Role and ownership checks run on the server.
- The user is reloaded from the database on every request, so a role change or deleted account takes effect immediately.
- Responses use DTOs, so password hashes and other users' contact details are never returned.
- Error responses never include stack traces or internal messages.

## Before real-world use

- Database migrations (Flyway or Liquibase) instead of `ddl-auto=update`; HTTPS; secrets in a vault
- Password reset, refresh tokens, shared (Redis) login throttling
- Integration tests against a test database, and a security review

## Project layout

```
backend/    Spring Boot API (controller, security, service, repository, model, dto, config)
frontend/   React app (pages, components, context, services, utils)
docker-compose.yml   Optional local MySQL
```
