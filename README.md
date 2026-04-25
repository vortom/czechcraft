# CzechCraft

> Czech-themed food and drinks for Minecraft.

[![Build](https://github.com/vortom/czech-craft/actions/workflows/build.yml/badge.svg)](https://github.com/vortom/czech-craft/actions/workflows/build.yml)
[![Modrinth](https://img.shields.io/modrinth/dt/czechcraft?label=Modrinth)](https://modrinth.com/mod/czechcraft)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## What's in v1

**Rohlík** — a traditional Czech bread roll. Crafted from 2 wheat placed horizontally adjacent on a crafting table or in your 2×2 inventory grid. Restores 1 hunger drumstick (nutrition 2, saturation 0.3 modifier).

## Compatibility

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | Fabric (NeoForge planned for v3) |
| Java | 21 |
| Required deps | [Fabric API](https://modrinth.com/mod/fabric-api) |

Compatible with Sodium, Iris, and other QoL/optimisation mods on Fabric.

## Installation (players)

1. Install the [Fabric loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Download the latest `czechcraft-X.Y.Z.jar` from [Modrinth](https://modrinth.com/mod/czechcraft) or [GitHub Releases](https://github.com/vortom/czech-craft/releases).
4. Drop it into `.minecraft/mods/`.

## Development

### Prerequisites
- JDK 21 (Temurin recommended).
- Git.

### Build
```bash
./gradlew build
```
Outputs `fabric/build/libs/czechcraft-<version>.jar`.

### Run a dev client
```bash
./gradlew :fabric:runClient
```

### Regenerate datagen output
```bash
./gradlew :fabric:runDatagen
```
Edits to recipes / models / translations require re-running datagen and committing the regenerated files in `fabric/src/main/generated/`.

### Run tests + format check
```bash
./gradlew spotlessCheck build test
```

### Apply formatting
```bash
./gradlew spotlessApply
```

## Architecture

CzechCraft uses the **MultiLoader-Template** structure:

- **`common/`** — loader-agnostic. Contains all gameplay logic (item definitions, registries, creative tab metadata) and compiles against vanilla Minecraft only. The compiler enforces this — Fabric API is not on the `common/` classpath.
- **`fabric/`** — thin Fabric-specific entry layer. Provides a `RegistryHelper` implementation, a `ModInitializer`, and the Fabric datagen pipeline. About 5 small classes total.

A future `neoforge/` module mirrors `fabric/` with no changes to `common/`.

See [`docs/superpowers/specs/2026-04-25-czechcraft-design.md`](docs/superpowers/specs/2026-04-25-czechcraft-design.md) for the full design.

## Adding a new item (extension guide)

To add e.g. **Pivo** (Czech beer):

1. Create `common/src/main/java/cz/czechcraft/content/drink/Drinks.java` with `public static final Item PIVO = new Item(new Item.Settings()...)`.
2. Add one line in `common/src/main/java/cz/czechcraft/registry/ModItems.java` static initialiser: `add("pivo", Drinks.PIVO);`.
3. Add a recipe in `fabric/src/main/java/cz/czechcraft/fabric/datagen/ModRecipeProvider.java`.
4. Add lang strings in `ModEnglishLangProvider` and `ModCzechLangProvider`.
5. Drop a 16×16 PNG in `common/src/main/resources/assets/czechcraft/textures/item/pivo.png`.
6. `./gradlew :fabric:runDatagen` then commit.

The creative tab auto-populates from `ModItems.getAll()`. No other files change.

## Releasing

1. Bump `mod_version` in `gradle.properties`.
2. Update `CHANGELOG.md` with a new section for this version.
3. Commit, tag (`git tag v1.2.3`), push tag (`git push origin v1.2.3`).
4. The Release workflow builds the jar, publishes to Modrinth (using the `MODRINTH_TOKEN` repo secret), and creates a GitHub Release.

## License

[MIT](LICENSE) © 2026 tomas.vorel and CzechCraft contributors.
