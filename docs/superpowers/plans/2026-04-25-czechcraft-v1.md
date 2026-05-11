# CzechCraft v1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a Fabric mod for Minecraft 1.21.1 that adds the **Rohlík** food item (crafted from 2 wheat horizontally, restores 1 hunger drumstick), built on a `common`/`fabric` Gradle multi-module structure that allows NeoForge (v3) and additional content (Pivo in v2) to drop in without rewrites. Includes Modrinth release pipeline and CI from day one.

**Architecture:** MultiLoader-Template structure. `common/` module compiles against vanilla Minecraft only (loader-agnostic gameplay + registration logic). `fabric/` module is a thin Fabric-specific entry layer that supplies a `RegistryHelper` implementation and runs Fabric's datagen API. A future `neoforge/` module mirrors `fabric/` with no changes to `common/`.

**Tech Stack:** Java 21, Gradle (multi-module composite build), `fabric-loom` plugin, Fabric API, Yarn mappings, JUnit 5, Mockito, Spotless (Google Java Format), GitHub Actions, `modrinth-minotaur` Gradle plugin.

**Spec:** [`docs/superpowers/specs/2026-04-25-czechcraft-design.md`](../specs/2026-04-25-czechcraft-design.md). Read it before starting — it defines the architecture and constraints this plan implements.

---

## Notes for the executing engineer

- **Working directory:** `.`. All `gradlew` invocations are run from there.
- **`./gradlew` may need `chmod +x gradlew`** after Task 2.
- **Internet required for first `./gradlew build`** — Loom downloads Minecraft + mappings.
- **First Gradle run is slow** (10+ minutes for Minecraft download + remapping). Subsequent runs are seconds.
- **JDK 21 must be active** (`java -version` shows 21.x). On WSL/Linux: install Temurin 21 via `sdkman` or your distro's package manager.
- **Commit cadence:** every task ends with a commit. Don't batch commits. The granularity is intentional so a failing task can be reverted cleanly.
- **TDD discipline:** for tasks marked `(TDD)`, write the test, watch it fail, then implement. Don't write implementation first.
- **Fabric APIs only in `fabric/`.** If you find yourself adding a `net.fabricmc.*` import in `common/` source, stop — that's an architectural failure.

---

## File structure (final state at end of v1)

```
czechcraft/
├── .editorconfig                                  Task 1
├── .gitignore                                     Task 1
├── LICENSE                                        Task 1
├── README.md                                      Task 33 (final content)
├── CHANGELOG.md                                   Task 32 (v1.0.0 entry)
├── settings.gradle                                Task 4
├── build.gradle                                   Task 5
├── gradle.properties                              Task 3
├── gradlew, gradlew.bat                           Task 2
├── gradle/wrapper/{gradle-wrapper.jar,.properties} Task 2
│
├── common/
│   ├── build.gradle                               Task 6
│   └── src/
│       ├── main/
│       │   ├── java/cz/czechcraft/
│       │   │   ├── CzechCraft.java                Tasks 7, 12
│       │   │   ├── platform/
│       │   │   │   └── RegistryHelper.java        Task 8
│       │   │   ├── content/food/
│       │   │   │   └── FoodItems.java             Task 9
│       │   │   └── registry/
│       │   │       ├── ModItems.java              Task 10
│       │   │       └── ModItemGroups.java         Task 11
│       │   └── resources/
│       │       └── assets/czechcraft/
│       │           └── textures/item/rohlik.png   Task 19
│       └── test/java/cz/czechcraft/
│           ├── content/food/FoodItemsTest.java    Task 9
│           ├── registry/ModItemsTest.java         Task 10
│           └── CzechCraftTest.java                Task 12
│
├── fabric/
│   ├── build.gradle                               Tasks 13, 31
│   └── src/
│       ├── main/
│       │   ├── java/cz/czechcraft/fabric/
│       │   │   ├── CzechCraftFabric.java          Task 15
│       │   │   ├── FabricRegistryHelper.java      Task 16
│       │   │   ├── CzechCraftItemGroupFabric.java Task 17
│       │   │   └── datagen/
│       │   │       ├── CzechCraftDataGenerator.java Task 20
│       │   │       ├── ModRecipeProvider.java     Task 21
│       │   │       ├── ModModelProvider.java      Task 22
│       │   │       ├── ModEnglishLangProvider.java Task 23
│       │   │       └── ModCzechLangProvider.java  Task 24
│       │   ├── resources/
│       │   │   ├── fabric.mod.json                Task 14
│       │   │   └── czechcraft.mixins.json         Task 18
│       │   └── generated/                         Task 25 (datagen output)
│       └── test/java/cz/czechcraft/fabric/datagen/
│           └── DatagenOutputTest.java             Task 26
│
└── .github/workflows/
    ├── build.yml                                  Task 29
    └── release.yml                                Task 30
```

---

## Task 1: Repository scaffolding files

**Files:**
- Create: `LICENSE`
- Create: `.gitignore`
- Create: `.editorconfig`
- Create: `README.md` (skeleton; full content in Task 33)
- Create: `CHANGELOG.md` (skeleton; v1 entry in Task 32)

- [ ] **Step 1: Write `LICENSE` (MIT)**

```
MIT License

Copyright (c) 2026 Someone_Cz and CzechCraft contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

- [ ] **Step 2: Write `.gitignore`**

```
# Gradle
.gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

# Eclipse
.classpath
.project
.settings/
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

# VS Code
.vscode/

# Loom / Fabric
run/
remappedSrc/

# OS
.DS_Store
Thumbs.db

# Env / secrets
.env
*.local
```

- [ ] **Step 3: Write `.editorconfig`**

```
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true

[*.md]
trim_trailing_whitespace = false

[*.{yml,yaml,json,toml}]
indent_size = 2

[*.gradle]
indent_size = 4
```

- [ ] **Step 4: Write `README.md` skeleton**

```markdown
# CzechCraft

