# CzechCraft — Design Spec (v1)

**Date:** 2026-04-25
**Status:** Approved
**Authors:** Someone_Cz (with Claude)
**Scope:** v1 — Fabric-only, single item (Rohlík). Architecture is multi-loader-ready so NeoForge (v3) and additional content (Pivo in v2) drop in without rewrites.

---

## 1. Goals & non-goals

### Goals
- Ship a working Fabric mod for Minecraft 1.21.1 that adds **Rohlík**, a Czech bread-roll food item crafted from 2 wheat placed horizontally adjacent on a crafting table or 2×2 inventory grid.
- Establish the **multi-module structure** that allows NeoForge support to be added in v3 by mirroring the Fabric module — with **zero changes** to gameplay code in `common/`.
- Establish the **content registration pattern** that allows v2 (Pivo) and beyond to add items by dropping a class file and one registration line — with **no changes** to entry points, build files, or CI.
- Establish the **CI / release pipeline** so that v1 can be tested locally and released to Modrinth on demand by tagging a commit.

### Non-goals (v1)
- NeoForge module (deferred to v3).
- Pivo or any drink content (deferred to v2).
- Mixins (mixin config scaffolded empty, no mixin classes yet).
- Custom artist-quality textures (placeholder 16×16 PNG ships in v1; can be replaced anytime without code changes).
- In-game gametest harness (manual `runClient` verification documented in README).
- Architectury or any cross-platform abstraction library.

---

## 2. Targets & versions

| Field | Value | Why |
|---|---|---|
| Minecraft | **1.21.1** | Best ecosystem support, stable; matches the most common modpack target as of 2026. |
| Java | **21** | Required by Minecraft 1.20.5+. |
| Fabric Loader | latest stable for 1.21.1 | Pulled from `gradle.properties`. |
| Fabric API | matching 1.21.1 build | Required for Fabric registry/datagen APIs. |
| Loom | latest stable | Build plugin (`fabric-loom`). |
| Yarn mappings | latest for 1.21.1 | Source mappings (`net.fabricmc:yarn`). |
| Mod ID | `czechcraft` | Lowercase, alphanumeric — Fabric and Modrinth requirement. |
| Modrinth slug | `czechcraft` (fallback `czech-craft`) | Decided on first publish based on availability. |
| Java package root | `cz.czechcraft` | Czech country code as TLD prefix, conventional. |
| License | **MIT** | In `LICENSE`, source headers, `fabric.mod.json` `license` field, and Modrinth project metadata. |

All version pins live in `gradle.properties` so a version bump touches one file.

---

## 3. Repository / module architecture

