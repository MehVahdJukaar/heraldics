package net.mehvahdjukaar.heraldics.common.items;

import net.mehvahdjukaar.heraldics.client.TabardLoomPreview;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Supplier;

public class TabardChestplateItem extends ArmorItem implements ILoomItem {

    public TabardChestplateItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }

    @Override
    public DyeColor getLoomBaseColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
    }

    @Override
    public Supplier<LoomItemRenderer> getLoomRenderer() {
        return TabardLoomPreview.CHESTPLATE;
    }
}
