package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.platform.RegistryHelper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Central catalogue of all CzechCraft items. Iteration order is insertion order (LinkedHashMap) so
 * the creative tab and tests see a stable sequence.
 *
 * <p>Adding a new item: declare a factory in the appropriate {@code content/...} class (e.g. {@link
 * FoodItems#createRohlik}), then add one line to the static fields below using {@link #add(String,
 * Function)}.
 */
public final class ModItems {

  private static final Map<ResourceKey<Item>, Item> ITEMS = new LinkedHashMap<>();
  private static final Map<ResourceKey<Item>, Item> ITEMS_VIEW = Collections.unmodifiableMap(ITEMS);

  public static final Item ROHLIK = add(FoodItems.ROHLIK_PATH, FoodItems::createRohlik);

  private ModItems() {
    // static-only
  }

  private static Item add(String path, Function<ResourceKey<Item>, Item> factory) {
    ResourceKey<Item> key =
        ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(CzechCraft.MOD_ID, path));
    Item item = factory.apply(key);
    ITEMS.put(key, item);
    return item;
  }

  /** Returns an unmodifiable view of every CzechCraft item, in insertion order. */
  public static Map<ResourceKey<Item>, Item> getAll() {
    return ITEMS_VIEW;
  }

  /** Registers every item with the loader-supplied helper. */
  public static void register(RegistryHelper helper) {
    ITEMS.forEach(helper::registerItem);
  }
}
