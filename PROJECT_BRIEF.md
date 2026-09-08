# DevPulse Project Brief

DevPulse is a production-quality Android application project. This initial milestone establishes only the project skeleton, build configuration, module boundaries, and a minimal Compose shell.

## Current Scope

- Kotlin Android project using Jetpack Compose and Material 3.
- Gradle Kotlin DSL build configuration.
- Centralized dependency and plugin versions in `gradle/libs.versions.toml`.
- Modular project structure with `:app`, shared `:core:*` modules, and independent `:feature:*` modules.
- Minimal app entry point that renders a placeholder DevPulse screen through `:core:designsystem`.

## Explicit Non-Goals For This Milestone

- No product feature implementation.
- No Retrofit, Room, Hilt, or product-specific infrastructure.
- No feature-to-feature module dependencies.
- No production data models, persistence schemas, API clients, navigation graph, or dependency injection setup.

## Quality Bar

The project should build with:

```sh
./gradlew assembleDebug
```

Future work should preserve clear module ownership, keep dependency direction explicit, and add infrastructure only when required by an implemented product capability.

