// STOMP WebSocket Client — STUB for Phase 1
// Full implementation in Phase 4 (Notifications + WebSocket)
// This file must be importable — do NOT leave empty.

class StompClientService {
  /// Connect to WebSocket — Phase 4 implementation
  Future<void> connect({required String url, required String token}) async {}

  /// Disconnect — Phase 4 implementation
  Future<void> disconnect() async {}

  /// Subscribe to a topic — Phase 4 implementation
  void subscribe(
      {required String topic, required void Function(dynamic) onMessage}) {}
}