```
czechcraft/
├── settings.gradle                  ← composite build: include "common" + "fabric"
├── build.gradle                     ← root: shared plugin config, Java 21, repos
├── gradle.properties                ← all version pins (mc, fabric, loom, mod_version)
├── gradle/                          ← gradle wrapper
├── LICENSE                          ← MIT
├── README.md                        ← what + install + how to extend
├── CHANGELOG.md                     ← Keep-a-Changelog format
├── .gitignore                       ← Gradle, IntelliJ, Eclipse
├── .editorconfig                    ← consistent formatting across editors
│
├── common/                          ← LOADER-AGNOSTIC. Vanilla MC APIs only.
│   ├── build.gradle                 ← compiles against vanilla MC; no Fabric API on classpath
│   └── src/main/
│       ├── java/cz/czechcraft/
│       │   ├── CzechCraft.java         ← MOD_ID constant, LOGGER, common init(RegistryHelper)
│       │   ├── content/
│       │   │   └── food/
│       │   │       └── FoodItems.java  ← Rohlík definition (Item.Settings + FoodComponent)
│       │   ├── registry/
│       │   │   ├── ModItems.java       ← collects all items in a Map<Identifier, Item>
│       │   │   └── ModItemGroups.java  ← CzechCraft creative tab definition
│       │   └── platform/
│       │       └── RegistryHelper.java ← interface; loader modules supply implementation
│       └── resources/                  ← LOADER-AGNOSTIC vanilla-format assets
│           └── assets/czechcraft/
│               └── textures/item/rohlik.png  ← placeholder texture (16×16) — shared across loaders
│
├── fabric/                          ← FABRIC-SPECIFIC. Thin entry layer.
│   ├── build.gradle                 ← depends on :common, applies Loom, declares Fabric API
│   └── src/main/
│       ├── java/cz/czechcraft/fabric/
│       │   ├── CzechCraftFabric.java          ← implements ModInitializer
│       │   ├── FabricRegistryHelper.java      ← implements platform.RegistryHelper
│       │   ├── CzechCraftItemGroupFabric.java ← registers ItemGroup (Fabric tab API)
│       │   └── datagen/
│       │       ├── CzechCraftDataGenerator.java  ← DataGeneratorEntrypoint
│       │       ├── ModRecipeProvider.java        ← Rohlík shaped recipe
│       │       ├── ModModelProvider.java         ← item model (generated)
│       │       ├── ModEnglishLangProvider.java   ← en_us
│       │       └── ModCzechLangProvider.java     ← cs_cz
│       ├── resources/                  ← FABRIC-ONLY metadata
│       │   ├── fabric.mod.json
│       │   └── czechcraft.mixins.json   ← scaffolded empty, ready when needed
│       └── generated/                   ← datagen output, committed to repo
│           └── assets/czechcraft/lang/, data/czechcraft/recipe/, etc.
│
└── .github/
    └── workflows/
        ├── build.yml                ← every push/PR: gradle build + spotless + tests
        └── release.yml              ← on git tag v*: build → upload to Modrinth → GitHub Release
```

**Boundary rule (compiler-enforced):** `common/` may import only `net.minecraft.*` and `org.slf4j.*`. Anything `net.fabricmc.*` is a build error there because `common/build.gradle` does not put Fabric API on the compile classpath. This is what makes the `common/` discipline cheap to enforce — the compiler does it for us.

**Resource layout rationale:**
- **Hand-authored cross-loader assets (textures)** → `common/src/main/resources/`. They use vanilla Minecraft asset paths (`assets/czechcraft/...`), so both Fabric and a future NeoForge module load them identically. Fabric's `build.gradle` includes `common`'s resources in the final jar via standard Gradle subproject configuration.
- **Fabric-specific metadata** (`fabric.mod.json`, mixin config) → `fabric/src/main/resources/`. These files are loader-format-specific.
- **Datagen-generated files** (recipes, models, lang) → `fabric/src/main/generated/`. Datagen is a loader-specific API; output is committed to the repo so reviewers and modpack authors can see the JSON without running Gradle.

---

## 4. Component responsibilities

| Component | Module | Responsibility | Loader-aware? |
|---|---|---|---|
| `CzechCraft` | common | Holds `MOD_ID = "czechcraft"`, `LOGGER`, exposes `init(RegistryHelper)` that calls `ModItems.register(...)` | No |
| `FoodItems` | common | Defines `createRohlik(ResourceKey<Item>)` factory returning `new Item(new Item.Properties().setId(key).food(...))` with `nutrition=4, saturation=0.3f` | No |
| `ModItems` | common | Central registry; `register(helper)` registers each item; `getAll()` returns a stable iteration order | No |
| `ModItemGroups` | common | Defines a `CzechCraft` creative tab populated from `ModItems.getAll()` | No |
| `RegistryHelper` | common (interface) | `void registerItem(Identifier id, Item item)` — abstracts the actual registry call | No |
| `CzechCraftFabric` | fabric | `ModInitializer` entry — instantiates `FabricRegistryHelper`, calls `CzechCraft.init(helper)`, registers item group | Yes |
| `FabricRegistryHelper` | fabric | Implements `RegistryHelper` using `Registry.register(Registries.ITEM, ...)` | Yes |
| `CzechCraftItemGroupFabric` | fabric | Registers the creative tab via `Registries.ITEM_GROUP` + `FabricItemGroup.builder()` | Yes |
| `ModRecipeProvider` | fabric/datagen | Generates `data/czechcraft/recipe/rohlik.json` — shaped, pattern `["WW"]`, key `W = wheat`, result 1 Rohlík | Yes (datagen API is loader-specific) |
| `ModModelProvider` | fabric/datagen | Generates `assets/czechcraft/models/item/rohlik.json` (parent `item/generated`, layer0 = `czechcraft:item/rohlik`) | Yes |
| `ModEnglishLangProvider` | fabric/datagen | `en_us`: `item.czechcraft.rohlik = "Rohlík"`, `itemGroup.czechcraft = "CzechCraft"` | Yes |
| `ModCzechLangProvider` | fabric/datagen | `cs_cz`: `item.czechcraft.rohlik = "Rohlík"`, `itemGroup.czechcraft = "CzechCraft"` | Yes |

