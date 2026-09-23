# DevPulse Project Brief

DevPulse is a production-quality Android application for exploring GitHub developer and repository activity. The current MVP is a modular Kotlin, Jetpack Compose, Material 3 app with Room as the offline-first source of truth and Hilt for dependency injection.

## Current MVP

- Search: enter a GitHub username and navigate without making a network call from the search screen.
- Developer Dashboard: observe cached developer and repository data, then refresh from GitHub when stale or when the user explicitly retries.
- Repository Detail: observe one cached repository, refresh it through the data layer, save or unsave it locally, and open its GitHub URL.
- Saved Repositories: show locally saved repositories from Room and allow unsaving without requiring a network refresh on entry.

## Data And Refresh Model

Reads come from Room-backed `Flow`s exposed through repository contracts in `:core:data`. Refresh operations call GitHub, map remote DTOs into Room entities, update Room transactionally, and return `DataResult<Unit>` so UI updates still come from the local source of truth.

Refresh policy is handled in the data layer with synchronization metadata, an injectable clock, and a five-minute cache TTL. Automatic refreshes use stale-aware checks, while manual refresh actions can force a network call.

Saved state is locally owned in a dedicated saved-repository table. Remote repository-list refreshes preserve saved repositories even if a later GitHub owner-list response no longer includes them; unsaving a known-missing cached repository allows cleanup.

## Current Limitations

- GitHub access is unauthenticated.
- Repository list refresh currently requests page 1 with up to 100 repositories; full pagination is not implemented yet.
- Paging 3, WorkManager, background sync, README fetching, commit history, contributors, releases, and screenshots are intentionally out of scope.
- Cached saved repository details may be stale until a future individual refresh succeeds.
