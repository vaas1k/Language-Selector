# Agent notes for Language Selector

Android app that sets per-app locales through the hidden `ILocaleManager`
API, executed in a privileged process via Shizuku (or root through libsu).

## Stack

- Kotlin, Jetpack Compose (Material 3), Hilt (KSP), Room, Navigation Compose
- `hidden_api/` – Java stubs of hidden framework APIs (`ILocaleManager`,
  `IActivityManager`, `IActivityTaskManager`), compileOnly
- `app/src/main/aidl/` – `IUserService` contract between the app and the
  privileged process
- minSdk 33 (Android 13+), targets AOSP and HyperOS; Shizuku: latest two releases

## Layout

| Path | What |
|------|------|
| `service/UserService.kt` | Runs as shell/root. The only code that touches system APIs |
| `service/UserServiceProvider.kt` | Binds/rebinds the service, `run {}` / `getService()` for callers |
| `QSTile.kt` | Quick Settings tile, cycles pinned locales of the foreground app |
| `ui/screen/main/` | App list (`MainScreenVm` builds `AppInfo`, rows load icons on demand from its cache) |
| `ui/screen/appinfo/` | Locale picker for one app, pinned locales in SharedPreferences |
| `LocaleManager.kt` | Groups `Locale.getAvailableLocales()` by language |
| `fastlane/metadata/` | F-Droid/IzzyOnDroid listing, changelogs by versionCode |

## Build, test, run

```
./gradlew :app:testDebugUnitTest      # JVM tests, app/src/test
./gradlew :app:lintDebug
./gradlew :app:assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Needs JDK 21 and an Android SDK (`local.properties` with `sdk.dir=...`,
see README). Release signing reads `keystore.properties` (gitignored);
without it release falls back to the debug key.

## Rules

- `UserService` runs privileged. Never feed it package names or locales
  that come from an Intent, deep link or another app. Only our own UI
  and the QS tile may call it.
- Do not add dependencies for what a few lines do. No new abstractions
  for single-use code.
- Keep `AppInfo` immutable and icons as `ImageBitmap`: the list scroll
  performance depends on it. Never convert Drawables inside composables.
- Lists in `LazyColumn` use `key = { it.pkg }`; filter in the ViewModel,
  not with early returns inside `items`.
- Service calls go through `UserServiceProvider.run {}` or
  `getService()` on an IO dispatcher, never on the main thread.
- Pure logic gets a JUnit test in `app/src/test`. Binder/Compose paths
  are verified on a device.
- Bump `versionCode`/`versionName` in `app/build.gradle.kts` and add
  `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` per release.
- Strings live in `res/values*/strings.xml` (en, ja, pt-BR, zh-CN); add a
  key to all of them.
