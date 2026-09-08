# DevPulse

DevPulse is a production-quality Android project scaffold for a Kotlin, Jetpack Compose, and Material 3 application.

## Current Status

This repository currently contains only the initial project and module structure. It includes a minimal Compose application shell that renders a placeholder DevPulse screen using the shared design system theme. Product features, API clients, persistence, dependency injection, and navigation are intentionally not implemented yet.

## Planned Architecture

DevPulse is organized as a modular Android app with `:app` as the composition root. Feature modules remain independent from one another and consume shared foundations from `:core:*` modules. Infrastructure such as networking, database storage, and dependency injection should be introduced only when required by concrete product work.

## Module Overview

- `:app` - Android application module and composition root.
- `:core:common` - Shared utilities and common foundations.
- `:core:model` - Shared app/domain model contracts.
- `:core:network` - Future network boundary, with no client implementation yet.
- `:core:database` - Future persistence boundary, with no database implementation yet.
- `:core:designsystem` - Shared Compose Material 3 theme and design primitives.
- `:core:testing` - Shared testing utilities and test dependencies.
- `:feature:search` - Future search feature boundary.
- `:feature:developer` - Future developer profile/details feature boundary.
- `:feature:repository` - Future repository feature boundary.
- `:feature:saved` - Future saved items feature boundary.

## Build

```sh
./gradlew assembleDebug
```

