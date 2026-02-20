# TaskHive — Auth Module Structure
## Based on SRS v2.3

---

## API Endpoints

```
POST   /api/v1/auth/login               # Sets accessToken + refreshToken as HttpOnly Cookies
POST   /api/v1/auth/logout              # Revokes refreshToken in DB, clears both cookies
POST   /api/v1/auth/refresh             # Issues new accessToken cookie
POST   /api/v1/auth/activate-account    # Employee sets password via email token
POST   /api/v1/auth/forgot-password     # Sends reset email (no email enumeration)
POST   /api/v1/auth/reset-password      # Resets password using token from email
POST   /api/v1/auth/change-password     # Change password (JWT cookie required)
GET    /api/v1/auth/me                  # Get current logged-in user info
```

---

## Backend Structure

```
taskhive-backend/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/digiwork/taskhive/
│   │   │       │
│   │   │       ├── TaskHiveApplication.java
│   │   │       │
│   │   │       ├── common/
│   │   │       │   ├── config/
│   │   │       │   │   ├── SecurityConfig.java
│   │   │       │   │   ├── CorsConfig.java
│   │   │       │   │   ├── WebConfig.java
│   │   │       │   │   ├── WebSocketConfig.java
│   │   │       │   │   └── OpenApiConfig.java
│   │   │       │   │
│   │   │       │   ├── exception/
│   │   │       │   │   ├── GlobalExceptionHandler.java
│   │   │       │   │   ├── ResourceNotFoundException.java
│   │   │       │   │   ├── UnauthorizedException.java
│   │   │       │   │   ├── InvalidTokenException.java
│   │   │       │   │   ├── DuplicateResourceException.java
│   │   │       │   │   └── BusinessException.java
│   │   │       │   │
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ApiResponse.java
│   │   │       │   │   ├── ErrorResponse.java
│   │   │       │   │   └── PageResponse.java
│   │   │       │   │
│   │   │       │   ├── util/
│   │   │       │   │   ├── CookieUtil.java
│   │   │       │   │   ├── DateUtil.java
│   │   │       │   │   ├── ValidationUtil.java
│   │   │       │   │   └── StringUtil.java
│   │   │       │   │
│   │   │       │   ├── constants/
│   │   │       │   │   ├── AppConstants.java
│   │   │       │   │   ├── CookieConstants.java
│   │   │       │   │   └── MessageConstants.java
│   │   │       │   │
│   │   │       │   └── storage/
│   │   │       │       ├── StorageService.java
│   │   │       │       ├── LocalStorageService.java
│   │   │       │       ├── S3StorageService.java
│   │   │       │       └── StorageConfig.java
│   │   │       │
│   │   │       └── module/
│   │   │           └── auth/
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   └── AuthController.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── TokenService.java
│   │   │               │   ├── PasswordService.java
│   │   │               │   ├── AccountActivationService.java
│   │   │               │   ├── PasswordResetService.java
│   │   │               │   └── AdminSeeder.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── LoginRequest.java
│   │   │               │   ├── LoginResponse.java
│   │   │               │   ├── ActivateAccountRequest.java
│   │   │               │   ├── ForgotPasswordRequest.java
│   │   │               │   ├── ResetPasswordRequest.java
│   │   │               │   ├── ChangePasswordRequest.java
│   │   │               │   └── UserInfoResponse.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── User.java
│   │   │               │   ├── Role.java
│   │   │               │   ├── UserRole.java
│   │   │               │   ├── RefreshToken.java
│   │   │               │   ├── PasswordResetToken.java
│   │   │               │   ├── AccountActivationToken.java
│   │   │               │   └── PasswordHistory.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── RoleRepository.java
│   │   │               │   ├── UserRoleRepository.java
│   │   │               │   ├── RefreshTokenRepository.java
│   │   │               │   ├── PasswordResetTokenRepository.java
│   │   │               │   ├── AccountActivationTokenRepository.java
│   │   │               │   └── PasswordHistoryRepository.java
│   │   │               │
│   │   │               ├── security/
│   │   │               │   ├── JwtConfig.java
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── CustomUserDetailsService.java
│   │   │               │   ├── CustomUserDetails.java
│   │   │               │   └── SecurityUtils.java
│   │   │               │
│   │   │               ├── mapper/
│   │   │               │   └── UserMapper.java
│   │   │               │
│   │   │               ├── event/
│   │   │               │   ├── UserAuthenticatedEvent.java
│   │   │               │   ├── UserLoggedOutEvent.java
│   │   │               │   ├── PasswordChangedEvent.java
│   │   │               │   ├── PasswordResetRequestedEvent.java
│   │   │               │   └── AccountActivatedEvent.java
│   │   │               │
│   │   │               ├── exception/
│   │   │               │   ├── InvalidCredentialsException.java
│   │   │               │   ├── AccountLockedException.java
│   │   │               │   ├── AccountNotActiveException.java
│   │   │               │   ├── TokenExpiredException.java
│   │   │               │   ├── TokenAlreadyUsedException.java
│   │   │               │   └── InvalidTokenException.java
│   │   │               │
│   │   │               ├── enums/
│   │   │               │   ├── RoleType.java
│   │   │               │   └── UserStatus.java
│   │   │               │
│   │   │               └── config/
│   │   │                   └── PasswordEncoderConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       │
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1.0__create_users_table.sql
│   │       │       ├── V1.1__create_roles_table.sql
│   │       │       ├── V1.2__create_user_roles_table.sql
│   │       │       ├── V1.3__create_refresh_tokens_table.sql
│   │       │       ├── V1.4__create_password_reset_tokens_table.sql
│   │       │       ├── V1.5__create_account_activation_tokens_table.sql
│   │       │       ├── V1.6__create_password_history_table.sql
│   │       │       └── V7.0__create_indexes.sql
│   │       │
│   │       └── templates/
│   │           └── email/
│   │               ├── account-activation.html
│   │               ├── password-reset.html
│   │               └── password-changed.html
│   │
│   └── test/
│       └── java/
│           └── com/digiwork/taskhive/
│               ├── module/
│               │   └── auth/
│               │       ├── controller/
│               │       │   └── AuthControllerTest.java
│               │       ├── service/
│               │       │   ├── AuthServiceTest.java
│               │       │   ├── TokenServiceTest.java
│               │       │   ├── PasswordServiceTest.java
│               │       │   ├── AccountActivationServiceTest.java
│               │       │   └── PasswordResetServiceTest.java
│               │       └── security/
│               │           ├── JwtTokenProviderTest.java
│               │           └── JwtAuthenticationFilterTest.java
│               └── integration/
│                   └── AuthIntegrationTest.java
│
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## application.properties (Auth-Related)

```properties
# DATABASE
spring.datasource.url=jdbc:postgresql://localhost:5432/taskhive
spring.datasource.username=taskhive
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.datasource.hikari.maximum-pool-size=20

