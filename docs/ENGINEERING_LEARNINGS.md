# Engineering Learnings

This file records project-shaping decisions that should carry forward into DevPulse and similar future Android projects.

## 2026-09-08: Keep Model Contracts Independent

` :core:model` should start dependency-free. Model contracts are easiest to share, test, and reason about when they do not inherit utility or platform assumptions from lower-level modules.

Add a dependency from `:core:model` only when a concrete model type needs a shared primitive that clearly belongs elsewhere.

## 2026-09-08: Commit In Small Honest Milestones

Commits should tell the story of the project without manufacturing false history. Prefer small, reviewable commits grouped by intent:

- project and build scaffold
- architecture and documentation
- boundary corrections or follow-up learning

## 2026-09-08: Add Infrastructure Only On Demand

Libraries such as Retrofit, Room, Hilt, and navigation should enter the project when a product slice requires them. The initial baseline should stay buildable and modular without committing to implementation choices too early.

