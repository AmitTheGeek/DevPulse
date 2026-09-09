# DevPulse Architecture

DevPulse is a modular Android application with `:app` as the composition root. Feature modules are isolated from each other and share cross-cutting foundations through `:core:*` modules.

## Module Layers

```text
:app
  -> :core:data
  -> :core:database
  -> :feature:search
  -> :feature:developer
  -> :feature:repository
  -> :feature:saved
  -> :core:designsystem
  -> :core:network

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
- `:app` is the composition root and may depend directly on core infrastructure modules for dependency graph assembly.
- Feature modules must not depend directly on other feature modules.
- Feature modules must not depend directly on `:core:network` or `:core:database`.
- Feature modules may depend on `:core:data` for application-level repository contracts.
- Shared contracts and reusable code belong in `:core:*`, not in feature modules.
- `:core:model` stays dependency-free from other project modules.
- Product-specific infrastructure should be added only when a feature needs it.
- `:core:network` owns remote API contracts and DTOs only.
- `:core:database` owns local persistence entities only.
- `:core:data` owns mapping, application-level error contracts, and repository interfaces.

## Composition Root And DI

Hilt is installed from `:app`, with `DevPulseApplication` as the generated application root and `MainActivity` as the Android entry point for the Compose tree.

Dependency bindings stay close to the module that owns the implementation:

- `:core:network` provides a singleton `OkHttpClient` and singleton `GitHubApi`.
- `:core:database` provides a singleton `DevPulseDatabase`; DAOs are provided from that database and share its lifetime.
- `:core:data` binds singleton repository implementations and provides the singleton app clock/cache policy.

Singleton scope is used for the network client, GitHub API, Room database, repositories, clock, and cache policy because each is process-wide infrastructure. DAOs are lightweight accessors backed by the singleton database.

Feature modules do not know how these dependencies are built. They depend on repository contracts from `:core:data` and receive implementations through constructor injection.

## Navigation Ownership

`:app` owns top-level navigation. Feature modules expose route-level composables and destination constants, but they do not navigate directly to each other and do not depend on sibling feature modules.

The current graph intentionally contains only:

```text
search -> developer/{username}
```

Navigation passes the developer username identifier only. Domain models are loaded by the destination from repositories so navigation arguments remain small, stable, serializable, and independent of cache shape.

## Model Boundaries

DevPulse keeps three model shapes separate:

- Remote DTOs live in `:core:network` and mirror the GitHub REST API JSON contract.
- Room entities live in `:core:database` and mirror local persistence needs.
- Domain models live in `:core:model` and represent app-level data consumed by features.

Network DTOs are not reused as Room entities or domain models. Room entities are not exposed as feature-facing models. `:core:data` maps between these representations and keeps DTO/entity dependencies as implementation details.

## Repository Responsibilities

`:core:data` defines and implements the first offline-first repository layer:

- `DeveloperRepository` observes and refreshes developer profiles.
- `RepositoryCatalog` observes developer repositories, observes a single repository, refreshes repository data, toggles saved state, and observes saved repositories.

Observation methods are shaped around the future local source of truth. Refresh operations return `DataResult<Unit>` because their job should be to fetch remote data, map it, and update local storage. UI-facing data should then flow from local observation APIs. This avoids split-brain behavior where a refresh returns one-off network data that can diverge from cached state.

## Room Source Of Truth

Room is the observable source of truth for DevPulse data. Repository observation APIs read from `DevPulseDatabase` using DAO `Flow` queries, map entities/projections into `:core:model` domain models, and expose those domain models to callers.

Refresh APIs follow a separate write path:

1. Call the GitHub REST API.
2. Map remote DTOs into Room entities.
3. Write the snapshot transactionally.
4. Store synchronization metadata.
5. Return `DataResult<Unit>`.

The network response is intentionally not exposed as a second observable source. If refresh succeeds, Room emits the new local state. If refresh fails, the existing cached data remains observable and the refresh returns a mapped `DevPulseError`.

## Remote-Owned And Local-Owned Data

Repository profile fields such as name, description, language, counts, archived/private flags, and update time are remote-owned GitHub data and live in `repositories`.

Saved state is locally owned and lives in a separate `saved_repositories` table keyed by repository id. Remote refreshes do not write a saved flag, and list replacement only removes missing repositories that are not saved. This keeps user intent from being cleared by a network snapshot.

Saved repository observation joins `saved_repositories` with `repositories`, so saved items are emitted when DevPulse has a local repository snapshot for them. A future detail refresh can hydrate saved ids that are known locally but missing repository details.

## Synchronization

`refreshDeveloper(username)` currently fetches the GitHub profile and the first repository page together, then writes both in one database transaction. This gives developer screens one cohesive manual refresh entry point and prevents profile/repository observations from updating halfway through a refresh. The trade-off is that a repository-list failure causes the whole developer refresh to fail; if future UI needs independent partial success, the contracts can split profile refresh and repository-list refresh while keeping Room as the only observable source.

Repository-list refreshes explicitly request `per_page=100` and `page=1`. This avoids silently relying on GitHub's 30-item default while keeping the API shape ready for a later pagination loop. Paging 3 and background sync are intentionally out of scope for now.

After a complete repository-list snapshot, unsaved repositories for that owner that are missing from the response are deleted. Saved repositories are preserved because saved state is locally owned. With the current single-page MVP implementation, a response containing fewer than 100 repositories is treated as a complete snapshot; a full 100-item page defers deletion until pagination metadata is available so DevPulse does not accidentally remove repositories that may exist on a later page.

## Cache Freshness

Synchronization metadata lives in `sync_metadata` as a simple key-value table of sync key to last refreshed epoch milliseconds. `CacheFreshnessPolicy` uses an injectable `DevPulseClock` and a five-minute TTL. That TTL is an application cache policy, not a GitHub freshness guarantee.

Refresh methods accept `RefreshPolicy`. `RefreshPolicy.IfStale` checks synchronization metadata before hitting GitHub, while `RefreshPolicy.Force` bypasses freshness checks for explicit user actions. ViewModels choose the user intent, but the time and staleness calculation stays in `:core:data`.

## Date And Time

Remote `updated_at` values remain strings in GitHub DTOs because that is the wire contract. Room stores repository update time as nullable epoch milliseconds for stable persistence and queries. The domain `Repository.updatedAt` is `java.time.Instant?`, which keeps app code typed without introducing Android framework date/time dependencies into `:core:model`.

## Error Handling

`:core:data` maps infrastructure failures into `DevPulseError`:

- network I/O failures -> `NetworkUnavailable`
- HTTP 404 -> `NotFound`
- GitHub rate-limit responses -> `RateLimited`, preserving status code, reset epoch seconds when available, and message
- HTTP 5xx -> `ServerError`
- local SQL failures -> `LocalStorageError`
- everything else -> `Unknown`

Rate-limited requests are not retried aggressively. Existing cached data remains available through Room observations after any refresh failure.

## Presentation State

Developer presentation follows unidirectional data flow:

- The route receives `username`.
- `DeveloperViewModel` observes Room-backed repository flows.
- The ViewModel exposes one immutable `StateFlow<DeveloperUiState>`.
- Compose renders from that state and calls ordinary ViewModel functions such as `onRefresh()` and `onRetry()`.

`DeveloperUiState` is a data class rather than one mutually exclusive sealed state because valid screen states can overlap. Cached content can be visible while a refresh is in progress, and cached content can remain visible with a non-destructive refresh error.

Feature-level UI errors map from `DevPulseError` without exposing Retrofit exceptions, SQL exceptions, DTOs, or Room entities to Compose.

## Current Vertical Slice

`:core:designsystem` provides `DevPulseTheme`, a small Material 3 theme wrapper. `:app` uses that theme and owns navigation between the Search and Developer feature routes.

Search collects and validates a GitHub username locally, then asks the app navigation layer to open the Developer destination. Search does not call GitHub just to navigate.

Developer observes cached Room data immediately, triggers an automatic `RefreshPolicy.IfStale` refresh through `DeveloperRepository`, and uses forced refresh for manual retry/refresh actions. Repository rows render from the cached list; repository detail and saved screens remain out of scope.

## Planned Direction

As DevPulse grows, the architecture should remain modular:

- UI and user journeys live in feature modules.
- Shared models live in `:core:model`.
- Reusable utilities live in `:core:common`.
- Network DTOs and Retrofit interfaces live in `:core:network`.
- Room entities, DAOs, and `DevPulseDatabase` live in `:core:database`.
- Repository contracts, mapping, error translation, cache freshness, and synchronization live in `:core:data`.
- Test utilities live in `:core:testing`.
- Dependency wiring, navigation, and application-level composition remain in `:app`.
