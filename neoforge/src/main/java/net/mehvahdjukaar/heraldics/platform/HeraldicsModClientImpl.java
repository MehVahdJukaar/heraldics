package net.mehvahdjukaar.heraldics.platform;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class HeraldicsModClientImpl {

    public static void registerArmorRenderers() {
        ModLoadingContext.get().getActiveContainer().getEventBus()
                .addListener(HeraldicsModClientImpl::onRegisterClientExtensions);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType,
                                               Model original) {
                return HeraldicsModClient.getTabardArmorModel();
            }
        }, HeraldicsMod.TABARD_CHESTPLATE.get());
    }
}
