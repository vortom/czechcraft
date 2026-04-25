package cz.czechcraft.content.food;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FoodItems}.
 *
 * <p>MC bootstrap cannot run in a plain JUnit environment: both {@code Bootstrap.initialize()} and
 * simply referencing {@code FoodItems.ROHLIK} (which triggers {@code FoodComponent.<clinit>} →
 * {@code ItemStack.<clinit>}) fail with {@code ExceptionInInitializerError} / {@code
 * IllegalAccessError} because Fabric Loader's bytecode transformation (which opens the
 * package-private {@code RegistryEntry$Reference.setRegistryKey} across packages) is not active.
 *
 * <p>Full food-value verification (nutrition=2, saturation=0.3f, snack) therefore happens via the
 * manual {@code runClient} smoke test in plan Task 28.
 *
 * <p>This test verifies only what is testable without a Loom test runtime: that the
 * {@code FoodItems} class is correctly declared (final, package-private constructor).
 */
class FoodItemsTest {

    @Test
    void foodItemsClassIsFinal() {
        // Verifies the utility-class contract is met at the structural level.
        // Accessing FoodItems.class does NOT trigger the static initialiser, so no bootstrap needed.
        assertTrue(java.lang.reflect.Modifier.isFinal(FoodItems.class.getModifiers()),
                "FoodItems must be a final utility class");
    }

    @Test
    void foodItemsHasPrivateNoArgConstructor() throws NoSuchMethodException {
        var ctor = FoodItems.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(ctor.getModifiers()),
                "FoodItems constructor must be private (utility class pattern)");
    }
}
