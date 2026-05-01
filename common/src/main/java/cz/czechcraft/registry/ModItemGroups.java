package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Loader-agnostic creative-tab metadata. The actual {@link CreativeModeTab} is constructed and
 * registered in the loader module (e.g. {@code CzechCraftItemGroupFabric}).
 */
public final class ModItemGroups {

  public static final ResourceKey<CreativeModeTab> MAIN_GROUP_KEY =
      ResourceKey.create(
          Registries.CREATIVE_MODE_TAB,
          Identifier.fromNamespaceAndPath(CzechCraft.MOD_ID, CzechCraft.MOD_ID));

  /** Translation key for the tab's display name. Lang files supply en_us + cs_cz. */
  public static final String MAIN_GROUP_TRANSLATION_KEY = "itemGroup." + CzechCraft.MOD_ID;

  // Cached because Fabric's icon supplier is invoked every frame the creative tab is on screen.
  // Lazy-initialised on first call (post-world-load) so ItemStack construction is safe under the
  // 26.1 "no ItemStack pre-world-load" rule.
  private static ItemStack mainGroupIcon;

  /** Default icon stack — first item in {@link ModItems}, or {@link ItemStack#EMPTY} if none. */
  public static ItemStack mainGroupIcon() {
    if (mainGroupIcon == null) {
      mainGroupIcon =
          ModItems.getAll().values().stream()
              .findFirst()
              .map(ItemStack::new)
              .orElse(ItemStack.EMPTY);
    }
    return mainGroupIcon;
  }

  private ModItemGroups() {
    // static-only
  }
}
