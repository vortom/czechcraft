package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.platform.RegistryHelper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Central catalogue of all CzechCraft items. Iteration order is insertion order
 * (LinkedHashMap) so the creative tab and tests see a stable sequence.
 *
 * Adding a new item: declare it in the appropriate {@code content/...} class
 * (e.g. {@link FoodItems}), then add one line in the static initialiser below.
 */
public final class ModItems {

    private static final Map<Identifier, Item> ITEMS = new LinkedHashMap<>();

    static {
        add("rohlik", FoodItems.ROHLIK);
    }

    private ModItems() {
        // static-only
    }

    private static void add(String path, Item item) {
        ITEMS.put(Identifier.of(CzechCraft.MOD_ID, path), item);
    }

    /** Returns an unmodifiable view of every CzechCraft item, in insertion order. */
    public static Map<Identifier, Item> getAll() {
        return Collections.unmodifiableMap(ITEMS);
    }

    /** Registers every item with the loader-supplied helper. */
    public static void register(RegistryHelper helper) {
        ITEMS.forEach(helper::registerItem);
    }
}
