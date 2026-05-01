package cz.czechcraft.content.food;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Definitions of all CzechCraft food items.
 *
 * <p>Each item is built by a factory method that takes a {@code ResourceKey<Item>} (required by
 * {@code Item.Properties.setId} since MC 26.1) and is invoked from {@link
 * cz.czechcraft.registry.ModItems}.
 */
public final class FoodItems {

  public static final String ROHLIK_PATH = "rohlik";

  /** Rohlík — Czech bread roll. Nutrition 4 (= 2 drumstick icons), modest saturation. */
  public static Item createRohlik(ResourceKey<Item> key) {
    return new Item(
        new Item.Properties()
            .setId(key)
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build()));
  }

  private FoodItems() {
    // static-only
  }
}
