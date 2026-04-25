package cz.czechcraft.registry;

import cz.czechcraft.CzechCraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Loader-agnostic creative-tab metadata. The actual ItemGroup is constructed and registered in the
 * loader module (e.g. {@code CzechCraftItemGroupFabric}).
 */
public final class ModItemGroups {

  public static final Identifier MAIN_GROUP_ID =
      Identifier.of(CzechCraft.MOD_ID, CzechCraft.MOD_ID);

  /** Translation key for the tab's display name. Lang files supply en_us + cs_cz. */
  public static final String MAIN_GROUP_TRANSLATION_KEY = "itemGroup." + CzechCraft.MOD_ID;

  // Cached because Fabric's icon supplier is invoked every frame the creative tab is on screen.
  // Lazy-initialised on first call so that class-loading order between ModItemGroups and ModItems
  // cannot trigger an empty-registry crash.
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
