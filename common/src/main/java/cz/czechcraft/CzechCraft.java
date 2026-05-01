package cz.czechcraft;

import cz.czechcraft.platform.RegistryHelper;
import cz.czechcraft.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point for CzechCraft. Loader-agnostic.
 *
 * <p>Each loader module (fabric, neoforge, ...) calls {@link #init(RegistryHelper)} exactly once at
 * mod startup, supplying its own RegistryHelper implementation.
 */
public final class CzechCraft {

  public static final String MOD_ID = "czechcraft";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private CzechCraft() {
    // static-only
  }

  /** Registers all CzechCraft content. Called once per game start by each loader entry point. */
  public static void init(RegistryHelper helper) {
    ModItems.register(helper);
    LOGGER.info("CzechCraft initialised — {} items registered.", ModItems.getAll().size());
  }
}
