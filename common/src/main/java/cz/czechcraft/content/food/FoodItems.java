package cz.czechcraft.content.food;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;

/**
 * Definitions of all CzechCraft food items.
 *
 * Each item is a static final field; it is registered in {@link cz.czechcraft.registry.ModItems}.
 */
public final class FoodItems {

    // -------------------------------------------------------------------------
    // Food-component specs (package-visible for testing without MC bootstrap)
    // -------------------------------------------------------------------------

    static final int ROHLIK_NUTRITION = 2;
    static final float ROHLIK_SATURATION = 0.3f;

    /**
     * Rohlík — Czech bread roll. Nutrition 2 (= 1 drumstick icon), light saturation.
     */
    public static final Item ROHLIK = new Item(
            new Item.Settings()
                    .food(new FoodComponent.Builder()
                            .nutrition(ROHLIK_NUTRITION)
                            .saturationModifier(ROHLIK_SATURATION)
                            .snack()
                            .build()));

    private FoodItems() {
        // static-only
    }
}
