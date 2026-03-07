import 'package:cookie_jar/cookie_jar.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../../../../core/storage/cache_service.dart';
import '../../../../core/storage/secure_storage_service.dart';
import '../../data/models/user_model.dart';
import '../../data/repositories/auth_repository.dart';

part 'auth_provider.freezed.dart';

// ── AuthState ────────────────────────────────────────────────────────────────

@freezed
class AuthState with _$AuthState {
  const factory AuthState({
    @Default(true) bool isLoading, // true on app start (session restore)
    @Default(false) bool isAuthenticated,
    UserModel? user,
    String? error,
  }) = _AuthState;
}

// ── Provider ─────────────────────────────────────────────────────────────────

// NOTE: Manual StateNotifierProvider — do NOT add @riverpod annotation.
// AuthNotifier calls restoreSession() in its constructor which requires
// the notifier to be fully constructed first — @riverpod does not support this.
final StateNotifierProvider<AuthNotifier, AuthState> authStateNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(
    authRepository: ref.watch(authRepositoryProvider),
    secureStorage: ref.watch(secureStorageServiceProvider),
    cookieJar: ref.watch(cookieJarProvider),
    cacheService: CacheService(),
  )..restoreSession();
});

// ── AuthNotifier ──────────────────────────────────────────────────────────────

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _repo;
  final SecureStorageService _storage;
  final CookieJar _cookieJar;
  final CacheService _cache;

  AuthNotifier({
    required AuthRepository authRepository,
    required SecureStorageService secureStorage,
    required CookieJar cookieJar,
    required CacheService cacheService,
  })  : _repo = authRepository,
        _storage = secureStorage,
        _cookieJar = cookieJar,
        _cache = cacheService,
        super(const AuthState(isLoading: true));

  // ── Session Restore ──────────────────────────────────────────────────────

  /// Called via ..restoreSession() in the provider constructor.
  /// isLoading=true keeps the router on SplashScreen until this completes.
  Future<void> restoreSession() async {
    try {
      final cached = await _storage.getUserInfo();
      if (cached == null) {
        state = const AuthState(isLoading: false, isAuthenticated: false);
        return;
      }
      // Verify session is still valid — cookie jar auto-sends accessToken
      final user = await _repo.getMe();
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = AuthState(isLoading: false, isAuthenticated: true, user: user);
    } catch (_) {
      // /auth/me failed — session expired or no network; treat as logged out
      await _clearAll();
      state = const AuthState(isLoading: false, isAuthenticated: false);
    }
  }

  // ── Login ────────────────────────────────────────────────────────────────

  Future<void> login({
    required String email,
    required String password,
  }) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final user = await _repo.login(email: email, password: password);
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = AuthState(isLoading: false, isAuthenticated: true, user: user);
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e is ApiException ? e.message : 'Login failed. Try again.',
      );
    }
  }

  // ── Logout ───────────────────────────────────────────────────────────────

  Future<void> logout() async {
    try {
      await _repo.logout();
    } catch (_) {
      // Proceed with local cleanup even if server call fails
    }
    await _clearAll();
    state = const AuthState(isLoading: false, isAuthenticated: false);
  }

  /// Called by AuthInterceptor callback when refresh token is expired.
  /// Must not make any API calls — cookie jar is already cleared by interceptor.
  Future<void> forceLogout() async {
    await _storage.clearUserInfo();
    await _cache.clearAll();
    state = const AuthState(isLoading: false, isAuthenticated: false);
  }

  // ── Refresh User ─────────────────────────────────────────────────────────

  Future<void> refreshCurrentUser() async {
    try {
      final user = await _repo.getMe();
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = state.copyWith(user: user);
    } catch (_) {
      // Silent fail — user info stays stale but session remains
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  Future<void> _clearAll() async {
    await Future.wait([
      _storage.clearUserInfo(),
      _cookieJar.deleteAll(),
      _cache.clearAll(),
    ]);
  }
}
