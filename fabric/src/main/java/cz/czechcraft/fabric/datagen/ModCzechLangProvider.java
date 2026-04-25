package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import cz.czechcraft.registry.ModItemGroups;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public final class ModCzechLangProvider extends FabricLanguageProvider {

  public ModCzechLangProvider(
      FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
    super(output, "cs_cz", registryLookup);
  }

  @Override
  public void generateTranslations(
      RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
    builder.add(FoodItems.ROHLIK, "Rohlík");
    builder.add(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY, "CzechCraft");
  }
}
