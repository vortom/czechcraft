package cz.czechcraft.fabric;

import cz.czechcraft.platform.RegistryHelper;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Fabric implementation of {@link RegistryHelper} — delegates straight to the vanilla {@link
 * Registry#register(Registry, Identifier, Object)} call, which is what Fabric API expects mods to
 * use directly.
 */
public final class FabricRegistryHelper implements RegistryHelper {

  @Override
  public void registerItem(Identifier id, Item item) {
    Registry.register(Registries.ITEM, id, item);
  }
}
