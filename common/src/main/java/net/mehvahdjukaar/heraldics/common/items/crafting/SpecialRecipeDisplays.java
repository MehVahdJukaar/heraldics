package net.mehvahdjukaar.heraldics.common.items.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpecialRecipeDisplays {

    public static List<RecipeHolder<CraftingRecipe>> createMixedStonesDisplays(RecipeManager manager) {
        List<RecipeHolder<CraftingRecipe>> displays = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof MixedStonesRecipe recipe)){
                continue;
            }

            List<Ingredient> stones = recipe.getStones();
            int count = stones.size();
            int side = (int) Math.sqrt(count);
            NonNullList<Ingredient> slots = NonNullList.withSize(count, Ingredient.EMPTY);
            for (int i = 0; i < count; i++) {
                List<ItemStack> shifted = new ArrayList<>();
                for (int j = 0; j < count; j++) {
                    shifted.addAll(List.of(stones.get((i + j) % count).getItems()));
                }
                slots.set(i, Ingredient.of(shifted.stream()));
            }

            var pattern = new ShapedRecipePattern(side, side, slots, Optional.empty());
            var shaped = new ShapedRecipe("", recipe.category(), pattern, recipe.getResult());
            displays.add(new RecipeHolder<>(holder.id(), shaped));
        }
        return displays;
    }
}
