# CzechCraft — guidance for Claude

Minecraft Fabric mod (Java 25, MC 26.1.x). Multi-loader-ready architecture: `common/` (loader-agnostic, vanilla MC only) + `fabric/` (Fabric entry layer). Future `neoforge/` mirrors `fabric/`.

Spec: `docs/superpowers/specs/2026-04-25-czechcraft-design.md`. Plan: `docs/superpowers/plans/2026-04-25-czechcraft-v1.md`. MC 26.1 port plan: `docs/superpowers/plans/2026-05-01-mc-26.1-port.md`. Read these before non-trivial work.

## Architectural rules (compiler-enforced — don't break them)
- `common/` Java may import only `net.minecraft.*` + `org.slf4j.*`. NO `net.fabricmc.*`. Compile fails if violated (Fabric API isn't on `common/`'s classpath).
- Cross-loader assets (textures) → `common/src/main/resources/`. Fabric-only metadata (`fabric.mod.json`, mixin config) → `fabric/src/main/resources/`. Datagen output → `fabric/src/main/generated/` (committed).
- The `RegistryHelper` interface in `common/platform/` is the only seam between `common/` and loaders. Keep it minimal; each loader supplies a concrete impl.
- Items must be constructed with `new Item.Properties().setId(ResourceKey<Item>)` *before* registration — the 26.1 unobfuscation made `setId` mandatory at construction time. `ModItems` uses a factory pattern (`Function<ResourceKey<Item>, Item>`) so each item knows its key at birth.

## Build & dev commands
- `./gradlew build` — full build incl. tests + Spotless. First run: 10+ min (Loom downloads MC + JDK 25 via toolchain). After: seconds.
- `./gradlew :fabric:runClient` — launch dev Minecraft client with the mod.
- `./gradlew :fabric:runDatagen` — regenerate recipe/model/lang JSON. Datagen runs in CLIENT environment (model providers are now client-only in 26.1). Commit the resulting `fabric/src/main/generated/` changes (excluding `.cache/`, which is gitignored).
- `./gradlew spotlessApply` — auto-format Java to Google Java Format (2-space). CI runs `spotlessCheck` and fails on violations.
- All version pins (MC, Loader, Loom, Fabric API, mod_version, Java) live in `gradle.properties`. Bump there, nowhere else. (No `yarn_mappings` line — MC 26.1+ ships unobfuscated.)

## Gotchas (encountered the hard way)
- **Datagen output — not Java — is what limits our Minecraft version span.** A 26.1.2-built jar runs unmodified on 26.2 (verified in-game: item registers, recipe and advancement load). It breaks on 26.3 only because 26.3 renamed the `recipe_unlocked` trigger field (`recipe` → `recipes`), so the generated advancement fails to parse and the game **refuses to load datapacks at all** — the server won't boot. Supporting a new MC version therefore means regenerating datagen against it, which is a separate artifact, not a wider range. Corollary: never widen `minecraft_supported_range` without first running the version smoke test (spin up a Loom project for the target version with the built jar in `run/mods/`, then `give @a czechcraft:rohlik` and A/B the recipe count with the mod present vs absent). An unverified range is worse than a narrow one — it hands players a mod that stops their game starting.
- **`minecraft_version` is the compile target; `minecraft_supported_range` is the compatibility claim.** Keep them separate. Conflating them (`"minecraft": "~${minecraft_version}"`) caused the original 26.2 incompatibility report. Also keep `minecraft_supported_versions` (the Modrinth list) honest — it advertised 26.1/26.1.1 for two releases while the range rejected them. `SupportedVersionsTest` enforces all of this with Fabric Loader's own `VersionPredicate`.
- **Dev classpath ≠ shipped jar — a green build is NOT evidence the artifact works.** `implementation project(":common")` compiles and runs against common but does **not** bundle its classes into the mod jar. `runClient`, `runDatagen`, CI `build` and every JUnit test resolve common through the project dependency, so all of them pass even when the artifact is missing those classes entirely. That is exactly how v1.0.0 shipped unable to start on any MC version (`NoClassDefFoundError` on the first line of `onInitialize`). `fabric/build.gradle`'s `jar { from project(":common").sourceSets.main.output.classesDirs }` does the bundling; `JarPackagingTest` guards it by opening the built jar. When adding a new module or moving classes between modules, verify the jar contents, not the build's exit code.
- **JUnit tests live in `fabric/src/test/`, not `common/`.** With unobfuscated 26.1, `Item`/`FoodProperties` instantiation works in plain JUnit if you bootstrap with `SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();` (`net.minecraft.server.Bootstrap`). The `fabric/` test classpath has the full Loader+Mixin runtime via Loom. Pure `common/test/` would require manually wiring Loader+Mixin+ASM, so prefer `fabric/test/` and reference common classes through the project dependency.
- **`Item.components()` returns null pre-registration.** Unit tests can construct items via factories but cannot inspect data components on them — those bind only after `Registry.register`. Verify component values via `runClient` smoke test, not JUnit.
- **Datagen needs `inherit client`** in the Loom run config — `FabricModelProvider` moved to the `net.fabricmc.fabric.api.client.datagen.v1.provider` package and crashes in SERVER environment.
- **Loom multi-module:** plain `implementation project(":common")` resolves correctly in 26.1 for *compilation* (unobfuscated MC has no `namedElements` configuration to worry about) — but it does not package. See the dev-classpath gotcha above.
- **Gradle plugin classloader:** plugins applied only in subprojects (e.g. `com.modrinth.minotaur`) can collide with fabric-loom-SNAPSHOT's `BuildSharedServiceManager`. Pattern: declare in root `build.gradle` with `apply false`, apply (without version) in the subproject.
- **`processResources.expand` cache:** every key used in `expand` must also be declared via `inputs.property` — otherwise stale outputs survive `gradle.properties` changes silently.
- **`fabric/src/main/generated/.cache/`** files contain timestamps that re-dirty on every datagen run — gitignored by design.
- **JDK toolchain auto-provisioning:** Gradle 9 doesn't bundle the foojay resolver. We declare it in `settings.gradle` so CI runners without JDK 25 can auto-download one.
- **Configuration cache must be off** (`org.gradle.configuration-cache=false` in `gradle.properties`) — IntelliJ + Loom 1.15 combo isn't fully compatible. See fabric-loom #1349.
- **Mixins not in use yet** — when adding any mixin, verify Mixin's ASM version supports JDK 25 bytecode. The empty `czechcraft.mixins.json` ships at `compatibilityLevel: JAVA_25` so the wiring is ready, but no actual transformer has been exercised against the new bytecode yet.

