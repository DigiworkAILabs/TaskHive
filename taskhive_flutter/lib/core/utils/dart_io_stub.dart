/// Stub for dart:io on Web.
///
/// This file is selected by Dart's conditional import system when compiling
/// for a platform that provides `dart.library.html` (i.e., Web).
/// On native platforms (Android / iOS / macOS / Windows / Linux), the real
/// `dart:io` is used instead, so this file is never compiled there.
///
/// The [File] class here is a structural placeholder — it provides the same
/// interface used by [EmployeeRepository.uploadPhoto] so that static analysis
/// succeeds on Web. At runtime, the `kIsWeb` guard in `profile_photo_provider.dart`
/// ensures this code path is never executed on Web.
class File {
  final String path;
  const File(this.path);
}