# FLYWAY
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# JWT
jwt.secret=your-256-bit-minimum-secret-key-here
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=604800000

# COOKIE
app.cookie.secure=true
app.cookie.same-site=Strict
app.cookie.domain=yourdomain.com

# DEFAULT ADMIN
app.admin.email=admin@taskhive.com
app.admin.password=Admin@123
app.admin.first-name=System
app.admin.last-name=Admin

# TOKEN EXPIRY
app.auth.activation-token-expiry=86400000
app.auth.reset-token-expiry=3600000

# ACCOUNT LOCKOUT
app.auth.max-failed-attempts=5
app.auth.lockout-duration=900000

# MAIL
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=erwin1410c@gmail.com
spring.mail.password=ezmmmitmxmklksjg
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# FRONTEND URL
app.frontend.url=http://localhost:3000

# STORAGE
storage.type=local
storage.local.upload-dir=/var/taskhive/uploads

# ML
ml.enabled=false
```

---

## Frontend Structure

```
taskhive-frontend/
│
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── layout.tsx
│   │   │   ├── login/page.tsx
│   │   │   ├── forgot-password/page.tsx
│   │   │   ├── reset-password/page.tsx
│   │   │   └── activate-account/page.tsx
│   │   │
│   │   └── (employee)/
│   │       └── settings/
│   │           └── security/page.tsx
│   │
│   ├── features/
│   │   └── auth/
│   │       ├── components/
│   │       │   ├── LoginForm.tsx
│   │       │   ├── ForgotPasswordForm.tsx
│   │       │   ├── ResetPasswordForm.tsx
│   │       │   ├── ActivateAccountForm.tsx
│   │       │   └── ChangePasswordForm.tsx
│   │       │
│   │       ├── hooks/
│   │       │   ├── useLogin.ts
│   │       │   ├── useLogout.ts
│   │       │   ├── useActivateAccount.ts
│   │       │   ├── useForgotPassword.ts
│   │       │   ├── useResetPassword.ts
│   │       │   ├── useChangePassword.ts
│   │       │   ├── useCurrentUser.ts
│   │       │   └── useMe.ts
│   │       │
│   │       ├── services/
│   │       │   └── authService.ts
│   │       │
│   │       ├── store/
│   │       │   └── authStore.ts
│   │       │
│   │       ├── types/
│   │       │   ├── auth.types.ts
│   │       │   └── user.types.ts
│   │       │
│   │       └── utils/
│   │           └── authUtils.ts
│   │
│   ├── shared/
│   │   └── services/
│   │       └── api/
│   │           ├── apiClient.ts
│   │           └── endpoints.ts
│   │
│   └── middleware.ts
```

---

## apiClient.ts

```typescript
import axios from 'axios';
import { useAuthStore } from '@/features/auth/store/authStore';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: Function; reject: Function }> = [];

