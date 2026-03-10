import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'dio_client.dart';
import '../utils/logger.dart';
import '../../features/notification/data/models/notification_model.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import '../../features/notification/domain/providers/stomp_provider.dart';

class StompClientWrapper with WidgetsBindingObserver {
  StompClient? _client;
  final String userId;
  final Ref ref;
  bool _isConnected = false;

  StompClientWrapper({required this.userId, required this.ref}) {
    WidgetsBinding.instance.addObserver(this);
  }

  StompClientWrapper.empty({required this.ref}) : userId = '' {
    // Empty wrapper logic
  }

  void connect() async {
    if (userId.isEmpty) return;

    final baseUrl = dotenv.env['WS_BASE_URL'] ?? 'ws://localhost:8080/ws';

    Map<String, String> headers = {};
    if (!kIsWeb) {
      final cookieJar = ref.read(cookieJarProvider);
      // We only need the access token for stomp websocket connection
      final cookies = await cookieJar.loadForRequest(Uri.parse(baseUrl));
      final tokenCookie =
          cookies.where((c) => c.name == 'accessToken').firstOrNull;
      if (tokenCookie != null) {
        headers['Cookie'] = '${tokenCookie.name}=${tokenCookie.value}';
      }
    }

    _client = StompClient(
      config: StompConfig(
        url: baseUrl,
        onConnect: _onConnect,
        onStompError: _onStompError,
        onDisconnect: _onDisconnect,
        onWebSocketError: _onWebSocketError,
        reconnectDelay: const Duration(seconds: 5),
        webSocketConnectHeaders: headers,
      ),
    );

    _client?.activate();
  }

  void _onConnect(StompFrame frame) {
    _isConnected = true;
    appLogger.i('STOMP Connected: ${frame.body}');

    subscribe('/topic/notifications/$userId', _onNotificationFrame);
    subscribe('/topic/system', _onSystemFrame);
  }

  void _onNotificationFrame(StompFrame frame) {
    if (frame.body != null) {
      try {
        final json = jsonDecode(frame.body!);
        final notification = NotificationModel.fromJson(json);
        // Dispatch to providers via the callback in stomp_provider
        ref.read(stompProvider.notifier).handleNewNotification(notification);
      } catch (e) {
        appLogger.e('Error parsing notification frame: $e');
      }
    }
  }

  void _onSystemFrame(StompFrame frame) {
    if (frame.body != null) {
      appLogger.i('System notification: ${frame.body}');
    }
  }

  void _onStompError(StompFrame frame) {
    appLogger.e('STOMP Error: ${frame.body}');
    // Client auto-reconnects based on reconnectDelay
  }

  void _onWebSocketError(dynamic error) {
    appLogger.e('WebSocket Error: $error');
  }

  void _onDisconnect(StompFrame frame) {
    _isConnected = false;
    appLogger.i('STOMP Disconnected');
  }

  void disconnect() {
    _client?.deactivate();
    _client = null;
    _isConnected = false;
    WidgetsBinding.instance.removeObserver(this);
  }

  void reconnect() {
    if (!_isConnected && userId.isNotEmpty) {
      appLogger.i('Attempting manual STOMP reconnect...');
      _client?.activate();
    }
  }

  void subscribe(String topic, void Function(StompFrame) callback) {
    if (_isConnected) {
      _client?.subscribe(destination: topic, callback: callback);
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      appLogger.i('App resumed, checking STOMP connection...');
      reconnect();
    }
  }
}
