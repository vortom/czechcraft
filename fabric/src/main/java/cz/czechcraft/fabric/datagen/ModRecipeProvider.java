package cz.czechcraft.fabric.datagen;

import cz.czechcraft.CzechCraft;
import cz.czechcraft.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

public final class ModRecipeProvider extends FabricRecipeProvider {

  public ModRecipeProvider(
      FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected RecipeProvider createRecipeProvider(
      HolderLookup.Provider registries, RecipeOutput exporter) {
    return new RecipeProvider(registries, exporter) {
      @Override
      public void buildRecipes() {
        shaped(RecipeCategory.FOOD, ModItems.ROHLIK)
            .pattern("WW")
            .define('W', Items.WHEAT)
            .group(CzechCraft.MOD_ID + ":bread")
            .unlockedBy("has_wheat", has(Items.WHEAT))
            .save(exporter);
      }
    };
  }

  @Override
  public String getName() {
    return "CzechCraft Recipes";
  }
}