---

## 5. Food specification — Rohlík

| Property | Value |
|---|---|
| Identifier | `czechcraft:rohlik` |
| Display name (en_us) | Rohlík |
| Display name (cs_cz) | Rohlík |
| `FoodProperties.nutrition` | **4** (= 2 drumstick icons restored) |
| `FoodProperties.saturationModifier` | **0.3f** (yields ~2.4 saturation; between cookie and bread) |
| `alwaysEdible` | false (default — cannot eat at full hunger) |
| Max stack size | 64 (default) |
| Creative tab | CzechCraft |

Rationale: the user wants "2 hunger bars" = 2 drumstick icons = nutrition 4 (Minecraft hunger uses 1 drumstick = 2 nutrition points). The 0.3 saturation modifier sits between vanilla cookie (`nutrition=2, mod=0.1`) and bread (`nutrition=5, mod=0.6`). The original spec briefly used `nutrition=2` due to a units misreading; corrected during internal testing — see PR #12.

> **MC 26.1 note:** the `FoodProperties.snack()` method was removed from the builder (the `Consumable` data component took over fast-eat semantics). Rohlík uses default eat speed.

---

## 6. Crafting recipe — Rohlík

- **Type:** `minecraft:crafting_shaped`
- **Pattern:** `["WW"]` (single row, two wheat horizontally adjacent)
- **Key:** `W` → `minecraft:wheat`
- **Result:** 1 × `czechcraft:rohlik`
- **Group:** `czechcraft:bread` (groups Czech bread items together in recipe book; future-proof for v2+ bread variants)
- **Where it works:** crafting table AND 2×2 inventory grid (single-row patterns fit both — this is vanilla shaped-recipe behavior, no special handling needed).
- **Generated by:** `ModRecipeProvider` at build time via `gradlew runDatagen`. The resulting JSON is committed to the repo so users (and code reviewers) can see it.

---

## 7. Data flow — Rohlík from boot to belly

```
Game start
   └─ Fabric loader reads fabric.mod.json
       └─ Calls CzechCraftFabric.onInitialize()
           ├─ Builds FabricRegistryHelper
           ├─ Calls CzechCraft.init(helper)
           │   └─ ModItems.register(helper)
           │       └─ helper.registerItem(id("rohlik"), FoodItems.ROHLIK)
           │           └─ Registry.register(Registries.ITEM, ...)   ← vanilla call
           └─ CzechCraftItemGroupFabric.register()
               └─ Adds CzechCraft tab populated from ModItems.getAll()

Build time (gradlew runDatagen)
   └─ CzechCraftDataGenerator runs:
       ├─ ModRecipeProvider → data/czechcraft/recipe/rohlik.json
       ├─ ModModelProvider  → assets/czechcraft/models/item/rohlik.json
       └─ ModLangProviders  → assets/czechcraft/lang/{en_us,cs_cz}.json

Player crafts (in-game)
   2× wheat side-by-side in a row → 1× Rohlík
   (vanilla shaped-crafting machinery; no mod code involved)

Player eats Rohlík
   FoodProperties(nutrition=4, saturationModifier=0.3) →
       hunger meter +4 (= 2 drumsticks), saturation +2.4
   (vanilla food machinery; no mod code involved)
```

