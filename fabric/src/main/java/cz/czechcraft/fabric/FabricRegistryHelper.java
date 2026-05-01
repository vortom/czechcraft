package cz.czechcraft.fabric;

import cz.czechcraft.platform.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Fabric implementation of {@link RegistryHelper} — delegates straight to {@link
 * Registry#register(Registry, ResourceKey, Object)} on Minecraft's built-in item registry.
 */
public final class FabricRegistryHelper implements RegistryHelper {

  @Override
  public void registerItem(ResourceKey<Item> key, Item item) {
    Registry.register(BuiltInRegistries.ITEM, key, item);
  }
}
