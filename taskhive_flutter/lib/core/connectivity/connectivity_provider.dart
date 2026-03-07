import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// StreamProvider<bool> — true when online, false when offline.
/// Returns AsyncValue<bool> when watched.
/// In app.dart: use connectivityAsync.valueOrNull ?? true
final connectivityProvider = StreamProvider<bool>((ref) {
  return Connectivity().onConnectivityChanged.map(
        (results) => !results.contains(ConnectivityResult.none),
      );
});
