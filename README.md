# DevPulse

DevPulse is a production-quality Android project scaffold for a Kotlin, Jetpack Compose, and Material 3 application.

## Current Status

This repository currently contains the initial project structure plus Task 002 data-layer boundaries. It includes a minimal Compose application shell, GitHub remote DTO/API definitions, Room entity boundaries, domain models, data-layer mapping, repository contracts, and mapper tests. Product UI, navigation, authentication, full DAO/database implementation, and offline synchronization are intentionally not implemented yet.

## Planned Architecture

DevPulse is organized as a modular Android app with `:app` as the composition root. Feature modules remain independent from one another and consume shared foundations from `:core:*` modules. `:core:data` is the application data boundary that will coordinate network and local persistence later, while keeping DTOs, entities, and domain models separate.

## Module Overview

- `:app` - Android application module and composition root.
- `:core:common` - Shared utilities and common foundations.
- `:core:model` - Shared app/domain model contracts.
- `:core:network` - GitHub REST API interface, remote DTOs, and unauthenticated network configuration.
- `:core:database` - Room entity models for future local persistence.
- `:core:data` - Mapping functions, data errors, and repository contracts for future offline-first behavior.
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
