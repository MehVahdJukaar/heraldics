package net.mehvahdjukaar.feudalist.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.feudalist.dynamicpack.ModClientDynamicResources;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws the banner a tabard was made from over the armor piece, one tinted pass per pattern, the same way
 * a banner stacks its layers. The generated layers are stitched onto the banner atlas, so all the passes
 * share a render type and come out as a single draw.
 */
public class TabardArmorRenderer {

    //full coverage pattern. banners use it for the background color too
    private static final ResourceLocation BASE_PATTERN = ResourceLocation.withDefaultNamespace("base");

    private static final Map<ResourceLocation, Material> MATERIALS = new HashMap<>();

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                      ItemStack stack, Model model) {
        //a tabard that never was a banner just shows the armor texture
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;
        renderLayer(poseStack, buffer, packedLight, model, BASE_PATTERN, baseColor);

        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            renderLayer(poseStack, buffer, packedLight, model, layer.pattern().value().assetId(), layer.color());
        }
    }

    private static void renderLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Model model,
                                    ResourceLocation bannerAsset, DyeColor color) {
        //what banners use, plus the armor view offset. it blends instead of cutting out, and it doesn't write
        //depth, so every layer can sit on the exact same faces without fighting the one under it
        VertexConsumer vc = materialOf(bannerAsset).buffer(buffer, ModRenderTypes::armorPatternLayer);
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color.getTextureDiffuseColor());
    }

    private static Material materialOf(ResourceLocation bannerAsset) {
        return MATERIALS.computeIfAbsent(bannerAsset,
                a -> new Material(Sheets.BANNER_SHEET, ModClientDynamicResources.patternLayer(a)));
    }
}
