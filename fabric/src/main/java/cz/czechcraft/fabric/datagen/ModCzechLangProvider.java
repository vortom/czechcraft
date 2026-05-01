package cz.czechcraft.fabric.datagen;

import cz.czechcraft.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

public final class ModCzechLangProvider extends BaseLangProvider {

  public ModCzechLangProvider(
      FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
    super(output, "cs_cz", registryLookup);
  }

  @Override
  protected void addItemTranslations(TranslationBuilder builder) {
    builder.add(ModItems.ROHLIK, "Rohlík");
  }
}
