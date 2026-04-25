package cz.czechcraft.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Datagen entry point — Fabric calls this from the {@code runDatagen} task.
 *
 * <p>Each provider here generates a slice of the resource/data tree (recipes, models, lang). Adding
 * new content == adding lines inside an existing provider, not adding new providers.
 */
public final class CzechCraftDataGenerator implements DataGeneratorEntrypoint {

  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();

    pack.addProvider(ModRecipeProvider::new);
    pack.addProvider(ModModelProvider::new);
    pack.addProvider(ModEnglishLangProvider::new);
    pack.addProvider(ModCzechLangProvider::new);
  }
}
