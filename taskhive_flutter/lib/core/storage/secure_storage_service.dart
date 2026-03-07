import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final secureStorageServiceProvider = Provider<SecureStorageService>(
  (_) => SecureStorageService(),
);

/// Stores ONLY user info (id, email, firstName, lastName, role).
/// JWT tokens are NEVER stored here — they live exclusively in the cookie jar.
class SecureStorageService {
  // Platform-specific options — see Master Context for per-platform notes
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
    iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
    mOptions: MacOsOptions(),
    wOptions: WindowsOptions(),
    lOptions: LinuxOptions(),
  );

  static const _keyId = 'user_id';
  static const _keyEmail = 'user_email';
  static const _keyFirstName = 'user_first_name';
  static const _keyLastName = 'user_last_name';
  static const _keyRole = 'user_role';

  Future<void> saveUserInfo({
    required String id,
    required String email,
    required String firstName,
    required String lastName,
    required String role,
  }) async {
    await Future.wait([
      _storage.write(key: _keyId, value: id),
      _storage.write(key: _keyEmail, value: email),
      _storage.write(key: _keyFirstName, value: firstName),
      _storage.write(key: _keyLastName, value: lastName),
      _storage.write(key: _keyRole, value: role),
    ]);
  }

  /// Returns null if any required field is missing (user not logged in)
  Future<Map<String, String>?> getUserInfo() async {
    final results = await Future.wait([
      _storage.read(key: _keyId),
      _storage.read(key: _keyEmail),
      _storage.read(key: _keyFirstName),
      _storage.read(key: _keyLastName),
      _storage.read(key: _keyRole),
    ]);

    if (results.any((v) => v == null)) return null;

    return {
      'id': results[0]!,
      'email': results[1]!,
      'firstName': results[2]!,
      'lastName': results[3]!,
      'role': results[4]!,
    };
  }

  Future<void> clearUserInfo() async {
    await Future.wait([
      _storage.delete(key: _keyId),
      _storage.delete(key: _keyEmail),
      _storage.delete(key: _keyFirstName),
      _storage.delete(key: _keyLastName),
      _storage.delete(key: _keyRole),
    ]);
  }
}
