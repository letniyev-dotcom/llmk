# ПикПэй

Android-приложение на Kotlin + Jetpack Compose, воссоздающее прототип из `app__6_.html`.

## Сборка локально

Требуется Gradle 8.7+ и JDK 17 (в проекте нет gradle-wrapper.jar — используйте установленный Gradle):

```
gradle assembleRelease
```

APK появится в `app/build/outputs/apk/release/`. Release-сборка подписана автоматическим debug-ключом Android, поэтому APK сразу устанавливается на устройство.

## Сборка через GitHub Actions

Workflow `.github/workflows/android-build.yml` собирает release-APK при каждом push и выкладывает его как артефакт сборки (`pikpay-release-apk`) — скачать можно во вкладке Actions выбранного запуска.
