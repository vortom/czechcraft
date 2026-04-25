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
