package cz.czechcraft.fabric;

import cz.czechcraft.registry.ModItemGroups;
import cz.czechcraft.registry.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/**
 * Registers the CzechCraft creative tab and auto-populates it from {@link ModItems#getAll()} so
 * adding a new item never requires touching this file.
 */
public final class CzechCraftItemGroupFabric {

  private CzechCraftItemGroupFabric() {
    // static-only
  }

  public static void register() {
    ItemGroup group =
        FabricItemGroup.builder()
            .icon(ModItemGroups::mainGroupIcon)
            .displayName(Text.translatable(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY))
            .entries((displayContext, entries) -> ModItems.getAll().values().forEach(entries::add))
            .build();

    Registry.register(Registries.ITEM_GROUP, ModItemGroups.MAIN_GROUP_ID, group);
  }
}
