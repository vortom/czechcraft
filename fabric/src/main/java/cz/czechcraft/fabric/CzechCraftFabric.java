package cz.czechcraft.fabric;

import cz.czechcraft.CzechCraft;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric loader entry point. Bridges Fabric's lifecycle into the loader-agnostic {@link
 * CzechCraft#init(cz.czechcraft.platform.RegistryHelper)} method, then registers the
 * Fabric-specific creative tab.
 */
public final class CzechCraftFabric implements ModInitializer {

  @Override
  public void onInitialize() {
    CzechCraft.init(new FabricRegistryHelper());
    CzechCraftItemGroupFabric.register();
  }
}
