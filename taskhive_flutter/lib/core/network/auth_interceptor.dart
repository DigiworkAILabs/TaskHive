import 'dart:async';

import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';

typedef VoidCallback = void Function();

/// Handles 401 responses by refreshing the access token.
/// Does NOT hold a Riverpod ref — uses a callback to prevent circular dependency.
class AuthInterceptor extends Interceptor {
  final Dio _dio;
  final CookieJar _cookieJar;
  final VoidCallback onForceLogout;
  final String refreshEndpoint;

  bool _isRefreshing = false;
  final List<_PendingRequest> _queue = [];

  AuthInterceptor({
    required Dio dio,
    required CookieJar cookieJar,
    required this.onForceLogout,
    required this.refreshEndpoint,
  })  : _dio = dio,
        _cookieJar = cookieJar;

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // Only intercept 401s — skip the refresh endpoint itself (infinite loop guard)
    if (err.response?.statusCode != 401 ||
        err.requestOptions.path.contains(refreshEndpoint)) {
      return handler.next(err);
    }

    final originalRequest = err.requestOptions;

    // Another refresh already in flight — queue this request
    if (_isRefreshing) {
      final completer = Completer<void>();
      _queue
          .add(_PendingRequest(completer: completer, options: originalRequest));
      try {
        await completer.future;
        final retried = await _retry(originalRequest);
        return handler.resolve(retried);
      } catch (_) {
        return handler.next(err);
      }
    }

    _isRefreshing = true;

    try {
      // refreshToken cookie is in the jar — sent automatically by CookieManager
      await _dio.post(refreshEndpoint);

      // Refresh succeeded — unblock all queued requests
      for (final pending in _queue) {
        pending.completer.complete();
      }
      _queue.clear();

      final retried = await _retry(originalRequest);
      handler.resolve(retried);
    } catch (_) {
      // Refresh failed — reject queue and force logout
      for (final pending in _queue) {
        pending.completer.completeError('Session expired');
      }
      _queue.clear();
      await _cookieJar.deleteAll();
      onForceLogout(); // Notifies AuthNotifier → router redirects to login
      handler.next(err);
    } finally {
      _isRefreshing = false;
    }
  }

  Future<Response<dynamic>> _retry(RequestOptions options) {
    return _dio.request(
      options.path,
      data: options.data,
      queryParameters: options.queryParameters,
      options: Options(method: options.method, headers: options.headers),
    );
  }
}

class _PendingRequest {
  final Completer<void> completer;
  final RequestOptions options;
  _PendingRequest({required this.completer, required this.options});
}
