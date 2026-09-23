# DevPulse

DevPulse is a production-quality Android project for exploring GitHub developer and repository activity with Kotlin, Jetpack Compose, Material 3, and an offline-first modular architecture.

## Current Status

This repository currently contains the initial project structure, the first Room-backed offline-first data layer, Hilt dependency injection, and the primary MVP navigation: Explore, Developer Dashboard, Repository Detail, and Saved Repositories.

Authentication, full GitHub pagination, Paging 3, WorkManager, background sync, README fetching, commit history, contributors, and releases are intentionally not implemented yet.

## Planned Architecture

DevPulse is organized as a modular Android app with `:app` as the composition root. Feature modules remain independent from one another and consume shared foundations from `:core:*` modules. `:core:data` is the application data boundary that coordinates GitHub network refreshes with Room persistence while keeping DTOs, entities, and domain models separate.

Reads come from Room-backed `Flow`s. Refresh operations update Room and return `DataResult<Unit>`, allowing cached data to remain observable even when a network request fails. `:app` owns top-level navigation and dependency graph assembly; feature modules expose routes and stay independent from each other.

Saved repository state is locally owned. Owner-list synchronization preserves saved cached repositories even when they disappear from a later list response, and unsaving allows known-missing cached rows to be cleaned up.

## Module Overview

- `:app` - Android application module, Hilt root, and top-level Compose navigation owner.
- `:core:common` - Shared utilities and common foundations.
- `:core:model` - Shared app/domain model contracts.
- `:core:network` - GitHub REST API interface, remote DTOs, unauthenticated network configuration, and network DI.
- `:core:database` - Room database, DAOs, entities, saved-state persistence, synchronization metadata, and database DI.
- `:core:data` - Repository contracts and implementations, mapping functions, data errors, cache freshness policy, synchronization transactions, and repository DI.
- `:core:designsystem` - Shared Compose Material 3 theme and design primitives.
- `:core:testing` - Shared testing utilities and test dependencies.
- `:feature:search` - Search route and username submission UI.
- `:feature:developer` - Developer Dashboard route, ViewModel, screen state, and cached repository list UI.
- `:feature:repository` - Repository Detail route, ViewModel, state, save/unsave actions, and GitHub link action.
- `:feature:saved` - Saved repositories route, ViewModel, local saved list UI, unsave action, and repository navigation.

## Build

```sh
./gradlew assembleDebug
./gradlew test
```