Czech-themed food and drinks for Minecraft (Fabric, 1.21.1).

**v1** adds the **Rohlík** — a traditional Czech bread roll, crafted from two wheat.

> Full README content lands in Task 33. This skeleton is a placeholder so the file exists from day one.
```

- [ ] **Step 5: Write `CHANGELOG.md` skeleton**

```markdown
# Changelog

All notable changes to CzechCraft are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

> v1.0.0 entry lands in Task 32.
```

- [ ] **Step 6: Commit**

```bash
git add LICENSE .gitignore .editorconfig README.md CHANGELOG.md
git commit -m "chore: add repo scaffolding (license, gitignore, editorconfig, readme, changelog)"
```

---

## Task 2: Gradle wrapper

**Files:**
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`

- [ ] **Step 1: Generate the wrapper**

Requires a system-installed Gradle (8.x or later). If not present, install via `sdk install gradle 8.10` (sdkman) or your package manager.

Run:
```bash
gradle wrapper --gradle-version 8.10 --distribution-type bin
```

This creates `gradlew`, `gradlew.bat`, and the `gradle/wrapper/` directory.

- [ ] **Step 2: Make the wrapper executable**

```bash
chmod +x gradlew
```

- [ ] **Step 3: Verify it runs**

Run: `./gradlew --version`
Expected: prints "Gradle 8.10" and JVM info.

- [ ] **Step 4: Commit**

```bash
git add gradlew gradlew.bat gradle/
git commit -m "chore: add gradle wrapper (8.10)"
```

---

## Task 3: `gradle.properties` — version pins

**Files:**
- Create: `gradle.properties`

- [ ] **Step 1: Write `gradle.properties`**

```properties
# JVM
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

# Mod metadata
mod_id=czechcraft
mod_version=1.0.0
mod_group=cz.czechcraft
mod_name=CzechCraft

# Minecraft / loader / mappings
minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.16.5

# Fabric
fabric_api_version=0.102.1+1.21.1

# Java
java_version=21
```

> Version pin notes:
> - `yarn_mappings` and `fabric_api_version` may need a small bump to the latest available — check https://fabricmc.net/develop/ for current values for MC 1.21.1.
> - `loader_version` 0.16.x is current for 1.21.1 as of writing.

- [ ] **Step 2: Commit**

```bash
git add gradle.properties
git commit -m "build: pin minecraft 1.21.1, fabric loader 0.16.5, java 21 in gradle.properties"
```

---

## Task 4: `settings.gradle` — composite build

**Files:**
- Create: `settings.gradle`

- [ ] **Step 1: Write `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        maven { url = "https://maven.fabricmc.net/" }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "czechcraft"

include "common"
include "fabric"
```

- [ ] **Step 2: Verify the project structure is recognised**

Run: `./gradlew projects`
Expected output contains:
```
Root project 'czechcraft'
+--- Project ':common'
\--- Project ':fabric'
```

(Will fail because subproject directories don't exist yet — that's fine for now; we just need the file syntactically valid. Skip verification if it errors and proceed.)

- [ ] **Step 3: Commit**

```bash
git add settings.gradle
git commit -m "build: add settings.gradle with common + fabric subprojects"
```

---

## Task 5: Root `build.gradle` — shared config + Spotless

**Files:**
- Create: `build.gradle`

- [ ] **Step 1: Write root `build.gradle`**

```groovy
plugins {
    id "java"
    id "com.diffplug.spotless" version "6.25.0" apply false
}

allprojects {
    apply plugin: "java"
    apply plugin: "com.diffplug.spotless"

    group = project.mod_group
    version = project.mod_version

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(project.java_version as int)
        }
        withSourcesJar()
    }

    tasks.withType(JavaCompile).configureEach {
        options.encoding = "UTF-8"
        options.release = project.java_version as int
    }

    repositories {
        maven { url = "https://maven.fabricmc.net/" }
        mavenCentral()
    }

    spotless {
        java {
            googleJavaFormat("1.22.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    test {
        useJUnitPlatform()
    }
}
```

- [ ] **Step 2: Apply the spotless plugin id to all subprojects**

The `apply plugin: "com.diffplug.spotless"` line above does this. No additional action.

- [ ] **Step 3: Commit**

```bash
git add build.gradle
git commit -m "build: add root build.gradle with shared java 21 + spotless (google-java-format) config"
```

---

## Task 6: `common/build.gradle` — vanilla Minecraft only, no Fabric API

**Files:**
- Create: `common/build.gradle`
- Create: `common/src/main/java/.gitkeep`
- Create: `common/src/test/java/.gitkeep`

- [ ] **Step 1: Write `common/build.gradle`**

```groovy
plugins {
    id "fabric-loom" version "1.7-SNAPSHOT"
}

loom {
    // common module: no fabric runs, just vanilla MC for compilation + tests.
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"

    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
    testImplementation "org.mockito:mockito-core:5.12.0"
    testImplementation "org.mockito:mockito-junit-jupiter:5.12.0"
}

processResources {
    inputs.property "version", project.version
}

jar {
    from(rootProject.file("LICENSE")) { rename { "${it}_${project.mod_id}" } }
}
```

- [ ] **Step 2: Create empty source dirs (so Gradle initialises them)**

```bash
mkdir -p common/src/main/java common/src/main/resources common/src/test/java
touch common/src/main/java/.gitkeep common/src/test/java/.gitkeep
```

- [ ] **Step 3: Verify the common module builds (will be slow first time — Loom downloads Minecraft)**

Run: `./gradlew :common:build`
Expected: BUILD SUCCESSFUL after Loom finishes downloading and remapping Minecraft.

- [ ] **Step 4: Commit**

```bash
git add common/build.gradle common/src/main/java/.gitkeep common/src/test/java/.gitkeep
git commit -m "build(common): add common module with vanilla minecraft + junit5 deps (no fabric api)"
```

---

## Task 7: `CzechCraft.java` skeleton — `MOD_ID` + `LOGGER`

