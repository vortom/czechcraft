package cz.czechcraft.fabric.datagen;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.content.food.FoodItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

public final class ModRecipeProvider extends FabricRecipeProvider {

  public ModRecipeProvider(
      FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  public void generate(RecipeExporter exporter) {
    ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, FoodItems.ROHLIK, 1)
        .pattern("WW")
        .input('W', Items.WHEAT)
        .group(CzechCraft.MOD_ID + ":bread")
        .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
        .offerTo(exporter);
  }
}
