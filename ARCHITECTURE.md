# DevPulse Architecture

DevPulse is a modular Android application with `:app` as the composition root. Feature modules are isolated from each other and share cross-cutting foundations through `:core:*` modules.

## Module Layers

```text
:app
  -> :feature:search
  -> :feature:developer
  -> :feature:repository
  -> :feature:saved
  -> :core:designsystem

:feature:*
  -> :core:common
  -> :core:data
  -> :core:model
  -> :core:designsystem

:core:data
  -> :core:model
  -> :core:network
  -> :core:database

:core:network
  -> Retrofit, OkHttp, Kotlin Serialization

:core:database
  -> Room annotations

:core:common
:core:model
:core:designsystem
:core:testing
```

## Dependency Rules

- `:app` owns app assembly and can depend on feature modules.
- Feature modules must not depend directly on other feature modules.
- Feature modules must not depend directly on `:core:network` or `:core:database`.
- Feature modules may depend on `:core:data` for application-level repository contracts.
- Shared contracts and reusable code belong in `:core:*`, not in feature modules.
- `:core:model` stays dependency-free from other project modules.
- Product-specific infrastructure should be added only when a feature needs it.
- `:core:network` owns remote API contracts and DTOs only.
- `:core:database` owns local persistence entities only.
- `:core:data` owns mapping, application-level error contracts, and repository interfaces.

## Model Boundaries

DevPulse keeps three model shapes separate:

- Remote DTOs live in `:core:network` and mirror the GitHub REST API JSON contract.
- Room entities live in `:core:database` and mirror local persistence needs.
- Domain models live in `:core:model` and represent app-level data consumed by features.

Network DTOs are not reused as Room entities or domain models. Room entities are not exposed as feature-facing models. `:core:data` maps between these representations and keeps DTO/entity dependencies as implementation details.

## Repository Responsibilities

`:core:data` defines repository contracts for future offline-first behavior:

- `DeveloperRepository` observes and refreshes developer profiles.
- `RepositoryCatalog` observes developer repositories, observes a single repository, refreshes repository data, toggles saved state, and observes saved repositories.

Observation methods are shaped around the future local source of truth. Refresh operations return `DataResult<Unit>` because their job should be to fetch remote data, map it, and update local storage. UI-facing data should then flow from local observation APIs. This avoids split-brain behavior where a refresh returns one-off network data that can diverge from cached state.

## Current UI Shell

`:core:designsystem` provides `DevPulseTheme`, a small Material 3 theme wrapper. `:app` uses that theme to display a single placeholder screen so the project has a buildable Compose entry point without product behavior.

## Planned Direction

As DevPulse grows, the architecture should remain modular:

- UI and user journeys live in feature modules.
- Shared models live in `:core:model`.
- Reusable utilities live in `:core:common`.
- Network DTOs and Retrofit interfaces live in `:core:network`.
- Room entities and future DAOs/database implementation live in `:core:database`.
- Repository contracts, mapping, error translation, and future synchronization live in `:core:data`.
- Test utilities live in `:core:testing`.
- Dependency wiring, navigation, and application-level composition remain in `:app`.