**Files:**
- Create: `common/src/main/java/cz/czechcraft/CzechCraft.java`

- [ ] **Step 1: Write the class**

```java
package cz.czechcraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point for CzechCraft. Loader-agnostic.
 */
public final class CzechCraft {

    public static final String MOD_ID = "czechcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private CzechCraft() {
        // static-only
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/cz/czechcraft/CzechCraft.java
git commit -m "feat(common): add CzechCraft entry class with MOD_ID + LOGGER"
```

---

## Task 8: `RegistryHelper` interface

**Files:**
- Create: `common/src/main/java/cz/czechcraft/platform/RegistryHelper.java`

- [ ] **Step 1: Write the interface**

```java
package cz.czechcraft.platform;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Loader-agnostic abstraction for registering objects. Each loader module
 * (fabric, neoforge, ...) supplies an implementation backed by its native
 * registry API.
 */
public interface RegistryHelper {

    void registerItem(Identifier id, Item item);
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/cz/czechcraft/platform/RegistryHelper.java
git commit -m "feat(common): add RegistryHelper interface (loader abstraction for item registration)"
```

---

## Task 9: `FoodItems.ROHLIK` (TDD)

**Files:**
- Test: `common/src/test/java/cz/czechcraft/content/food/FoodItemsTest.java`
- Create: `common/src/main/java/cz/czechcraft/content/food/FoodItems.java`

- [ ] **Step 1: Write the failing test**

```java
package cz.czechcraft.content.food;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import org.junit.jupiter.api.Test;

class FoodItemsTest {

    @Test
    void rohlikHasExpectedFoodComponent() {
        FoodComponent food = FoodItems.ROHLIK.getComponents().get(DataComponentTypes.FOOD);

        assertNotNull(food, "Rohlík must have a FoodComponent");
        assertEquals(2, food.nutrition(), "nutrition should be 2 (= 1 drumstick)");
        assertEquals(0.3f, food.saturation(), 0.0001f, "saturation modifier should be 0.3f");
        assertFalse(food.canAlwaysEat(), "Rohlík should NOT be always-edible");
    }

    @Test
    void rohlikIsNotNull() {
        assertNotNull(FoodItems.ROHLIK);
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails**

Run: `./gradlew :common:test --tests cz.czechcraft.content.food.FoodItemsTest`
Expected: FAIL with `cannot find symbol class FoodItems` or similar.

- [ ] **Step 3: Implement `FoodItems`**

```java
package cz.czechcraft.content.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;

/**
 * Definitions of all CzechCraft food items.
 *
 * Each item is a static final field; it is registered in {@link cz.czechcraft.registry.ModItems}.
 */
public final class FoodItems {

    /**
     * Rohlík — Czech bread roll. Nutrition 2 (= 1 drumstick icon), light saturation.
     */
    public static final Item ROHLIK = new Item(
            new Item.Settings()
                    .food(new FoodComponent.Builder()
                            .nutrition(2)
                            .saturationModifier(0.3f)
                            .snack()
                            .build()));

    private FoodItems() {
        // static-only
    }
}
```

- [ ] **Step 4: Run the test to confirm it passes**

Run: `./gradlew :common:test --tests cz.czechcraft.content.food.FoodItemsTest`
Expected: PASS, both test methods green.

- [ ] **Step 5: Commit**

```bash
git add common/src/main/java/cz/czechcraft/content/food/FoodItems.java \
        common/src/test/java/cz/czechcraft/content/food/FoodItemsTest.java
