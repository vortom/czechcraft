# Changelog

All notable changes to CzechCraft are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- `release.yml` creates the GitHub Release *before* touching Modrinth version metadata, and the metadata step now only verifies rather than writing. That write was redundant — minotaur already uploads with `environment=client_and_server` — but it failed on both the 1.0.1 and 1.1.0 releases after the jar was already public, skipping GitHub Release creation and needing manual repair each time.

## [1.1.0] - 2026-09-06

### Added
- **Minecraft 26.2 support.** A single jar now covers 26.1.2 and 26.2 — no second download. Verified by running the published jar as a real mod on a dedicated server for each version: the item registers and resolves via `give`, and an A/B recipe count (mod present vs absent) confirms the recipe and its advancement load on both.

### Fixed
- **Modrinth no longer advertises versions the mod rejects.** 26.1 and 26.1.1 were listed but never worked — the declared range has always been `>=26.1.2`, so the loader refused them. They are removed from the listing rather than silently handed a jar that cannot start.
- Declared Fabric API dependency tightened from `*` (any version at all) to a real minimum.

### Changed
- The supported Minecraft range is now `minecraft_supported_range` in `gradle.properties`, separate from `minecraft_version` (the compile target). Conflating the two produced the original "requires 26.1.2 but 26.2 is present" report. The Modrinth game-version list is generated from the same property, and `SupportedVersionsTest` asserts — using Fabric Loader's own `VersionPredicate` — that everything advertised is actually accepted, and that versions known to break stay excluded.

### Not supported
- **Minecraft 26.3.** The Java code runs there, but 26.3 renamed the `recipe_unlocked` advancement trigger field (`recipe` → `recipes`), so our generated advancement fails to parse and the game **refuses to load its datapacks at all**. Supporting 26.3 requires regenerating datagen against it, not widening a range.

### Fixed
- `release.yml` now retries the Modrinth version-ID lookup instead of failing on the first miss. Modrinth is not read-your-writes consistent: during the 1.0.1 release the lookup ran ~300 ms after upload, found nothing, and aborted the workflow *after* the jar was already public — skipping GitHub Release creation.
- `release.yml` refuses to publish a version number that already exists on Modrinth. Re-running a release previously re-uploaded silently, which is how 1.0.0 came to be listed twice.

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

[Unreleased]: https://github.com/vortom/czechcraft/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/vortom/czechcraft/releases/tag/v1.1.0
[1.0.1]: https://github.com/vortom/czechcraft/releases/tag/v1.0.1
[1.0.0]: https://github.com/vortom/czechcraft/releases/tag/v1.0.0
