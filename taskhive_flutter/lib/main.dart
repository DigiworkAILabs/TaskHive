import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';

import 'package:flutter_web_plugins/url_strategy.dart';

import 'app.dart';
import 'core/network/dio_client.dart';

// Conditional import — safe on ALL platforms including web
import 'core/network/cookie_jar_factory_stub.dart'
    if (dart.library.io) 'core/network/cookie_jar_factory_native.dart'
    if (dart.library.html) 'core/network/cookie_jar_factory_web.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  usePathUrlStrategy();

  // 1. Load environment variables (change to .env.prod for production)
  await dotenv.load(fileName: 'assets/env/.env.dev');

  // 2. Initialize Hive offline cache — open all boxes before runApp
  await Hive.initFlutter();
  await Future.wait([
    Hive.openBox('employees_box'),
    Hive.openBox('tasks_box'),
    Hive.openBox('my_tasks_box'),
    Hive.openBox('notifications_box'),
    Hive.openBox('analytics_box'),
  ]);

  // 3. Create cookie jar BEFORE ProviderScope — async, platform-aware
  // createCookieJar() comes from the conditional import above
  final cookieJar = await createCookieJar();

  // 4. Run app — inject cookieJar as a ProviderScope override
  runApp(
    ProviderScope(
      overrides: [
        cookieJarProvider.overrideWithValue(cookieJar),
      ],
      child: const TaskHiveApp(),
    ),
  );
}
