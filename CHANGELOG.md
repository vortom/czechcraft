# Changelog

All notable changes to CzechCraft are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-09-06

### Fixed
- **The mod now actually loads.** v1.0.0 shipped without any of the `common` module's classes (`CzechCraft`, `RegistryHelper`, `ModItems`, `ModItemGroups`, `FoodItems`), so Fabric crashed with `NoClassDefFoundError` on the first line of `CzechCraftFabric.onInitialize()` — on every Minecraft version, for every player. `implementation project(":common")` is a compile/runtime dependency and does not bundle classes into the jar; the loader jar now includes common's compiled output.

### Added
- `JarPackagingTest` — verifies the **built artifact** contains every class compiled into `common`, and that each `cz.czechcraft.*` class links using the jar alone. Runs on every `build`, so a release can no longer ship a jar that cannot start. The existing checks all resolved `common` through the dev classpath and were structurally blind to this.

## [1.0.0] - 2026-04-25

### Added
- **Rohlík** — Czech bread roll. Crafted from 2 wheat placed horizontally adjacent on a crafting table or 2×2 inventory grid. Restores 2 hunger drumsticks (nutrition 4, saturation modifier 0.3).
- **CzechCraft** creative tab containing all CzechCraft items.
- English (`en_us`) and Czech (`cs_cz`) translations.
- Fabric loader support for Minecraft 26.1.x (Java 25, Mojang Mappings).
- MultiLoader-Template architecture: `common/` module compiles against vanilla Minecraft only; `fabric/` is a thin entry layer. Future loader modules (NeoForge planned for v3) drop in without touching `common/` source.
- GitHub Actions: `build.yml` runs Spotless + tests + datagen verification on every push/PR; `release.yml` publishes to Modrinth and creates a GitHub Release on `v*` tags.

[Unreleased]: https://github.com/vortom/czechcraft/compare/v1.0.1...HEAD
[1.0.1]: https://github.com/vortom/czechcraft/releases/tag/v1.0.1
[1.0.0]: https://github.com/vortom/czechcraft/releases/tag/v1.0.0
