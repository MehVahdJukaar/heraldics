package net.mehvahdjukaar.feudalist.mixins.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.feudalist.FeudalistModClient;
import net.mehvahdjukaar.feudalist.client.TabardArmorRenderer;
import net.mehvahdjukaar.feudalist.common.items.TabardChestplateItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Appends the banner passes after the base layer, which is already drawn by then on the model our client
 * item extension handed over.
 * Going through ArmorMaterial layers and getArmorLayerTintColor instead would mean declaring all 17
 * possible layers up front, and vanilla draws each of them cutout off a plain texture, so the atlas
 * batching and the soft pattern edges would both be gone.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"))
    private void feudalist$renderTabardPatterns(PoseStack poseStack, MultiBufferSource bufferSource,
                                                LivingEntity entity, EquipmentSlot slot, int packedLight,
                                                HumanoidModel<?> model, float limbSwing, float limbSwingAmount,
                                                float partialTick, float ageInTicks, float netHeadYaw,
                                                float headPitch, CallbackInfo ci) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof TabardChestplateItem) {
            TabardArmorRenderer.renderPatterns(poseStack, bufferSource, packedLight, stack,
                    FeudalistModClient.getTabardArmorModel());
        }
    }
}
