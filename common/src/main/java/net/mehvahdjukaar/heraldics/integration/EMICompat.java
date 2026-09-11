package net.mehvahdjukaar.heraldics.integration;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.mehvahdjukaar.heraldics.common.items.crafting.SpecialRecipeDisplays;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EmiEntrypoint
public class EMICompat implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        for (var holder : SpecialRecipeDisplays.createMixedStonesDisplays(registry.getRecipeManager())) {
            ShapedRecipe shaped = (ShapedRecipe) holder.value();
            List<Ingredient> ingredients = shaped.getIngredients();
            int width = shaped.getWidth();

            List<EmiIngredient> grid = new ArrayList<>(Collections.nCopies(9, EmiStack.EMPTY));
            for (int i = 0; i < ingredients.size(); i++) {
                grid.set(i / width * 3 + i % width, EmiIngredient.of(ingredients.get(i)));
            }
            registry.addRecipe(new EmiCraftingRecipe(grid, EmiStack.of(shaped.getResultItem(null)), holder.id(), false));
        }
    }
}
