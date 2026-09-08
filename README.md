# DevPulse

DevPulse is a production-quality Android project for exploring GitHub developer and repository activity with Kotlin, Jetpack Compose, Material 3, and an offline-first modular architecture.

## Current Status

This repository currently contains the initial project structure plus the first Room-backed offline-first data layer. It includes a minimal Compose application shell, GitHub remote DTO/API definitions, Room entities and DAOs, domain models, data-layer mappings, repository contracts, repository implementations, cache freshness metadata, and data-layer tests.

Product UI, ViewModels, navigation, authentication, full GitHub pagination, Paging 3, WorkManager, and background sync are intentionally not implemented yet.

## Planned Architecture

DevPulse is organized as a modular Android app with `:app` as the composition root. Feature modules remain independent from one another and consume shared foundations from `:core:*` modules. `:core:data` is the application data boundary that coordinates GitHub network refreshes with Room persistence while keeping DTOs, entities, and domain models separate.

Reads come from Room-backed `Flow`s. Refresh operations update Room and return `DataResult<Unit>`, allowing cached data to remain observable even when a network request fails.

## Module Overview

- `:app` - Android application module and composition root.
- `:core:common` - Shared utilities and common foundations.
- `:core:model` - Shared app/domain model contracts.
- `:core:network` - GitHub REST API interface, remote DTOs, and unauthenticated network configuration.
- `:core:database` - Room database, DAOs, entities, saved-state persistence, and synchronization metadata.
- `:core:data` - Repository contracts and implementations, mapping functions, data errors, cache freshness policy, and synchronization transactions.
- `:core:designsystem` - Shared Compose Material 3 theme and design primitives.
- `:core:testing` - Shared testing utilities and test dependencies.
- `:feature:search` - Future search feature boundary.
- `:feature:developer` - Future developer profile/details feature boundary.
- `:feature:repository` - Future repository feature boundary.
- `:feature:saved` - Future saved items feature boundary.

## Build

```sh
./gradlew assembleDebug
./gradlew test
```
