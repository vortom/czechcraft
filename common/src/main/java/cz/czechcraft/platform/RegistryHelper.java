package cz.czechcraft.platform;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Loader-agnostic abstraction for registering objects. Each loader module (fabric, neoforge, ...)
 * supplies an implementation backed by its native registry API.
 */
public interface RegistryHelper {

  void registerItem(Identifier id, Item item);
}
