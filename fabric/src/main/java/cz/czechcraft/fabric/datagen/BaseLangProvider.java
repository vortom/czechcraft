package cz.czechcraft.fabric.datagen;

import cz.czechcraft.registry.ModItemGroups;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

/**
 * Shared scaffolding for per-locale CzechCraft lang providers. Subclasses supply only their item
 * translations; locale-invariant strings (e.g. the creative-tab name) live here so they can never
 * drift between locales.
 */
abstract class BaseLangProvider extends FabricLanguageProvider {

  BaseLangProvider(
      FabricDataOutput output,
      String locale,
      CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
    super(output, locale, registryLookup);
  }

  @Override
  public final void generateTranslations(
      RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
    addItemTranslations(builder);
    builder.add(ModItemGroups.MAIN_GROUP_TRANSLATION_KEY, "CzechCraft");
  }

  protected abstract void addItemTranslations(TranslationBuilder builder);
}
