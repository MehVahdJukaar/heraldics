package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.function.Function;

public class TabardArmorRenderer {

    private static final ResourceLocation BASE_PATTERN = ResourceLocation.withDefaultNamespace("base");

    private static final Function<ResourceLocation, Material> MATERIALS = Util.memoize(
            a -> new Material(Sheets.BANNER_SHEET, ModClientDynamicResources.patternLayer(a)));
    public static final Function<ResourceLocation, ResourceLocation> LAYER_TEXTURES = Util.memoize(
            a -> ResType.TEXTURES.getPath(ModClientDynamicResources.patternLayer(a)));
    public static final Function<ResourceLocation, ResourceLocation> HORSE_LAYER_TEXTURES = Util.memoize(
            a -> ResType.TEXTURES.getPath(ModClientDynamicResources.horsePatternLayer(a)));

    private static final ResourceLocation HORSE_CLOTH_TEXTURE =
            HeraldicsMod.res("textures/entity/horse/armor/horse_armor_tabard.png");
    private static final ResourceLocation HORSE_MAIL_TEXTURE =
            HeraldicsMod.res("textures/entity/horse/armor/horse_armor_tabard_mail.png");

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                      ItemStack stack, Model model) {
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;
        renderLayer(poseStack, buffer, packedLight, model, BASE_PATTERN, baseColor, RenderType::armorCutoutNoCull);

        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            renderLayer(poseStack, buffer, packedLight, model, layer.pattern().value().assetId(), layer.color(),
                    ModRenderTypes::armorPatternLayer);
        }
    }

    private static void renderLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Model model,
                                    ResourceLocation bannerAsset, DyeColor color,
                                    Function<ResourceLocation, RenderType> renderType) {
        VertexConsumer vc = MATERIALS.apply(bannerAsset).buffer(buffer, renderType);
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color.getTextureDiffuseColor());
    }

    public static void renderHorseTabard(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                         ItemStack stack, HorseModel<Horse> mail, HorseModel<Horse> drapes) {
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;

        mail.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(HORSE_MAIL_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, -1);
        drapes.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(HORSE_CLOTH_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, baseColor.getTextureDiffuseColor());

        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            VertexConsumer vc = buffer.getBuffer(
                    ModRenderTypes.armorPatternLayer(HORSE_LAYER_TEXTURES.apply(layer.pattern().value().assetId())));
            drapes.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY,
                    layer.color().getTextureDiffuseColor());
        }
    }
}