git commit -m "feat(common): add Rohlík food item (nutrition=2, saturation=0.3, snack)"
```

---

## Task 10: `ModItems` central registry (TDD)

**Files:**
- Test: `common/src/test/java/cz/czechcraft/registry/ModItemsTest.java`
- Create: `common/src/main/java/cz/czechcraft/registry/ModItems.java`

- [ ] **Step 1: Write the failing test**

```java
package cz.czechcraft.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.platform.RegistryHelper;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class ModItemsTest {

    @Test
    void getAllContainsRohlik() {
        Map<Identifier, Item> all = ModItems.getAll();
        Identifier rohlikId = Identifier.of(CzechCraft.MOD_ID, "rohlik");
        assertTrue(all.containsKey(rohlikId), "ModItems.getAll() must contain rohlik");
        assertSame(FoodItems.ROHLIK, all.get(rohlikId));
    }

    @Test
    void registerInvokesHelperOncePerItem() {
        RegistryHelper helper = mock(RegistryHelper.class);

        ModItems.register(helper);

        verify(helper, times(1))
                .registerItem(eq(Identifier.of(CzechCraft.MOD_ID, "rohlik")), same(FoodItems.ROHLIK));
        verifyNoMoreInteractions(helper);
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails**

Run: `./gradlew :common:test --tests cz.czechcraft.registry.ModItemsTest`
Expected: FAIL with `cannot find symbol class ModItems`.

- [ ] **Step 3: Implement `ModItems`**

```java
package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.platform.RegistryHelper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Central catalogue of all CzechCraft items. Iteration order is insertion order
 * (LinkedHashMap) so the creative tab and tests see a stable sequence.
 *
 * Adding a new item: declare it in the appropriate {@code content/...} class
 * (e.g. {@link FoodItems}), then add one line in the static initialiser below.
 */
public final class ModItems {

    private static final Map<Identifier, Item> ITEMS = new LinkedHashMap<>();

    static {
        add("rohlik", FoodItems.ROHLIK);
    }

    private ModItems() {
        // static-only
    }

    private static void add(String path, Item item) {
        ITEMS.put(Identifier.of(CzechCraft.MOD_ID, path), item);
    }

    /** Returns an unmodifiable view of every CzechCraft item, in insertion order. */
    public static Map<Identifier, Item> getAll() {
        return Collections.unmodifiableMap(ITEMS);
    }

    /** Registers every item with the loader-supplied helper. */
    public static void register(RegistryHelper helper) {
        ITEMS.forEach(helper::registerItem);
    }
}
```

- [ ] **Step 4: Run the test to confirm it passes**

Run: `./gradlew :common:test --tests cz.czechcraft.registry.ModItemsTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/src/main/java/cz/czechcraft/registry/ModItems.java \
        common/src/test/java/cz/czechcraft/registry/ModItemsTest.java
git commit -m "feat(common): add ModItems central registry (drives loader-side registration)"
```

---

## Task 11: `ModItemGroups` — creative tab definition

**Files:**
- Create: `common/src/main/java/cz/czechcraft/registry/ModItemGroups.java`

- [ ] **Step 1: Write the class**

```java
package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Loader-agnostic creative-tab metadata. The actual ItemGroup is constructed
 * and registered in the loader module (e.g. {@code CzechCraftItemGroupFabric}).
 */
public final class ModItemGroups {

    public static final Identifier MAIN_GROUP_ID = Identifier.of(CzechCraft.MOD_ID, CzechCraft.MOD_ID);

    /** Translation key for the tab's display name. Lang files supply en_us + cs_cz. */
    public static final String MAIN_GROUP_TRANSLATION_KEY = "itemGroup." + CzechCraft.MOD_ID;

    /** Default icon stack — first item in {@link ModItems}. */
    public static ItemStack mainGroupIcon() {
        return new ItemStack(FoodItems.ROHLIK);
    }

    private ModItemGroups() {
        // static-only
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/cz/czechcraft/registry/ModItemGroups.java
git commit -m "feat(common): add ModItemGroups (creative-tab metadata, loader-agnostic)"
```

---

## Task 12: Wire `CzechCraft.init(RegistryHelper)` (TDD)

**Files:**
- Test: `common/src/test/java/cz/czechcraft/CzechCraftTest.java`
- Modify: `common/src/main/java/cz/czechcraft/CzechCraft.java`

- [ ] **Step 1: Write the failing test**

```java
package cz.czechcraft;

import static org.mockito.Mockito.*;

import cz.czechcraft.platform.RegistryHelper;
import org.junit.jupiter.api.Test;

class CzechCraftTest {

    @Test
    void initDelegatesItemRegistrationToHelper() {
        RegistryHelper helper = mock(RegistryHelper.class);

        CzechCraft.init(helper);

        verify(helper, atLeastOnce()).registerItem(any(), any());
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails**

Run: `./gradlew :common:test --tests cz.czechcraft.CzechCraftTest`
Expected: FAIL with `cannot find symbol method init`.

- [ ] **Step 3: Add `init` to `CzechCraft`**

Modify `common/src/main/java/cz/czechcraft/CzechCraft.java` — replace the existing class body with:

```java
package cz.czechcraft;

import cz.czechcraft.platform.RegistryHelper;
import cz.czechcraft.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point for CzechCraft. Loader-agnostic.
 *
 * Each loader module (fabric, neoforge, ...) calls {@link #init(RegistryHelper)}
 * exactly once at mod startup, supplying its own RegistryHelper implementation.
 */
public final class CzechCraft {

    public static final String MOD_ID = "czechcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private CzechCraft() {
        // static-only
    }

    /** Registers all CzechCraft content. Called once per game start by each loader entry point. */
    public static void init(RegistryHelper helper) {
        ModItems.register(helper);
        LOGGER.info("CzechCraft initialised — {} items registered.", ModItems.getAll().size());
    }
}
```

- [ ] **Step 4: Run the test to confirm it passes**

Run: `./gradlew :common:test --tests cz.czechcraft.CzechCraftTest`
Expected: PASS.

- [ ] **Step 5: Run the full common test suite**

Run: `./gradlew :common:test`
Expected: all tests pass (`FoodItemsTest`, `ModItemsTest`, `CzechCraftTest`).

- [ ] **Step 6: Commit**

```bash
git add common/src/main/java/cz/czechcraft/CzechCraft.java \
        common/src/test/java/cz/czechcraft/CzechCraftTest.java
git commit -m "feat(common): wire CzechCraft.init() to drive ModItems registration via helper"
```

---

## Task 13: `fabric/build.gradle`

**Files:**
- Create: `fabric/build.gradle`
- Create: `fabric/src/main/java/.gitkeep`

- [ ] **Step 1: Write `fabric/build.gradle`**

```groovy
plugins {
    id "fabric-loom" version "1.7-SNAPSHOT"
}

archivesBaseName = project.mod_id

loom {
    runs {
        datagen {
            inherit server
            name "Data Generation"
            vmArg "-Dfabric-api.datagen"
            vmArg "-Dfabric-api.datagen.output-dir=${file("src/main/generated")}"
            vmArg "-Dfabric-api.datagen.modid=${project.mod_id}"
            runDir "build/datagen"
        }
    }
}

sourceSets.main.resources.srcDir "src/main/generated"

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"

    implementation project(":common")

    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
}

processResources {
    inputs.property "version", project.version
    inputs.property "minecraft_version", project.minecraft_version
    inputs.property "loader_version", project.loader_version

    filesMatching("fabric.mod.json") {
        expand "version": project.version,
                "minecraft_version": project.minecraft_version,
                "loader_version": project.loader_version,
                "mod_id": project.mod_id,
                "mod_name": project.mod_name
    }

    // include the common module's resources (textures, etc.)
    from(project(":common").sourceSets.main.resources)
}

jar {
    from(rootProject.file("LICENSE")) { rename { "${it}_${project.mod_id}" } }
}
```

- [ ] **Step 2: Verify the configuration parses (subproject build will fail until source files exist; that's OK)**

Run: `./gradlew :fabric:tasks`
Expected: BUILD SUCCESSFUL, prints task list including `runClient`, `runServer`, `runDatagen`.

- [ ] **Step 3: Commit**

```bash
git add fabric/build.gradle fabric/src/main/java/.gitkeep
git commit -m "build(fabric): add fabric module with loom + fabric-api + datagen run config"
```

---

## Task 14: `fabric.mod.json`

**Files:**
- Create: `fabric/src/main/resources/fabric.mod.json`

- [ ] **Step 1: Write the manifest**

```json
{
  "schemaVersion": 1,
  "id": "${mod_id}",
  "version": "${version}",
  "name": "${mod_name}",
  "description": "Czech-themed food and drinks for Minecraft. v1 adds the Rohlík — a traditional Czech bread roll, crafted from two wheat.",
  "authors": ["Someone_Cz"],
  "contact": {
    "homepage": "https://modrinth.com/mod/${mod_id}",
    "sources": "https://github.com/vortom/czechcraft",
    "issues": "https://github.com/vortom/czechcraft/issues"
  },
  "license": "MIT",
  "icon": "assets/${mod_id}/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["cz.czechcraft.fabric.CzechCraftFabric"],
    "fabric-datagen": ["cz.czechcraft.fabric.datagen.CzechCraftDataGenerator"]
  },
  "mixins": [
    "${mod_id}.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=${loader_version}",
    "fabric-api": "*",
    "minecraft": "${minecraft_version}",
    "java": ">=21"
  },
  "suggests": {}
}
```

> Note: `assets/${mod_id}/icon.png` — Modrinth shows this icon. We'll use the same placeholder PNG generated in Task 19, copied into `common/src/main/resources/assets/czechcraft/icon.png` if not already there. (Task 19 covers it.)

- [ ] **Step 2: Verify resource processing substitutes the variables**

Run: `./gradlew :fabric:processResources`
Expected: BUILD SUCCESSFUL. Inspect `fabric/build/resources/main/fabric.mod.json` and confirm `${version}` was replaced with `1.0.0`.

- [ ] **Step 3: Commit**

```bash
git add fabric/src/main/resources/fabric.mod.json
git commit -m "feat(fabric): add fabric.mod.json (entrypoints, deps, license)"
```

---

## Task 15: `CzechCraftFabric` ModInitializer

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/CzechCraftFabric.java`

