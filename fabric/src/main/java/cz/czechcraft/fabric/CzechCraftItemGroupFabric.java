package cz.czechcraft.fabric;

import cz.czechcraft.registry.ModItemGroups;
import cz.czechcraft.registry.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Registers the CzechCraft creative tab and auto-populates it from {@link ModItems#getAll()} so
 * adding a new item never requires touching this file.
 */
public final class CzechCraftItemGroupFabric {

  private CzechCraftItemGroupFabric() {
    // static-only
  }

  public static void register() {
    CreativeModeTab tab =
        FabricCreativeModeTab.builder()
            .icon(ModItemGroups::mainGroupIcon)
            .title(Component.translatable(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY))
            .displayItems((_, entries) -> ModItems.getAll().values().forEach(entries::accept))
            .build();

    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModItemGroups.MAIN_GROUP_KEY, tab);
  }
}
