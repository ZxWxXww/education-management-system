# EduSmart Manager Integration Notes

This file follows the same local integration baseline as the main workspace.

## Canonical Local Baseline

- Frontend dev URL: `http://localhost:5173`
- Backend base URL: `http://localhost:8080`
- Health endpoint: `http://localhost:8080/api/health`
- Database: `edu`
- JWT secret: `dev-secret-please-change-1234567890`
- Shared default password: `Admin@123456`

## Seed Accounts

| Role | Username | Password | Login role value |
| --- | --- | --- | --- |
| Admin | `admin` | `Admin@123456` | `admin` |
| Teacher | `user_test_01` | `Admin@123456` | `teacher` |
| Student | `user_auth_02` | `Admin@123456` | `student` |

## Frontend Proxy

`frontend/.env.development`

```bash
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:8080
```

`frontend/vite.config.js`

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## Backend Environment

The backend is expected to use the following local values:

```bash
EDU_SERVER_PORT=8080
EDU_DB_URL=jdbc:mysql://localhost:3306/edu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
EDU_DB_USER=root
EDU_DB_PASSWORD=Zxcvbnm9.
EDU_JWT_SECRET=dev-secret-please-change-1234567890
EDU_DEFAULT_PASSWORD=Admin@123456
```

Allowed CORS origins:

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://localhost:5174`
- `http://127.0.0.1:5174`
- `http://localhost:5175`
- `http://127.0.0.1:5175`
