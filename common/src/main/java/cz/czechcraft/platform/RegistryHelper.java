package cz.czechcraft.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Loader-agnostic abstraction for registering objects. Each loader module (fabric, neoforge, ...)
 * supplies an implementation backed by its native registry API.
 */
public interface RegistryHelper {

  void registerItem(ResourceKey<Item> key, Item item);
}
