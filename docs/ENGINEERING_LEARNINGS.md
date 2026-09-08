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
