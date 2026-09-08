# DevPulse Architecture

DevPulse starts as a modular Android application with `:app` as the composition root. Feature modules are isolated from each other and share cross-cutting foundations through `:core:*` modules.

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
  -> :core:model
  -> :core:designsystem

:core:network
  -> :core:common
  -> :core:model

:core:database
  -> :core:common
  -> :core:model

:core:common
:core:model
:core:designsystem
:core:testing
```

## Dependency Rules

- `:app` owns app assembly and can depend on feature modules.
- Feature modules must not depend directly on other feature modules.
- Shared contracts and reusable code belong in `:core:*`, not in feature modules.
- Product-specific infrastructure should be added only when a feature needs it.
- `:core:network` and `:core:database` are intentionally empty of Retrofit, Room, and concrete implementations at this stage.

## Current UI Shell

`:core:designsystem` provides `DevPulseTheme`, a small Material 3 theme wrapper. `:app` uses that theme to display a single placeholder screen so the project has a buildable Compose entry point without product behavior.

## Planned Direction

As DevPulse grows, the architecture should remain modular:

- UI and user journeys live in feature modules.
- Shared models live in `:core:model`.
- Reusable utilities live in `:core:common`.
- Network and persistence implementations live in their dedicated core modules when selected.
- Test utilities live in `:core:testing`.
- Dependency wiring, navigation, and application-level composition remain in `:app`.