**Key insight:** post-registration, no CzechCraft Java code runs again. Crafting and eating are 100% vanilla. This is what makes the `common/` boundary cheap — we register data, vanilla does the work.

---

## 8. Extensibility — how new content lands later

### v2 — adding "Pivo" (Czech beer)

1. New file `common/.../content/drink/Drinks.java` defining `PIVO` as an `Item` (with drinking behavior — `UseAction.DRINK` and a custom `finishUsing` that grants a brief effect, TBD in the v2 spec).
2. **One line** added to `ModItems.register(...)`.
3. New `recipeProvider` entry for the brewing/crafting recipe.
4. Lang strings appended to both `ModEnglishLangProvider` and `ModCzechLangProvider`.
5. Texture dropped into `assets/czechcraft/textures/item/pivo.png`.

**No changes to:** `RegistryHelper`, `CzechCraftFabric`, `ModItemGroups` (auto-populates), build files, or CI.

### v3 — adding NeoForge support

1. New `neoforge/` Gradle module mirroring `fabric/`'s small structure.
2. `NeoForgeRegistryHelper` implements the same `RegistryHelper` interface using NeoForge's `DeferredRegister`.
3. `settings.gradle` gets one new `include "neoforge"` line.
4. `release.yml` gets a second job uploading the NeoForge jar to Modrinth.
5. The `common/src/main/resources/assets/czechcraft/textures/` tree is included in the NeoForge jar the same way Fabric does it — no duplication.
6. **Zero changes to `common/` source code.**

These two paths are the design's load-bearing claims. Anything that breaks either claim is a design bug.

---

## 9. Error handling

Minimal by design — nothing in v1 can fail at runtime that isn't already a Minecraft assertion (e.g. duplicate registry IDs throw on registration, which is the correct fail-fast behavior). No try/catch anywhere in v1. The `LOGGER` logs one INFO line on init success: `"CzechCraft initialized — N items registered."`.

If a future version introduces config files, network packets, or external I/O, error handling will be added at those specific boundaries — not preemptively.

---

## 10. Testing

**Revised after implementation discovery:** Plain JUnit cannot run unit tests that touch `Item`, `FoodComponent`, or any Minecraft class whose static initializer reaches the registry path — Fabric Loader's runtime bytecode rewriting is what makes `RegistryEntry$Reference.setRegistryKey` accessible across packages, and that rewriting isn't active in a vanilla JUnit run. The proposed `FoodItemsTest`/`ModItemsTest`/`CzechCraftTest` were removed. We rely on:

### 10.1 Compilation as type-level verification
The Java compiler enforces correct use of the Yarn-mapped Minecraft APIs. Misuse of `FoodComponent.Builder`, wrong `Identifier` package, etc. → build fails. CI runs `./gradlew build`, so every push exercises this layer.

### 10.2 Datagen verification (`fabric/src/test/java/`)
- CI runs `./gradlew runDatagen` before tests.
- A small JUnit test reads the generated files and asserts:
  - `data/czechcraft/recipe/rohlik.json` exists, parses, has type `minecraft:crafting_shaped`, pattern `["WW"]`, key `W = minecraft:wheat`, result `czechcraft:rohlik` count 1.
  - `assets/czechcraft/models/item/rohlik.json` exists and references `czechcraft:item/rohlik`.
  - `assets/czechcraft/lang/{en_us,cs_cz}.json` exist and contain the expected keys.

  - **Note:** if `DatagenOutputTest` itself ever needs to load common-side classes that touch Minecraft, the same Bootstrap problem will reappear. Keep this test purely file-system based (read JSON from disk, assert on its contents).

### 10.3 Manual verification (documented in README)
- `./gradlew runClient` to launch a dev Minecraft instance.
- Verify CzechCraft tab appears in creative menu.
- Verify Rohlík can be crafted from 2 wheat in both crafting table and 2×2 inventory grid.
- Verify eating Rohlík restores 2 drumsticks of hunger.
- No automated gametest harness in v1 — overkill for one item.

---

## 11. CI / release pipeline

