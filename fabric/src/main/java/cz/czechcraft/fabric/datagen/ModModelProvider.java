package cz.czechcraft.fabric.datagen;

import cz.czechcraft.content.food.FoodItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public final class ModModelProvider extends FabricModelProvider {

  public ModModelProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockStateModelGenerator generator) {
    // no blocks in v1
  }

  @Override
  public void generateItemModels(ItemModelGenerator generator) {
    generator.register(FoodItems.ROHLIK, Models.GENERATED);
  }
}
