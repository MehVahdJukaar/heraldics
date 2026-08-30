package net.mehvahdjukaar.heraldics.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BaseColorTint implements ItemTintSource {

    public static final BaseColorTint INSTANCE = new BaseColorTint();
    public static final MapCodec<BaseColorTint> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        DyeColor color = stack.get(DataComponents.BASE_COLOR);
        return color == null ? -1 : color.getTextureDiffuseColor();
    }

    @Override
    public MapCodec<BaseColorTint> type() {
        return MAP_CODEC;
    }
}
