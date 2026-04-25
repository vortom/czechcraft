package cz.czechcraft.content.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;

/**
 * Definitions of all CzechCraft food items.
 *
 * <p>Each item is a static final field; it is registered in {@link
 * cz.czechcraft.registry.ModItems}.
 */
public final class FoodItems {

  public static final String ROHLIK_PATH = "rohlik";

  /** Rohlík — Czech bread roll. Nutrition 2 (= 1 drumstick icon), light saturation. */
  public static final Item ROHLIK =
      new Item(
          new Item.Settings()
              .food(
                  new FoodComponent.Builder()
                      .nutrition(2)
                      .saturationModifier(0.3f)
                      .snack()
                      .build()));

  private FoodItems() {
    // static-only
  }
}
