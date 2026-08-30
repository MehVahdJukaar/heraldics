package net.mehvahdjukaar.heraldics.platform;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class HeraldicsModClientImpl {

    public static void registerArmorRenderers() {
        //called while our mod is being constructed, so this is our own bus
        ModLoadingContext.get().getActiveContainer().getEventBus()
                .addListener(HeraldicsModClientImpl::onRegisterClientExtensions);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                //neoforge copies the pose and part visibility from the original for us
                return HeraldicsModClient.getTabardArmorModel();
            }
        }, HeraldicsMod.TABARD_CHESTPLATE.get());
    }
}
