package cz.czechcraft.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.junit.jupiter.api.Test;

/**
 * Keeps the Minecraft compatibility we <em>declare</em> aligned with the compatibility we have
 * actually <em>tested</em>.
 *
 * <p>The range lives in {@code gradle.properties} as {@code minecraft_supported_range}, separate
 * from {@code minecraft_version} (the compile target). Conflating the two is what produced the
 * original "requires 26.1.2 but 26.2 is present" report.
 *
 * <p>Evaluated with Fabric Loader's own {@link VersionPredicate}, so the assertions match what the
 * loader will do at startup rather than approximating it with string comparison.
 */
class SupportedVersionsTest {

  /**
   * Versions proven to break. 26.3 renamed the {@code minecraft:recipe_unlocked} trigger field from
   * {@code "recipe"} to {@code "recipes"}, so our generated advancement fails to parse and the game
   * refuses to load its datapacks — it does not merely disable the mod, it stops the server
   * booting. Supporting 26.3 requires regenerating datagen against it, not widening this range.
   */
  private static final List<String> MUST_BE_REJECTED = List.of("26.3", "26.3-pre-2", "26.4");

  /** The declared range must be exactly what gradle.properties configured. */
  @Test
  void jarDeclaresTheConfiguredRange() throws IOException {
    assertEquals(
        requiredProperty("czechcraft.supportedRange"),
        declaredMinecraftRange(),
        "the range baked into the jar does not match minecraft_supported_range — processResources"
            + " likely served a stale cached fabric.mod.json (every expand key must also be"
            + " declared via inputs.property)");
  }

  /** Everything we publish to Modrinth must actually be accepted by the range we ship. */
  @Test
  void everyPublishedGameVersionSatisfiesTheRange() throws IOException, VersionParsingException {
    VersionPredicate predicate = VersionPredicate.parse(declaredMinecraftRange());
    for (String version : publishedGameVersions()) {
      assertTrue(
          predicate.test(Version.parse(version)),
          "Modrinth lists "
              + version
              + " but the declared range "
              + declaredMinecraftRange()
              + " rejects it — players would download a jar the loader refuses to start");
    }
  }

  /** Versions we know are broken must stay excluded. */
  @Test
  void rangeRejectsVersionsKnownToBreak() throws IOException, VersionParsingException {
    VersionPredicate predicate = VersionPredicate.parse(declaredMinecraftRange());
    for (String version : MUST_BE_REJECTED) {
      assertFalse(
          predicate.test(Version.parse(version)),
          "declared range "
              + declaredMinecraftRange()
              + " accepts "
              + version
              + ", which is known to break datapack loading. Re-run the version smoke test before"
              + " widening the range.");
    }
  }

  private static String declaredMinecraftRange() throws IOException {
    Path jar = Path.of(requiredProperty("czechcraft.modJar"));
    assertTrue(
        Files.isRegularFile(jar), "mod jar not found at " + jar + " — run :fabric:jar first");
    try (ZipFile zip = new ZipFile(jar.toFile())) {
      var entry = zip.getEntry("fabric.mod.json");
      assertTrue(entry != null, "fabric.mod.json missing from the mod jar");
      try (var in = zip.getInputStream(entry)) {
        JsonObject root =
            JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
                .getAsJsonObject();
        return root.getAsJsonObject("depends").get("minecraft").getAsString();
      }
    }
  }

  private static List<String> publishedGameVersions() {
    return Arrays.stream(requiredProperty("czechcraft.supportedVersions").split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private static String requiredProperty(String key) {
    String value = System.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "system property " + key + " is not set — run through Gradle (:fabric:test)");
    }
    return value;
  }
}
