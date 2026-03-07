import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/models/user_model.dart';
import 'auth_provider.dart';

final currentUserProvider = Provider<UserModel?>((ref) {
  return ref.watch(authStateNotifierProvider).user;
});

final isAdminProvider = Provider<bool>((ref) {
  return ref.watch(currentUserProvider)?.isAdmin ?? false;
});

final isEmployeeProvider = Provider<bool>((ref) {
  return ref.watch(currentUserProvider)?.isEmployee ?? false;
});