const processQueue = (error: unknown) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve()));
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;

    if (error.response?.status === 401 && !original._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => apiClient(original)).catch((e) => Promise.reject(e));
      }

      original._retry = true;
      isRefreshing = true;

      try {
        await apiClient.post('/auth/refresh');
        processQueue(null);
        return apiClient(original);
      } catch (refreshError) {
        processQueue(refreshError);
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## authStore.ts

```typescript
import { create } from 'zustand';

interface UserInfo {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'EMPLOYEE';
}

interface AuthState {
  user: UserInfo | null;
  isAuthenticated: boolean;
  setUser: (user: UserInfo) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: true }),
  clearAuth: () => set({ user: null, isAuthenticated: false }),
}));

export const useIsAdmin = () => useAuthStore((s) => s.user?.role === 'ADMIN');
export const useIsEmployee = () => useAuthStore((s) => s.user?.role === 'EMPLOYEE');
export const useCurrentUser = () => useAuthStore((s) => s.user);
```

---

## middleware.ts

```typescript
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const PUBLIC_ROUTES = ['/login', '/forgot-password', '/reset-password', '/activate-account'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const accessToken = request.cookies.get('accessToken')?.value;
  const userRole = request.cookies.get('userRole')?.value;

  const isPublicRoute = PUBLIC_ROUTES.some((r) => pathname.startsWith(r));
  const isAdminRoute = pathname.startsWith('/admin');
  const isEmployeeRoute = pathname.startsWith('/employee');

  if (!isPublicRoute && !accessToken) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  if (isPublicRoute && accessToken) {
    const redirect = userRole === 'ADMIN' ? '/admin/dashboard' : '/employee/dashboard';
    return NextResponse.redirect(new URL(redirect, request.url));
  }

  if (isAdminRoute && userRole !== 'ADMIN') {
    return NextResponse.redirect(new URL('/employee/dashboard', request.url));
  }

  if (isEmployeeRoute && userRole !== 'EMPLOYEE') {
    return NextResponse.redirect(new URL('/admin/dashboard', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico|api).*)'],
};
```

---

## Auth Flow

```
LOGIN:
1.  POST /auth/login { email, password }
2.  Validate credentials → check UserStatus → check lockedUntil
3.  If invalid → increment failedAttempts; at 5 → set lockedUntil = now + 15min
4.  Generate JWT accessToken + UUID refreshToken
5.  Store SHA-256(refreshToken) in refresh_tokens table
6.  Set accessToken cookie  → HttpOnly, Secure, SameSite=Strict, Max-Age=900
    Set refreshToken cookie → HttpOnly, Secure, SameSite=Strict, Max-Age=604800, Path=/api/v1/auth/refresh
    Set userRole cookie     → non-HttpOnly (for Next.js middleware route protection only)
7.  Return { user: { id, email, firstName, lastName, role } }
8.  Frontend: authStore.setUser(user)

REQUEST:
1.  Browser auto-attaches accessToken cookie (withCredentials: true)
2.  JwtAuthenticationFilter reads "accessToken" cookie → validates JWT → sets SecurityContext

REFRESH:
1.  Server returns 401
2.  apiClient interceptor → POST /auth/refresh
3.  Browser auto-sends refreshToken cookie (Path matches)
4.  Server validates → issues new accessToken cookie
5.  apiClient retries original request

LOGOUT:
1.  POST /auth/logout
2.  Revoke refreshToken in DB
3.  Clear all 3 cookies (accessToken, refreshToken, userRole)
4.  Frontend: authStore.clearAuth() → redirect /login

CHANGE / RESET PASSWORD:
1.  Check last 3 in password_history → reject if match
2.  BCrypt encode → save to users.passwordHash
3.  Save to password_history (keep only last 3)
4.  Revoke ALL active refresh tokens for user
5.  Publish PasswordChangedEvent → confirmation email sent
```

---

## Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller / Pages | 1 | 5 pages |
| Services / Hooks | 6 services | 8 hooks |
| DTOs / Types | 7 DTOs | 2 type files |
| Models | 7 | 1 store |
| Repositories | 7 | — |
| Security | 6 files | 1 middleware |
| Mapper | 1 | — |
| Events | 5 | — |
| Exceptions (module) | 6 | — |
| Exceptions (common) | 5 | — |
| DB Migrations | V1.0 → V1.6 + V7.0 | — |
| Email Templates | 3 | — |
| Tests | 9 | — |
