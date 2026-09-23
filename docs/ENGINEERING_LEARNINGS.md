# Engineering Learnings

This file records project-shaping decisions that should carry forward into DevPulse and similar future Android projects.

## 2026-09-08: Keep Model Contracts Independent

`:core:model` should start dependency-free. Model contracts are easiest to share, test, and reason about when they do not inherit utility or platform assumptions from lower-level modules.

Add a dependency from `:core:model` only when a concrete model type needs a shared primitive that clearly belongs elsewhere.

## 2026-09-08: Commit In Small Honest Milestones

Commits should tell the story of the project without manufacturing false history. Prefer small, reviewable commits grouped by intent:

- project and build scaffold
- architecture and documentation
- boundary corrections or follow-up learning

## 2026-09-08: Add Infrastructure Only On Demand

Libraries such as Retrofit, Room, Hilt, and navigation should enter the project when a product slice requires them. The initial baseline should stay buildable and modular without committing to implementation choices too early.

## 2026-09-08: Separate DTOs, Entities, And Domain Models Early

Remote DTOs should mirror the API contract, Room entities should mirror local storage, and domain models should mirror app needs. Keeping these shapes separate adds a little mapping code up front, but it prevents feature modules from coupling to network or database implementation details.

## 2026-09-08: Make Local Storage The Observable Source

Offline-first repositories should expose Room-backed `Flow`s for reads and keep refresh APIs as write operations that return `DataResult<Unit>`. This prevents network responses from becoming a competing source of UI truth and lets stale-but-valid cached data remain visible when refreshes fail.

## 2026-09-08: Model Data Ownership Explicitly

Remote-owned repository fields and locally owned saved state should not share the same persistence field. A separate saved-repository table makes user intent durable across remote refreshes and keeps list replacement logic honest: remove missing unsaved remote rows, preserve saved rows.

## 2026-09-08: Keep Freshness Policy Testable

Cache freshness belongs in the data layer, not in ViewModels. Store refresh metadata in Room, inject a small clock abstraction, and treat TTL values as application policy. This keeps refresh-if-stale behavior deterministic in tests and easy to bypass for future manual refresh actions.

## 2026-09-08: Only Delete Missing Rows From Complete Snapshots

Replacement sync is safe only when the local layer knows it has a complete remote snapshot. For paginated APIs, a full page should not trigger stale-row deletion unless the pagination layer has confirmed there is no next page.

## 2026-09-09: Keep DI Bindings Near Ownership

Hilt modules should live near the code they construct: network bindings in `:core:network`, database bindings in `:core:database`, data/repository bindings in `:core:data`, and only app entry points in `:app`. This keeps the graph understandable as modules grow.

## 2026-09-09: Navigate With Identifiers

Navigation should pass stable identifiers, not domain objects or persistence shapes. The destination can observe its own data from repositories, which keeps navigation small and prevents stale object snapshots from bypassing the source of truth.

## 2026-09-09: Model Cached Content And Refresh Status Together

Screen state should allow cached content, loading, and refresh errors to coexist. A single immutable UI state data class can represent content plus a non-destructive refresh failure more naturally than a fully exclusive sealed state hierarchy.

## 2026-09-09: Match Library Versions To The Project Toolchain

Choosing the latest library blindly can pull transitive artifacts that require a newer compile SDK or Android Gradle Plugin. Prefer versions that fit the project toolchain unless the task explicitly upgrades that toolchain; document the compatibility reason.

## 2026-09-23: Local Intent Must Outlive Remote Snapshots

Saved repository state is user intent, not GitHub-owned data. Owner-list replacement may delete missing unsaved rows, but it must preserve saved rows and saved intent. Marking a saved row as missing from the owner list gives the app enough information to clean it up later when the user unsaves without silently discarding saved content during synchronization.

## 2026-09-23: Saved Screens Should Be Local-First

Opening Saved should not require network access. Saved is a view over durable local intent plus cached repository details; refresh belongs to Repository Detail or explicit future sync surfaces, not to basic saved-list entry.

## 2026-09-23: Shared Destinations Still Belong To App Navigation

Repository Detail can be reached from Developer or Saved, but feature modules still should not depend on each other. Pass owner/name callbacks up to `:app`, let `:app` navigate, and let Repository Detail load its own state from `RepositoryCatalog`.

## 2026-09-23: Format Typed Domain Values At Presentation Edges

Keep domain values typed, such as `Repository.updatedAt: Instant?`, and format them in presentation-specific mappers or formatters. This keeps `:core:model` free of Android formatting concerns while making UI output testable.
