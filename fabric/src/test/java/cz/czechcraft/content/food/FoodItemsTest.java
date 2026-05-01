package cz.czechcraft.content.food;

import static org.junit.jupiter.api.Assertions.*;

import cz.czechcraft.CzechCraft;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FoodItemsTest {

  @BeforeAll
  static void bootstrap() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  @Test
  void rohlikFactoryProducesItem() {
    ResourceKey<Item> key =
        ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(CzechCraft.MOD_ID, FoodItems.ROHLIK_PATH));
    Item rohlik = FoodItems.createRohlik(key);
    assertNotNull(rohlik, "factory must return an Item instance");
    // Food-property values themselves are verified by the in-game smoke test.
  }
}
