# EduSmart Manager Frontend

## Current Integration Baseline

- Frontend dev URL: `http://localhost:5173`
- Backend API base: `http://localhost:8080`
- Vite proxy: `/api -> http://localhost:8080`
- Database: `edu`
- Health check: `GET http://localhost:8080/api/health`
- JWT secret: `dev-secret-please-change-1234567890`
- CORS whitelist:
  - `http://localhost:5173`
  - `http://127.0.0.1:5173`
  - `http://localhost:5174`
  - `http://127.0.0.1:5174`
  - `http://localhost:5175`
  - `http://127.0.0.1:5175`

## Seed Login Accounts

Use the same default password for all three roles:

- Password: `Admin@123456`

| Role | Username | Login role value |
| --- | --- | --- |
| Admin | `admin` | `admin` |
| Teacher | `user_test_01` | `teacher` |
| Student | `user_auth_02` | `student` |

## Local Startup

```bash
npm install
npm run dev
```

After the dev server starts, open `http://localhost:5173/login` and log in with one of the accounts above.

## Proxy Rules

The frontend uses `frontend/.env.development` and `frontend/vite.config.js`.

- `VITE_API_BASE_URL=/api`
- `VITE_API_PROXY_TARGET=http://localhost:8080`

If login fails, check these two endpoints first:

```bash
curl http://localhost:8080/api/health
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"Admin@123456\",\"role\":\"admin\"}"
```
