# auth-service (first extraction slice)

This service currently contains the first extracted authentication flow from `event-mng`:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/auth/verify`
- `POST /api/v1/auth/introspect`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

## Notes

- This slice currently includes token lifecycle and email verification registration.
- JWT config keys are in `src/main/resources/application.properties`:
  - `jwt.secret`
  - `jwt.expiration` (seconds)
  - `jwt.refresh-expiration` (seconds)

## Quick test

From repository root:

```powershell
& "C:\Users\PhanAnh\Desktop\intellij\java_backend\event-mng-microservice\event-mng\mvnw.cmd" -f "C:\Users\PhanAnh\Desktop\intellij\java_backend\event-mng-microservice\pom.xml" -pl auth-service -am test
```

