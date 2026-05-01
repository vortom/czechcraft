package cz.czechcraft.registry;

import static org.junit.jupiter.api.Assertions.*;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModItemsTest {

  @BeforeAll
  static void bootstrap() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  @Test
  void rohlikIsCatalogued() {
    ResourceKey<Item> expectedKey =
        ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(CzechCraft.MOD_ID, FoodItems.ROHLIK_PATH));
    assertTrue(
        ModItems.getAll().containsKey(expectedKey),
        "ModItems must catalogue Rohlík under czechcraft:rohlik");
    assertSame(ModItems.ROHLIK, ModItems.getAll().get(expectedKey));
  }

  @Test
  void getAllIsUnmodifiable() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> ModItems.getAll().clear(),
        "view must be immutable");
  }
}
