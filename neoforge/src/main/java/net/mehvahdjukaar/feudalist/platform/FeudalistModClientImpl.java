package net.mehvahdjukaar.feudalist.platform;

import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.feudalist.FeudalistModClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class FeudalistModClientImpl {

    public static void registerArmorRenderers() {
        //called while our mod is being constructed, so this is our own bus
        ModLoadingContext.get().getActiveContainer().getEventBus()
                .addListener(FeudalistModClientImpl::onRegisterClientExtensions);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                //neoforge copies the pose and part visibility from the original for us
                return FeudalistModClient.getTabardArmorModel();
            }
        }, FeudalistMod.TABARD_CHESTPLATE.get());
    }
}
