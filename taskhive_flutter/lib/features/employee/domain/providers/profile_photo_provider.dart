// dart:io is only available on native platforms (Android, iOS, macOS, Windows, Linux).
// On Web, the conditional import resolves to the stub — the File type placeholder
// keeps the Dart analyzer happy, but the kIsWeb runtime guard below means the
// dart_io.File code path is NEVER executed on Web.
//
// This follows Master Context Rule 1:
// "Never import dart:io at the top level of any file compiled for Web."
import 'dart:io' as dart_io
    if (dart.library.html) 'package:taskhive_flutter/core/utils/dart_io_stub.dart';

import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/repositories/employee_repository.dart';
import 'employee_detail_provider.dart';
import 'employee_list_provider.dart';

part 'profile_photo_provider.g.dart';

/// Handles profile photo selection and upload (FR-EMP-10).
///
/// Platform split:
///   • **Native** (Android / iOS / macOS / Windows / Linux): picks via
///     `image_picker`, reads file path, passes a `dart:io.File` to
///     [EmployeeRepository.uploadPhoto].
///   • **Web**: picks via `image_picker`, reads raw bytes via `XFile.readAsBytes()`,
///     passes bytes to [EmployeeRepository.uploadPhotoBytes].
///     `dart:io` is never referenced at runtime on Web.
///
/// State is `AsyncValue<String?>` where the String is the returned photo URL
/// on success, or null in the initial/idle state.
@riverpod
class ProfilePhotoNotifier extends _$ProfilePhotoNotifier {
  @override
  AsyncValue<String?> build() => const AsyncData(null);

  /// Opens the gallery picker, uploads the selected image, then invalidates
  /// the detail cache and refreshes the list so avatars update everywhere.
  Future<void> pickAndUpload(String employeeId) async {
    final picker = ImagePicker();
    final picked = await picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 1024,
      maxHeight: 1024,
      imageQuality: 85,
    );
    if (picked == null) return; // User cancelled — leave state unchanged.

    state = const AsyncLoading();

    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);

      if (kIsWeb) {
        // ── Web path ────────────────────────────────────────────────────────
        // dart:io is unavailable on Web — read image as raw bytes instead.
        final bytes = await picked.readAsBytes();
        return repo.uploadPhotoBytes(employeeId, bytes, picked.name);
      } else {
        // ── Native path ─────────────────────────────────────────────────────
        // dart:io.File is safe here: kIsWeb == false guarantees this branch
        // is only reached on Android / iOS / macOS / Windows / Linux.
        // The conditional import above resolves to the real dart:io on these
        // platforms, so dart_io.File is the real dart:io.File.
        final file = dart_io.File(picked.path);
        return repo.uploadPhoto(employeeId, file);
      }
    });

    state = result;

    if (result is AsyncData<String?>) {
      // Invalidate the detail provider so the profile screen reloads with
      // the new avatar URL without requiring a manual pull-to-refresh.
      ref.invalidate(employeeDetailProvider(employeeId));
      // Refresh the list so avatar thumbnails also update without a full
      // list re-fetch — the refresh() method re-fetches page 0 and caches it.
      ref.read(employeeListNotifierProvider.notifier).refresh();
    }
  }
}
