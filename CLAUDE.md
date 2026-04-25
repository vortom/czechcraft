# CzechCraft — guidance for Claude

Minecraft Fabric mod (Java 21, MC 1.21.1). Multi-loader-ready architecture: `common/` (loader-agnostic, vanilla MC only) + `fabric/` (Fabric entry layer). Future `neoforge/` mirrors `fabric/`.

Spec: `docs/superpowers/specs/2026-04-25-czechcraft-design.md`. Plan: `docs/superpowers/plans/2026-04-25-czechcraft-v1.md`. Read these before non-trivial work.

## Architectural rules (compiler-enforced — don't break them)
- `common/` Java may import only `net.minecraft.*` + `org.slf4j.*`. NO `net.fabricmc.*`. Compile fails if violated (Fabric API isn't on `common/`'s classpath).
- Cross-loader assets (textures) → `common/src/main/resources/`. Fabric-only metadata (`fabric.mod.json`, mixin config) → `fabric/src/main/resources/`. Datagen output → `fabric/src/main/generated/` (committed).
- The `RegistryHelper` interface in `common/platform/` is the only seam between `common/` and loaders. Keep it minimal; each loader supplies a concrete impl.

## Build & dev commands
- `./gradlew build` — full build incl. tests + Spotless. First run: 10+ min (Loom downloads MC). After: seconds.
- `./gradlew :fabric:runClient` — launch dev Minecraft client with the mod.
- `./gradlew :fabric:runDatagen` — regenerate recipe/model/lang JSON. Commit the resulting `fabric/src/main/generated/` changes (excluding `.cache/`, which is gitignored).
- `./gradlew spotlessApply` — auto-format Java to Google Java Format (2-space). CI runs `spotlessCheck` and fails on violations.
- All version pins (MC, Yarn, Loader, Fabric API, mod_version, Java) live in `gradle.properties`. Bump there, nowhere else.

## Gotchas (encountered the hard way)
- **No JUnit tests in `common/`.** Plain JUnit can't run anything that touches `Item`/`FoodComponent` — Fabric Loader's runtime bytecode rewriting (which makes `RegistryEntry$Reference.setRegistryKey` accessible cross-package) isn't active in plain JUnit. Verify behavior via (a) compilation, (b) `fabric/`'s file-system datagen tests in `DatagenOutputTest`, (c) manual `runClient`.
- **Loom multi-module:** the `fabric/` module must depend on `common` via `implementation project(path: ":common", configuration: "namedElements")` — plain `project(":common")` resolves the intermediary jar and Knot can't load Yarn-named MC classes at runtime.
- **Gradle plugin classloader:** plugins applied only in subprojects (e.g. `com.modrinth.minotaur`) can collide with fabric-loom-SNAPSHOT's `BuildSharedServiceManager`. Pattern: declare in root `build.gradle` with `apply false`, apply (without version) in the subproject.
- **`processResources.expand` cache:** every key used in `expand` must also be declared via `inputs.property` — otherwise stale outputs survive `gradle.properties` changes silently.
- **`fabric/src/main/generated/.cache/`** files contain timestamps that re-dirty on every datagen run — gitignored by design.

## Adding new content (extension pattern)
1. New item class in `common/src/main/java/cz/czechcraft/content/<category>/<Name>.java` with `public static final Item ...` + a `<NAME>_PATH = "..."` constant.
2. One line in `ModItems` static initialiser: `add(<Name>.<NAME>_PATH, <Name>.<INSTANCE>);`.
3. Provider entries: recipe in `ModRecipeProvider.generate`, lang in `ModEnglishLangProvider`/`ModCzechLangProvider`, model in `ModModelProvider.generateItemModels`.
4. Texture at `common/src/main/resources/assets/czechcraft/textures/item/<path>.png` (16×16 PNG).
5. `./gradlew :fabric:runDatagen` and commit the regenerated files.

Creative tab auto-populates from `ModItems.getAll()` — don't touch `CzechCraftItemGroupFabric`.

## Releasing
1. Bump `mod_version` in `gradle.properties`.
2. Add a section to `CHANGELOG.md` matching the new tag (the release workflow extracts that section as the GitHub-Release body).
3. Tag `v<x.y.z>` and push the tag — `release.yml` builds, verifies datagen drift, publishes to Modrinth, creates a GitHub Release.

Prereq: `MODRINTH_TOKEN` repo secret set (Modrinth PAT with "Create version" scope).

## Conventions
- Java package root: `cz.czechcraft`. Mod ID: `czechcraft`. Modrinth slug: `czechcraft`. GitHub: `vortom/czech-craft`.
- Commit prefixes used: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `chore`, `build`, `ci`, `style`. Match this style.
- License: MIT.
- **Visual assets** (item textures, mod icon, etc.): see `docs/design/asset-handbook.md` before suggesting any art change. Validate with `python3 scripts/check-asset.py --all`. Item textures must be 16×16, ≤8 colors, binary alpha; mod icon must be 128×128, ≤64 colors. v1 placeholder assets predate the handbook and intentionally fail validation — issue #3 tracks bringing them into compliance.