- [ ] **Step 1: Write the entry point**

```java
package cz.czechcraft.fabric;

import cz.czechcraft.CzechCraft;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric loader entry point. Bridges Fabric's lifecycle into the loader-agnostic
 * {@link CzechCraft#init(cz.czechcraft.platform.RegistryHelper)} method, then
 * registers the Fabric-specific creative tab.
 */
public final class CzechCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CzechCraft.init(new FabricRegistryHelper());
        CzechCraftItemGroupFabric.register();
    }
}
```

- [ ] **Step 2: It will not compile yet — `FabricRegistryHelper` and `CzechCraftItemGroupFabric` are added in Tasks 16 and 17. Skip compile verification until Task 17.**

- [ ] **Step 3: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/CzechCraftFabric.java
git commit -m "feat(fabric): add CzechCraftFabric ModInitializer entry point"
```

---

## Task 16: `FabricRegistryHelper`

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/FabricRegistryHelper.java`

- [ ] **Step 1: Write the implementation**

```java
package cz.czechcraft.fabric;

import cz.czechcraft.platform.RegistryHelper;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Fabric implementation of {@link RegistryHelper} — delegates straight to the
 * vanilla {@link Registry#register(Registry, Identifier, Object)} call, which
 * is what Fabric API expects mods to use directly.
 */
public final class FabricRegistryHelper implements RegistryHelper {

    @Override
    public void registerItem(Identifier id, Item item) {
        Registry.register(Registries.ITEM, id, item);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/FabricRegistryHelper.java
git commit -m "feat(fabric): add FabricRegistryHelper (Registry.register backend)"
```

---

## Task 17: `CzechCraftItemGroupFabric`

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/CzechCraftItemGroupFabric.java`

- [ ] **Step 1: Write the class**

```java
package cz.czechcraft.fabric;

import cz.czechcraft.registry.ModItemGroups;
import cz.czechcraft.registry.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/**
 * Registers the CzechCraft creative tab and auto-populates it from
 * {@link ModItems#getAll()} so adding a new item never requires touching this file.
 */
public final class CzechCraftItemGroupFabric {

    private CzechCraftItemGroupFabric() {
        // static-only
    }

