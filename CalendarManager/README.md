# Calendar Manager - Phase 1 (project setup, architecture, schema)

Open the folder in Android Studio (Koala+). Studio generates the Gradle wrapper on import.
Run the DB tests: `./gradlew connectedDebugAndroidTest` (needs an emulator/device).

Layers: `data/local` (Room) -> `data/repository` -> `domain` (interfaces/models) -> `ui` (Compose, Phase 2+).
Offline by design: no INTERNET permission, `allowBackup=false`.
