package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

public final class ModCzechLangProvider extends BaseLangProvider {

  public ModCzechLangProvider(
      FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
    super(output, "cs_cz", registryLookup);
  }

  @Override
  protected void addItemTranslations(TranslationBuilder builder) {
    builder.add(FoodItems.ROHLIK, "Rohlík");
  }
}
