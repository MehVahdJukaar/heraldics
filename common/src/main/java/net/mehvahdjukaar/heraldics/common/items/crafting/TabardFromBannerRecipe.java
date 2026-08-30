package net.mehvahdjukaar.heraldics.common.items.crafting;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TabardFromBannerRecipe extends CustomRecipe {

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !craft(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return craft(input);
    }

    private static ItemStack craft(CraftingInput input) {
        ItemStack banner = ItemStack.EMPTY;
        ItemStack armor = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BannerItem) {
                if (!banner.isEmpty()) return ItemStack.EMPTY;
                banner = stack;
            } else if (tabardVersionOf(stack) != null) {
                if (!armor.isEmpty()) return ItemStack.EMPTY;
                armor = stack;
            } else return ItemStack.EMPTY;
        }

        if (banner.isEmpty() || armor.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = armor.transmuteCopy(tabardVersionOf(armor), 1);
        result.set(DataComponents.BANNER_PATTERNS, banner.get(DataComponents.BANNER_PATTERNS));
        result.set(DataComponents.BASE_COLOR, ((BannerItem) banner.getItem()).getColor());
        return result;
    }

    @Nullable
    private static Item tabardVersionOf(ItemStack stack) {
        if (stack.is(Items.CHAINMAIL_CHESTPLATE)) return HeraldicsMod.TABARD_CHESTPLATE.get();
        if (stack.is(HeraldicsMod.CHAINMAIL_HORSE_ARMOR.get())) return HeraldicsMod.TABARD_HORSE_ARMOR.get();
        return null;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return HeraldicsMod.TABARD_FROM_BANNER.get();
    }
}
