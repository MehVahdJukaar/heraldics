package net.mehvahdjukaar.heraldics.common.items.crafting;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixedStonesRecipe extends CustomRecipe {

    private final List<Ingredient> stones;
    private final ItemStack result;
    private final int side;

    public MixedStonesRecipe(CraftingBookCategory category, List<Ingredient> stones, ItemStack result) {
        super(category);
        this.stones = stones;
        this.result = result;
        this.side = (int) Math.sqrt(stones.size());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean isFilledSquare = input.width() == side && input.height() == side
                && input.ingredientCount() == stones.size();
        if (!isFilledSquare) return false;

        Set<Item> seen = new HashSet<>();
        for (ItemStack stack : input.items()) {
            if (!seen.add(stack.getItem())) return false;
        }
        return fitsInAnyOrder(input.items(), 0, new boolean[stones.size()]);
    }

    private boolean fitsInAnyOrder(List<ItemStack> stacks, int index, boolean[] usedStones) {
        if (index == stacks.size()) return true;
        for (int i = 0; i < stones.size(); i++) {
            if (usedStones[i] || !stones.get(i).test(stacks.get(index))) continue;
            usedStones[i] = true;
            if (fitsInAnyOrder(stacks, index + 1, usedStones)) return true;
            usedStones[i] = false;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= side && height >= side;
    }

    public List<Ingredient> getStones() {
        return stones;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HeraldicsMod.MIXED_STONES.get();
    }

    public static class Serializer implements RecipeSerializer<MixedStonesRecipe> {

        private static final MapCodec<MixedStonesRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.BUILDING).forGetter(CustomRecipe::category),
                Ingredient.CODEC_NONEMPTY.listOf().validate(Serializer::checkSquare).fieldOf("ingredients").forGetter(MixedStonesRecipe::getStones),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MixedStonesRecipe::getResult)
        ).apply(i, MixedStonesRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MixedStonesRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), MixedStonesRecipe::getStones,
                ItemStack.STREAM_CODEC, MixedStonesRecipe::getResult,
                MixedStonesRecipe::new);

        private static DataResult<List<Ingredient>> checkSquare(List<Ingredient> stones) {
            int side = (int) Math.sqrt(stones.size());
            boolean fillsSquare = side * side == stones.size() && side <= 3;
            if (!fillsSquare) return DataResult.error(() -> "Mixed stones recipe needs 4 or 9 ingredients, got " + stones.size());
            return DataResult.success(stones);
        }

        @Override
        public MapCodec<MixedStonesRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixedStonesRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
