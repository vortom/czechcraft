package cz.czechcraft.fabric.datagen;

import cz.czechcraft.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public final class ModModelProvider extends FabricModelProvider {

  public ModModelProvider(FabricPackOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockModelGenerators generator) {
    // no blocks in v1
  }

  @Override
  public void generateItemModels(ItemModelGenerators generator) {
    generator.generateFlatItem(ModItems.ROHLIK, ModelTemplates.FLAT_ITEM);
  }
}
