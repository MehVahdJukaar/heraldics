package net.mehvahdjukaar.heraldics.platform;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.mehvahdjukaar.heraldics.client.TabardArmorModel;
import net.mehvahdjukaar.heraldics.client.TabardArmorRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;

public class HeraldicsModClientImpl {

    public static void registerArmorRenderers() {
        ClientHelper.addClientSetup(() -> ArmorRenderer.register(HeraldicsModClientImpl::tabardRenderer,
                HeraldicsMod.TABARD_CHESTPLATE.get()));
    }

    private static ArmorRenderer tabardRenderer(EntityRendererProvider.Context context) {
        EquipmentLayerRenderer equipmentRenderer = context.getEquipmentRenderer();
        return (poseStack, collector, stack, state, slot, light, contextModel) -> {
            TabardArmorModel model = HeraldicsModClient.getTabardArmorModel();
            EquipmentClientInfo.LayerType layerType = state.isBaby
                    ? EquipmentClientInfo.LayerType.HUMANOID_BABY
                    : EquipmentClientInfo.LayerType.HUMANOID;
            equipmentRenderer.renderLayers(layerType, HeraldicsMod.TABARD_ARMOR.assetId(), model, state, stack,
                    poseStack, collector, light, state.outlineColor);
            TabardArmorRenderer.submitPatterns(poseStack, collector, light, stack, model, state, state.outlineColor);
        };
    }
}
