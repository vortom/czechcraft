package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

public final class ModEnglishLangProvider extends BaseLangProvider {

  public ModEnglishLangProvider(
      FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
    super(output, "en_us", registryLookup);
  }

  @Override
  protected void addItemTranslations(TranslationBuilder builder) {
    builder.add(FoodItems.ROHLIK, "Rohlík");
  }
}
