// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'profile_photo_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$profilePhotoNotifierHash() =>
    r'fb44ab255f21c033a938f7cebd15909c9e49b772';

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
///
/// Copied from [ProfilePhotoNotifier].
@ProviderFor(ProfilePhotoNotifier)
final profilePhotoNotifierProvider = AutoDisposeNotifierProvider<
    ProfilePhotoNotifier, AsyncValue<String?>>.internal(
  ProfilePhotoNotifier.new,
  name: r'profilePhotoNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$profilePhotoNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$ProfilePhotoNotifier = AutoDisposeNotifier<AsyncValue<String?>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