### 11.1 `.github/workflows/build.yml` (every push and PR to `main`)
1. Checkout
2. Set up JDK 21 (Temurin)
3. Cache Gradle dependencies (`actions/cache` keyed on `**/*.gradle*` + `**/gradle-wrapper.properties`)
4. `./gradlew spotlessCheck build test`
5. Upload built jar as a workflow artifact (for manual download / smoke test)

### 11.2 `.github/workflows/release.yml` (on git tag matching `v*`)
1. Checkout
2. Set up JDK 21 (Temurin)
3. Cache Gradle
4. `./gradlew build`
5. Publish to Modrinth via the **`modrinth-minotaur`** Gradle plugin (industry standard) — token from `MODRINTH_TOKEN` repo secret. Plugin reads version, changelog, supported MC versions, and loaders from `build.gradle` / `gradle.properties`.
6. Create GitHub Release with the jar attached. Body extracted from the matching section of `CHANGELOG.md`.
7. Optional second job (commented out in v1, ready for v3): build and upload NeoForge jar.

### 11.3 Code style
- **Spotless** plugin enforces consistent Java formatting (Google Java Format).
- Runs in CI as `./gradlew spotlessCheck` (fails the build on violations).
- Locally: `./gradlew spotlessApply` to auto-fix.

### 11.4 Versioning
- **Semantic Versioning** (`MAJOR.MINOR.PATCH`).
- `mod_version` in `gradle.properties` is the single source of truth.
- Release tag format: `v1.0.0`.
- `CHANGELOG.md` follows **Keep a Changelog** conventions; each release section corresponds to a tag.

---

## 12. Modrinth project metadata (filled at first publish)

| Field | Value |
|---|---|
| Project name | CzechCraft |
| Slug | `czechcraft` (fallback `czech-craft` if taken) |
| Summary (≤256 chars) | "Czech-themed food and drinks for Minecraft. v1 adds the Rohlík — a traditional Czech bread roll, crafted from two wheat." |
| Description | English first (full feature list, crafting recipe, planned roadmap with Pivo in v2). Czech translation appended. |
| Categories | Food, Adventure |
| Environment | Required on both client and server (items are server-side authoritative; client renders model + tooltip) |
| Loaders | Fabric (NeoForge added in v3) |
| Game versions | 1.21.1 |
| License | MIT |
| Source code URL | GitHub repo |
| Issues URL | GitHub Issues |

Modrinth review SLA is 24–48 hours per their support docs; first submission is the only one that requires manual review.

---

## 13. Out of scope for v1 (explicit YAGNI)

| Item | Deferred to | Reason |
|---|---|---|
| NeoForge module | v3 | Architecture supports it; not needed for first ship. |
| Mixins | when needed | Mixin config scaffolded empty; no current use case. |
| Pivo / additional items | v2 | Single item proves the registration pattern. |
| Custom artist textures | anytime | Placeholder PNG ships in v1; can be replaced without code changes. |
| Gametest harness | when content grows | Overkill for one item. |
| Architectury or Forgix | not planned | Adds runtime dep, weakens "minimalistic" goal. The MultiLoader approach is sufficient. |
| Config file (Cloth Config etc.) | when first config knob exists | No knobs needed in v1. |
| Custom recipe types | when needed | Vanilla shaped crafting is sufficient for v1 + v2 (Pivo recipe TBD but expected to be shaped or shapeless). |

---

## 14. Glossary

- **MDK / template** — Mod Development Kit, the starter project provided by Fabric/NeoForge.
- **Common module** — Gradle module containing platform-agnostic code that compiles against vanilla Minecraft only.
- **Loader module** — Gradle module containing platform-specific code (Fabric, NeoForge, etc.) that depends on the common module.
- **Datagen** — Data Generation: a Fabric/NeoForge mechanism that produces JSON files (recipes, models, lang) at build time from Java code, instead of hand-writing the JSON.
- **Yarn** — Open-source Minecraft source mappings used by Fabric.
- **Loom** — The Gradle plugin (`fabric-loom`) that wires Fabric mod development into a standard Gradle build.
- **Drumstick / shank** — One icon in Minecraft's hunger bar, worth 2 nutrition points.
