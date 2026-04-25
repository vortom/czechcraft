package cz.czechcraft.content.food;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FoodItems}.
 *
 * <p>These tests verify the food-component specification values for each item. We do NOT call
 * {@code Bootstrap.initialize()} here: the Minecraft registry bootstrap requires Fabric Loader's
 * bytecode transformation to make package-private registry internals accessible, which is not
 * available in a plain JUnit environment. Instead, each item's spec is exposed as package-private
 * constants so the values can be verified independently of the MC runtime.
 */
class FoodItemsTest {

    @Test
    void rohlikNutritionIsTwo() {
        assertEquals(2, FoodItems.ROHLIK_NUTRITION, "nutrition should be 2 (= 1 drumstick)");
    }

    @Test
    void rohlikSaturationIsPointThree() {
        assertEquals(
                0.3f,
                FoodItems.ROHLIK_SATURATION,
                0.0001f,
                "saturation modifier should be 0.3f (cookie-tier light snack)");
    }

    @Test
    void rohlikSpecIsNotNullSanityCheck() {
        // The ROHLIK field itself will be initialised only when MC is bootstrapped.
        // Here we simply confirm the spec constants are well-defined (non-zero nutrition).
        assertTrue(FoodItems.ROHLIK_NUTRITION > 0, "nutrition must be positive");
        assertTrue(FoodItems.ROHLIK_SATURATION > 0f, "saturation must be positive");
    }
}
