package net.mehvahdjukaar.heraldics.mixins.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.client.TabardArmorRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @Inject(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("TAIL"))
    private <S> void heraldics$submitTabardPatterns(EquipmentClientInfo.LayerType layerType,
                                                    ResourceKey<EquipmentAsset> assetId, Model<? super S> model,
                                                    S state, ItemStack stack, PoseStack poseStack,
                                                    SubmitNodeCollector collector, int lightCoords,
                                                    Identifier playerTextureOverride, int outlineColor, int order,
                                                    CallbackInfo ci) {
        if (stack.is(HeraldicsMod.TABARD_CHESTPLATE.get())) {
            TabardArmorRenderer.submitPatterns(poseStack, collector, lightCoords, stack, model, state, outlineColor);
        }
    }
}
