package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Draws the banner a tabard was made from over the armor piece, one tinted pass per pattern, the same way
 * a banner stacks its layers. The generated layers are stitched onto the banner atlas, so the whole stack
 * comes out as two draws no matter how many patterns it has.
 */
public class TabardArmorRenderer {

    //full coverage pattern. banners use it for the background color too
    private static final ResourceLocation BASE_PATTERN = ResourceLocation.withDefaultNamespace("base");

    private static final Map<ResourceLocation, Material> MATERIALS = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> HORSE_TEXTURES = new HashMap<>();

    private static final ResourceLocation HORSE_CLOTH_TEXTURE =
            HeraldicsMod.res("textures/entity/horse/armor/horse_armor_tabard.png");
    private static final ResourceLocation HORSE_MAIL_TEXTURE =
            HeraldicsMod.res("textures/entity/horse/armor/horse_armor_tabard_mail.png");

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                      ItemStack stack, Model model) {
        //a tabard that never was a banner just shows the armor texture
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;
        //this is the pass that puts the tabard in the depth buffer. the armor texture under it only covers
        //the torso, so without it the skirt is never depth tested and clouds and glass draw over the top of it
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
        VertexConsumer vc = materialOf(bannerAsset).buffer(buffer, renderType);
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color.getTextureDiffuseColor());
    }

    private static Material materialOf(ResourceLocation bannerAsset) {
        return MATERIALS.computeIfAbsent(bannerAsset,
                a -> new Material(Sheets.BANNER_SHEET, ModClientDynamicResources.patternLayer(a)));
    }

    /**
     * Same stack on a horse, only the drapes are their own model and the layers are plain textures
     * instead of atlas sprites. Both models come in already posed.
     */
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
                    ModRenderTypes.armorPatternLayer(horseTextureOf(layer.pattern().value().assetId())));
            drapes.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY,
                    layer.color().getTextureDiffuseColor());
        }
    }

    private static ResourceLocation horseTextureOf(ResourceLocation bannerAsset) {
        return HORSE_TEXTURES.computeIfAbsent(bannerAsset,
                a -> ModClientDynamicResources.horsePatternLayer(a).withPath(p -> "textures/" + p + ".png"));
    }
}