## Adding new content (extension pattern)
1. New factory method in `common/src/main/java/cz/czechcraft/content/<category>/<Name>.java`: `public static Item create<Name>(ResourceKey<Item> key) { return new Item(new Item.Properties().setId(key)...); }` + a `<NAME>_PATH = "..."` constant.
2. One line in `ModItems` static fields: `public static final Item <NAME> = add(<Name>.<NAME>_PATH, <Name>::create<Name>);`.
3. Provider entries: recipe in `ModRecipeProvider.createRecipeProvider`'s inner `RecipeProvider`, lang in `ModEnglishLangProvider`/`ModCzechLangProvider`, model in `ModModelProvider.generateItemModels`.
4. Texture at `common/src/main/resources/assets/czechcraft/textures/item/<path>.png` (16×16 PNG).
5. `./gradlew :fabric:runDatagen` and commit the regenerated files.

Creative tab auto-populates from `ModItems.getAll()` — don't touch `CzechCraftItemGroupFabric`.

## Releasing
1. Bump `mod_version` in `gradle.properties`.
2. Add a section to `CHANGELOG.md` matching the new tag (the release workflow extracts that section as the GitHub-Release body).
3. Tag `v<x.y.z>` and push the tag — `release.yml` builds, verifies datagen drift, refuses to publish a version number that already exists, publishes to Modrinth, syncs the listing body, creates the GitHub Release, and *finally* asserts the per-version environment metadata.
4. **Before tagging, if the supported Minecraft range changed:** re-run the version smoke test (see the datagen gotcha). `SupportedVersionsTest` catches an inconsistent *declaration*, but only a real server run proves the game actually accepts the jar.

### Prereqs
- `MODRINTH_TOKEN` repo secret set (Modrinth PAT with **two** scopes: `Versions → Create versions` AND `Projects → Write projects`). Missing the second scope makes the body-sync step fail. The env-metadata step now only reads.
- The GitHub repo must be **public** — Modrinth's Content Rules §5.4 requires the Source link to point at a publicly-reachable resource.

### Modrinth listing conventions
- `README.md` is **developer-facing** (build, architecture, extension guide). `MODRINTH.md` is **player-facing** (features, install, recipe) — `fabric/build.gradle`'s `syncBodyFrom` points at `MODRINTH.md`.
- `modrinth` (jar upload) and `modrinthSyncBody` (body push) are **two separate minotaur tasks**. `release.yml` runs both explicitly, then verifies the body landed.
- **Per-version environment metadata** is set by minotaur on upload — `release.yml` only *asserts* it reads back as `client_and_server`. It used to PATCH the value via the v3 API; that was redundant and broke two consecutive releases (v1.0.1 on a read-your-writes race, v1.1.0 on an HTTP error), each time *after* the jar was public. The v1.1.0 failure proved the write was pointless: the PATCH errored and the field was still correct. If a future module becomes one-sided, change the expected value in the assertion — do not reintroduce a write.
- **Step order matters.** Anything that merely inspects already-published state runs *after* `Create GitHub Release`. Both partial-release incidents happened because a verification step sat in front of it and skipped it on failure.

### Recovering a half-finished release
Publishing spans two services and is not atomic. If the Modrinth upload succeeds but a later step fails:
1. **Do not re-run the workflow.** It would attempt to re-upload the same version number; the duplicate guard will block it, and re-running is how 1.0.0 ended up listed twice in the first place.
2. Check what actually landed: `curl -s https://api.modrinth.com/v2/project/czechcraft/version` and `gh release list`.
3. If the GitHub Release is missing, create it from the *published* jar so the artifacts match byte-for-byte:
   `curl -sL -o czechcraft-<v>.jar <cdn url from the API>` then
   `gh release create v<x.y.z> --title "v<x.y.z>" --notes-file <changelog section> czechcraft-<v>.jar`
4. Fix the failing step's cause before the next release. The run stays red in history — GitHub cannot retroactively pass it, and that is cosmetic.

## Conventions
- Java package root: `cz.czechcraft`. Mod ID: `czechcraft`. Modrinth slug: `czechcraft`. GitHub: `vortom/czechcraft`.
- Commit prefixes used: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `chore`, `build`, `ci`, `style`. Match this style.
- License: MIT.
- **Visual assets** (item textures, mod icon, etc.): see `docs/design/asset-handbook.md` before suggesting any art change. Validate with `python3 scripts/check-asset.py --all`. The handbook adopted the designer's illustration-style as canonical in v1; the `check-asset.py` validator enforces dimensions/filename/file-size only.
