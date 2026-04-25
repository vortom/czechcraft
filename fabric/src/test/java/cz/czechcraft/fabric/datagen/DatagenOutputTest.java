package cz.czechcraft.fabric.datagen;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.registry.ModItemGroups;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DatagenOutputTest {

  private static final Path GENERATED = Path.of("src/main/generated");
  private static final String ROHLIK_TRANSLATION_KEY =
      "item." + CzechCraft.MOD_ID + "." + FoodItems.ROHLIK_PATH;
  private static final String ROHLIK_QUALIFIED_ID = CzechCraft.MOD_ID + ":" + FoodItems.ROHLIK_PATH;

  @Test
  void recipeJsonExistsAndMatchesSpec() throws IOException {
    Path recipe =
        GENERATED.resolve(
            "data/" + CzechCraft.MOD_ID + "/recipe/" + FoodItems.ROHLIK_PATH + ".json");
    assertTrue(Files.exists(recipe), "recipe json must exist — run :fabric:runDatagen");

    JsonObject root = JsonParser.parseString(Files.readString(recipe)).getAsJsonObject();
    assertEquals("minecraft:crafting_shaped", root.get("type").getAsString());

    var pattern = root.getAsJsonArray("pattern");
    assertEquals(1, pattern.size());
    assertEquals("WW", pattern.get(0).getAsString());

    var key = root.getAsJsonObject("key");
    assertEquals("minecraft:wheat", key.getAsJsonObject("W").get("item").getAsString());

    var result = root.getAsJsonObject("result");
    assertEquals(ROHLIK_QUALIFIED_ID, result.get("id").getAsString());
    assertEquals(1, result.get("count").getAsInt());
  }

  @Test
  void itemModelExists() {
    assertTrue(
        Files.exists(
            GENERATED.resolve(
                "assets/"
                    + CzechCraft.MOD_ID
                    + "/models/item/"
                    + FoodItems.ROHLIK_PATH
                    + ".json")));
  }

  @Test
  void englishLangFileContainsRohlikKey() throws IOException {
    Path lang = GENERATED.resolve("assets/" + CzechCraft.MOD_ID + "/lang/en_us.json");
    assertTrue(Files.exists(lang));
    JsonObject root = JsonParser.parseString(Files.readString(lang)).getAsJsonObject();
    assertEquals("Rohlík", root.get(ROHLIK_TRANSLATION_KEY).getAsString());
    assertEquals("CzechCraft", root.get(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY).getAsString());
  }

  @Test
  void czechLangFileContainsRohlikKey() throws IOException {
    Path lang = GENERATED.resolve("assets/" + CzechCraft.MOD_ID + "/lang/cs_cz.json");
    assertTrue(Files.exists(lang));
    JsonObject root = JsonParser.parseString(Files.readString(lang)).getAsJsonObject();
    assertEquals("Rohlík", root.get(ROHLIK_TRANSLATION_KEY).getAsString());
    assertEquals("CzechCraft", root.get(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY).getAsString());
  }
}
