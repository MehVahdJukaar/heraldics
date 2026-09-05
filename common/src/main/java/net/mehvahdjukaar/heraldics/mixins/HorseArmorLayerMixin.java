package net.mehvahdjukaar.heraldics.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.mehvahdjukaar.heraldics.client.TabardArmorRenderer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Draws the horse tabard in place of the plain armor pass. It is three models over each other,
 * so there is nothing here for vanilla's one texture one model layer to do.
 */
@Mixin(HorseArmorLayer.class)
public abstract class HorseArmorLayerMixin extends RenderLayer<Horse, HorseModel<Horse>> {

    public HorseArmorLayerMixin(RenderLayerParent<Horse, HorseModel<Horse>> parent) {
        super(parent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void heraldics$renderHorseTabard(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                             Horse horse, float limbSwing, float limbSwingAmount, float partialTick,
                                             float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack stack = horse.getBodyArmorItem();
        if (!stack.is(HeraldicsMod.TABARD_HORSE_ARMOR.get())) return;

        HorseModel<Horse> mail = HeraldicsModClient.getTabardHorseMailModel();
        HorseModel<Horse> drapes = HeraldicsModClient.getTabardHorseArmorModel();
        for (HorseModel<Horse> model : List.of(mail, drapes)) {
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(horse, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(horse, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
        TabardArmorRenderer.renderHorseTabard(poseStack, buffer, packedLight, stack, mail, drapes);
        ci.cancel();
    }
}