    public static void register() {
        ItemGroup group = FabricItemGroup.builder()
                .icon(ModItemGroups::mainGroupIcon)
                .displayName(Text.translatable(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY))
                .entries((displayContext, entries) ->
                        ModItems.getAll().values().forEach(entries::add))
                .build();

        Registry.register(Registries.ITEM_GROUP, ModItemGroups.MAIN_GROUP_ID, group);
    }
}
```

- [ ] **Step 2: Verify the fabric module compiles**

Run: `./gradlew :fabric:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/CzechCraftItemGroupFabric.java
git commit -m "feat(fabric): register CzechCraft creative tab (auto-populates from ModItems)"
```

---

## Task 18: Empty mixins config

**Files:**
- Create: `fabric/src/main/resources/czechcraft.mixins.json`

- [ ] **Step 1: Write the empty mixin config (referenced by `fabric.mod.json`)**

```json
{
  "required": true,
  "package": "cz.czechcraft.fabric.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 2: Verify resources still process**

Run: `./gradlew :fabric:processResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add fabric/src/main/resources/czechcraft.mixins.json
git commit -m "chore(fabric): scaffold empty mixin config (ready for future use)"
```

---

## Task 19: Placeholder texture + mod icon

**Files:**
- Create: `common/src/main/resources/assets/czechcraft/textures/item/rohlik.png`
- Create: `common/src/main/resources/assets/czechcraft/icon.png`

- [ ] **Step 1: Generate a 16×16 placeholder PNG using ImageMagick**

The texture is a tan/beige rounded rectangle — a stand-in until a real artist supplies one. Replace anytime without touching code.

```bash
mkdir -p common/src/main/resources/assets/czechcraft/textures/item
mkdir -p common/src/main/resources/assets/czechcraft

# 16x16 placeholder for the in-game item
convert -size 16x16 xc:transparent \
  -fill "#d4a574" -draw "roundrectangle 1,4 14,11 3,3" \
  -fill "#a67839" -draw "line 4,6 12,6" \
  -fill "#a67839" -draw "line 4,9 12,9" \
  common/src/main/resources/assets/czechcraft/textures/item/rohlik.png

# 128x128 icon for Modrinth + fabric.mod.json
convert -size 128x128 xc:"#3e2a1a" \
  -fill "#d4a574" -draw "roundrectangle 16,40 112,88 16,16" \
  -fill "#a67839" -stroke "#a67839" -strokewidth 3 \
  -draw "line 32,52 96,52" \
  -draw "line 32,76 96,76" \
  common/src/main/resources/assets/czechcraft/icon.png
```

If ImageMagick is not installed: `sudo apt-get install imagemagick` (Debian/Ubuntu/WSL) or use any tiny opaque PNG. The point is the file exists; visual quality is replaceable.

- [ ] **Step 2: Verify the PNG files exist and are valid**

Run:
```bash
file common/src/main/resources/assets/czechcraft/textures/item/rohlik.png
file common/src/main/resources/assets/czechcraft/icon.png
```
Expected: both report `PNG image data, ... ` with sensible dimensions.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/resources/assets/czechcraft/
git commit -m "feat(common): add placeholder Rohlík texture (16x16) + mod icon (128x128)"
```

---

## Task 20: `CzechCraftDataGenerator` entry point

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/datagen/CzechCraftDataGenerator.java`

- [ ] **Step 1: Write the entry point**

```java
package cz.czechcraft.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Datagen entry point — Fabric calls this from the {@code runDatagen} task.
 *
 * Each provider here generates a slice of the resource/data tree (recipes,
 * models, lang). Adding new content == adding lines inside an existing
 * provider, not adding new providers.
 */
public final class CzechCraftDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModEnglishLangProvider::new);
        pack.addProvider(ModCzechLangProvider::new);
    }
}
```

- [ ] **Step 2: Will not compile until Tasks 21–24 are done. Skip verification.**

- [ ] **Step 3: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/datagen/CzechCraftDataGenerator.java
git commit -m "feat(fabric): add datagen entry point (wires recipe/model/lang providers)"
```

---

## Task 21: `ModRecipeProvider` — Rohlík shaped recipe

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/datagen/ModRecipeProvider.java`

- [ ] **Step 1: Write the provider**

```java
package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

public final class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, FoodItems.ROHLIK, 1)
                .pattern("WW")
                .input('W', Items.WHEAT)
                .group("czechcraft:bread")
                .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
                .offerTo(exporter);
    }
}
```

- [ ] **Step 2: Commit (compile verification deferred to Task 25)**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/datagen/ModRecipeProvider.java
git commit -m "feat(fabric/datagen): add Rohlík shaped recipe (2 wheat horizontal -> 1 rohlik)"
```

---

## Task 22: `ModModelProvider` — item model

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/datagen/ModModelProvider.java`

- [ ] **Step 1: Write the provider**

```java
package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public final class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        // no blocks in v1
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(FoodItems.ROHLIK, Models.GENERATED);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/datagen/ModModelProvider.java
git commit -m "feat(fabric/datagen): add Rohlík item model (parent item/generated)"
```

---

## Task 23: `ModEnglishLangProvider`

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/datagen/ModEnglishLangProvider.java`

- [ ] **Step 1: Write the provider**

```java
package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.registry.ModItemGroups;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public final class ModEnglishLangProvider extends FabricLanguageProvider {

    public ModEnglishLangProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        builder.add(FoodItems.ROHLIK, "Rohlík");
        builder.add(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY, "CzechCraft");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/datagen/ModEnglishLangProvider.java
git commit -m "feat(fabric/datagen): add en_us translations (Rohlík + CzechCraft tab)"
```

---

## Task 24: `ModCzechLangProvider`

**Files:**
- Create: `fabric/src/main/java/cz/czechcraft/fabric/datagen/ModCzechLangProvider.java`

- [ ] **Step 1: Write the provider**

```java
package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.registry.ModItemGroups;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public final class ModCzechLangProvider extends FabricLanguageProvider {

    public ModCzechLangProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, "cs_cz", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        builder.add(FoodItems.ROHLIK, "Rohlík");
        builder.add(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY, "CzechCraft");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add fabric/src/main/java/cz/czechcraft/fabric/datagen/ModCzechLangProvider.java
git commit -m "feat(fabric/datagen): add cs_cz translations"
```

---

## Task 25: Run datagen and commit generated files

**Files:**
- Generated: `fabric/src/main/generated/data/czechcraft/recipe/rohlik.json`
- Generated: `fabric/src/main/generated/assets/czechcraft/models/item/rohlik.json`
- Generated: `fabric/src/main/generated/assets/czechcraft/lang/en_us.json`
- Generated: `fabric/src/main/generated/assets/czechcraft/lang/cs_cz.json`
- Generated (data driven by recipe): `fabric/src/main/generated/data/czechcraft/advancement/recipes/food/rohlik.json`

- [ ] **Step 1: Run the datagen task**

Run: `./gradlew :fabric:runDatagen`
Expected: BUILD SUCCESSFUL. Files appear under `fabric/src/main/generated/`.

- [ ] **Step 2: Inspect the recipe JSON**

Run: `cat fabric/src/main/generated/data/czechcraft/recipe/rohlik.json`
Expected: contains `"type": "minecraft:crafting_shaped"`, `"pattern": ["WW"]`, key `W = minecraft:wheat`, result `czechcraft:rohlik` count 1.

- [ ] **Step 3: Inspect the lang files**

Run: `cat fabric/src/main/generated/assets/czechcraft/lang/en_us.json`
Expected:
```json
{
  "item.czechcraft.rohlik": "Rohlík",
  "itemGroup.czechcraft": "CzechCraft"
}
```

(Same content for `cs_cz.json`.)

- [ ] **Step 4: Commit the generated files**

```bash
git add fabric/src/main/generated/
git commit -m "chore(fabric): commit datagen output (recipe, model, en_us+cs_cz lang)"
```

---

## Task 26: Datagen output verification test

**Files:**
- Test: `fabric/src/test/java/cz/czechcraft/fabric/datagen/DatagenOutputTest.java`

- [ ] **Step 1: Write the test**

```java
package cz.czechcraft.fabric.datagen;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DatagenOutputTest {

    private static final Path GENERATED = Path.of("src/main/generated");

    @Test
    void recipeJsonExistsAndMatchesSpec() throws IOException {
        Path recipe = GENERATED.resolve("data/czechcraft/recipe/rohlik.json");
        assertTrue(Files.exists(recipe), "recipe json must exist — run :fabric:runDatagen");

        JsonObject root = JsonParser.parseString(Files.readString(recipe)).getAsJsonObject();
        assertEquals("minecraft:crafting_shaped", root.get("type").getAsString());

        var pattern = root.getAsJsonArray("pattern");
        assertEquals(1, pattern.size());
        assertEquals("WW", pattern.get(0).getAsString());

        var key = root.getAsJsonObject("key");
        assertEquals("minecraft:wheat", key.getAsJsonObject("W").get("item").getAsString());

        var result = root.getAsJsonObject("result");
        assertEquals("czechcraft:rohlik", result.get("id").getAsString());
        assertEquals(1, result.get("count").getAsInt());
    }

    @Test
    void itemModelExists() {
        assertTrue(Files.exists(GENERATED.resolve("assets/czechcraft/models/item/rohlik.json")));
    }

    @Test
    void englishLangFileContainsRohlikKey() throws IOException {
        Path lang = GENERATED.resolve("assets/czechcraft/lang/en_us.json");
        assertTrue(Files.exists(lang));
        JsonObject root = JsonParser.parseString(Files.readString(lang)).getAsJsonObject();
        assertEquals("Rohlík", root.get("item.czechcraft.rohlik").getAsString());
        assertEquals("CzechCraft", root.get("itemGroup.czechcraft").getAsString());
    }

    @Test
    void czechLangFileContainsRohlikKey() throws IOException {
        Path lang = GENERATED.resolve("assets/czechcraft/lang/cs_cz.json");
        assertTrue(Files.exists(lang));
        JsonObject root = JsonParser.parseString(Files.readString(lang)).getAsJsonObject();
        assertEquals("Rohlík", root.get("item.czechcraft.rohlik").getAsString());
    }
}
```

- [ ] **Step 2: Run the test**

Run: `./gradlew :fabric:test --tests cz.czechcraft.fabric.datagen.DatagenOutputTest`
Expected: PASS.

If it fails because `gson` isn't on the test classpath, gson is already pulled in transitively by Loom/Minecraft. If it's missing, add `testImplementation "com.google.code.gson:gson:2.10.1"` to `fabric/build.gradle` and re-run.

- [ ] **Step 3: Commit**

```bash
git add fabric/src/test/java/cz/czechcraft/fabric/datagen/DatagenOutputTest.java
git commit -m "test(fabric): verify datagen output matches spec (recipe, model, lang)"
```

---

## Task 27: Apply Spotless formatting to all sources

**Files:**
- Modify: any Java source whose formatting drifts from Google Java Format

- [ ] **Step 1: Run spotlessApply**

Run: `./gradlew spotlessApply`
Expected: BUILD SUCCESSFUL. May modify files to match formatting.

- [ ] **Step 2: Verify spotlessCheck passes cleanly**

Run: `./gradlew spotlessCheck`
Expected: BUILD SUCCESSFUL with no violations.

- [ ] **Step 3: Commit any formatting fixups (skip if no changes)**

```bash
git status
# if there are modified files:
git add -u
git commit -m "style: apply google-java-format via spotless"
```

If `git status` shows no changes, skip the commit.

---

## Task 28: Manual smoke test via `runClient`

**Files:**
- None (manual verification)

- [ ] **Step 1: Launch the dev client**

Run: `./gradlew :fabric:runClient`
Expected: Minecraft launches into the main menu after ~30s. The CzechCraft mod is loaded automatically.

- [ ] **Step 2: Create a test world**

In the Minecraft window: Singleplayer → Create New World → Game Mode: **Creative** → Create New World.

- [ ] **Step 3: Verify CzechCraft creative tab**

Open the inventory (`E`). Look for a tab labelled "CzechCraft" (icon: Rohlík). Confirm Rohlík appears in the tab.

- [ ] **Step 4: Verify crafting**

Switch to Survival via the F3+N debug shortcut OR open a crafting table. Place 2 wheat horizontally in a row in the 2×2 inventory grid OR the 3×3 crafting table grid. Confirm 1 Rohlík appears as the output.

- [ ] **Step 5: Verify food behaviour**

Take damage so hunger drops below max. Hold and right-click Rohlík. Confirm:
- Eating animation plays.
- After eating, hunger meter increases by 1 drumstick (2 nutrition points).

- [ ] **Step 6: Quit Minecraft**

- [ ] **Step 7: No commit needed (manual verification)**

---

## Task 29: GitHub Actions — `build.yml`

**Files:**
- Create: `.github/workflows/build.yml`

- [ ] **Step 1: Write the workflow**

```yaml
name: Build

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Spotless check
        run: ./gradlew spotlessCheck

      - name: Build
        run: ./gradlew build

      - name: Run datagen + verify output
        run: ./gradlew :fabric:runDatagen :fabric:test

      - name: Upload mod jar
        uses: actions/upload-artifact@v4
        with:
          name: czechcraft-jar
          path: fabric/build/libs/*.jar
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/build.yml
git commit -m "ci: add build workflow (spotless + build + datagen verify on push/PR)"
```

---

## Task 30: GitHub Actions — `release.yml`

**Files:**
- Create: `.github/workflows/release.yml`

- [ ] **Step 1: Write the workflow**

```yaml
name: Release

on:
  push:
    tags: ["v*"]

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build
        run: ./gradlew build

      - name: Publish to Modrinth
        env:
          MODRINTH_TOKEN: ${{ secrets.MODRINTH_TOKEN }}
        run: ./gradlew :fabric:modrinth

      - name: Extract changelog section for this tag
        id: changelog
        run: |
          TAG=${GITHUB_REF#refs/tags/v}
          # Pull the section between "## [TAG]" and the next "## [" header.
          awk -v tag="$TAG" '
            $0 ~ "^## \\[" tag "\\]" { found=1; next }
            found && /^## \[/        { exit }
            found                    { print }
          ' CHANGELOG.md > release-body.md
          echo "body_path=release-body.md" >> "$GITHUB_OUTPUT"

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          body_path: ${{ steps.changelog.outputs.body_path }}
          files: fabric/build/libs/*.jar
```

> **Setup note** — before the first release, add a `MODRINTH_TOKEN` repository secret. Generate the token at https://modrinth.com/settings/pats with `Create version` scope.

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/release.yml
git commit -m "ci: add release workflow (tag -> build -> modrinth + github release)"
```

---

## Task 31: Wire `modrinth-minotaur` into `fabric/build.gradle`

**Files:**
- Modify: `fabric/build.gradle`

- [ ] **Step 1: Add the plugin and configuration**

Modify `fabric/build.gradle`. At the top, change the `plugins` block to:

```groovy
plugins {
    id "fabric-loom" version "1.7-SNAPSHOT"
    id "com.modrinth.minotaur" version "2.8.7"
}
```

At the bottom of the file, append:

```groovy
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = project.mod_id
    versionNumber = project.mod_version
    versionType = project.mod_version.endsWith("-SNAPSHOT") ? "alpha" : "release"
    uploadFile = remapJar
    gameVersions = [project.minecraft_version]
    loaders = ["fabric"]
    dependencies {
        required.project "fabric-api"
    }
    syncBodyFrom = rootProject.file("README.md").text
    changelog = rootProject.file("CHANGELOG.md").text
}

tasks.modrinth.dependsOn tasks.remapJar
```

- [ ] **Step 2: Verify the configuration parses**

Run: `./gradlew :fabric:tasks --group=upload`
Expected: BUILD SUCCESSFUL, lists the `modrinth` task.

- [ ] **Step 3: Commit**

```bash
git add fabric/build.gradle
git commit -m "build(fabric): wire modrinth-minotaur for automated release publishing"
```

---

## Task 32: `CHANGELOG.md` v1.0.0 entry

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Replace the file with**

```markdown
# Changelog

All notable changes to CzechCraft are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-04-25

### Added
- **Rohlík** — Czech bread roll. Crafted from 2 wheat placed horizontally adjacent on a crafting table or 2×2 inventory grid. Restores 1 hunger drumstick (nutrition 2, saturation modifier 0.3).
- **CzechCraft** creative tab containing all CzechCraft items.
- English (`en_us`) and Czech (`cs_cz`) translations.
- Fabric loader support for Minecraft 1.21.1 (Java 21).
- MultiLoader-Template architecture: `common/` module compiles against vanilla Minecraft only; `fabric/` is a thin entry layer. Future loader modules (NeoForge planned for v3) drop in without touching `common/` source.
- GitHub Actions: `build.yml` runs Spotless + tests + datagen verification on every push/PR; `release.yml` publishes to Modrinth and creates a GitHub Release on `v*` tags.

[Unreleased]: https://github.com/vortom/czechcraft/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/vortom/czechcraft/releases/tag/v1.0.0
```

- [ ] **Step 2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs: add v1.0.0 changelog entry"
```

---

## Task 33: Final `README.md`

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Replace the file with**

```markdown
# CzechCraft

> Czech-themed food and drinks for Minecraft.

[![Build](https://github.com/vortom/czechcraft/actions/workflows/build.yml/badge.svg)](https://github.com/vortom/czechcraft/actions/workflows/build.yml)
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
3. Download the latest `czechcraft-X.Y.Z.jar` from [Modrinth](https://modrinth.com/mod/czechcraft) or [GitHub Releases](https://github.com/vortom/czechcraft/releases).
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

[MIT](LICENSE) © 2026 Someone_Cz and CzechCraft contributors.
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: write full README (install, dev setup, architecture, extension guide)"
```

---

## Task 34: Final verification — full clean build

- [ ] **Step 1: Clean and rebuild from scratch**

Run: `./gradlew clean spotlessCheck build :fabric:runDatagen :fabric:test`
Expected: BUILD SUCCESSFUL, all tests pass, datagen produces the expected files.

- [ ] **Step 2: Inspect the final jar**

Run: `ls -lh fabric/build/libs/`
Expected: `czechcraft-1.0.0.jar` exists, ~30–50 KB.

- [ ] **Step 3: Verify the jar contents include common's resources**

Run: `unzip -l fabric/build/libs/czechcraft-1.0.0.jar | grep -E '(rohlik|fabric.mod.json|lang)'`
Expected: lists `assets/czechcraft/textures/item/rohlik.png`, `fabric.mod.json`, `assets/czechcraft/lang/en_us.json`, `assets/czechcraft/lang/cs_cz.json`, `data/czechcraft/recipe/rohlik.json`.

- [ ] **Step 4: No commit (this is verification only)**

---

## Done

At this point:
- The mod builds, all tests pass, datagen output is committed, format check is clean.
- A manual `runClient` confirms Rohlík can be crafted and eaten in-game.
- CI runs on every push.
- Tagging `v1.0.0` and pushing the tag will publish to Modrinth (after `MODRINTH_TOKEN` secret is set).
- Adding new content (Pivo or any other item) follows the documented extension flow with no architectural changes.

The architecture's two load-bearing claims (v2 adds items without touching the entry point; v3 adds NeoForge without touching `common/`) are now testable on the next iteration.
