package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TabardArmorRenderer {

    private static final Identifier BASE_PATTERN = Identifier.withDefaultNamespace("base");

    private static final int FIRST_PATTERN_ORDER = 4;

    private static final Map<Identifier, SpriteId> SPRITES = new HashMap<>();
    private static final Map<Identifier, Identifier> HORSE_TEXTURES = new HashMap<>();

    private static final Identifier HORSE_CLOTH_TEXTURE =
            HeraldicsMod.res("textures/entity/equipment/horse_body/tabard.png");

    public static <S> void submitHorseDrapes(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                                             ItemStack stack, S state, int outlineColor) {
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;

        Model<? super S> drapes = (Model<? super S>) HeraldicsModClient.getTabardHorseArmorModel();
        int order = FIRST_PATTERN_ORDER - 1;
        collector.order(order++).submitModel(drapes, state, poseStack,
                RenderTypes.armorCutoutNoCull(HORSE_CLOTH_TEXTURE), lightCoords, OverlayTexture.NO_OVERLAY,
                baseColor.getTextureDiffuseColor(), null, outlineColor, null);

        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            submitHorseLayer(poseStack, collector.order(order++), lightCoords, drapes, state,
                    layer.pattern().value().assetId(), layer.color(), RenderTypes::armorTranslucent, outlineColor);
        }
    }

    private static <S> void submitHorseLayer(PoseStack poseStack, OrderedSubmitNodeCollector collector, int lightCoords,
                                             Model<? super S> model, S state, Identifier bannerAsset, DyeColor color,
                                             Function<Identifier, RenderType> renderType, int outlineColor) {
        Identifier texture = HORSE_TEXTURES.computeIfAbsent(bannerAsset,
                a -> ModClientDynamicResources.horsePatternLayer(a).withPath(p -> "textures/" + p + ".png"));
        collector.submitModel(model, state, poseStack, renderType.apply(texture), lightCoords,
                OverlayTexture.NO_OVERLAY, color.getTextureDiffuseColor(), null, outlineColor, null);
    }

    public static <S> void submitPatterns(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                                          ItemStack stack, Model<? super S> model, S state, int outlineColor) {
        DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
        if (baseColor == null) return;

        SpriteGetter sprites = Minecraft.getInstance().getAtlasManager();
        int order = FIRST_PATTERN_ORDER;
        submitLayer(poseStack, collector.order(order++), lightCoords, model, state, sprites, BASE_PATTERN, baseColor,
                RenderTypes::armorCutoutNoCull, outlineColor);

        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            submitLayer(poseStack, collector.order(order++), lightCoords, model, state, sprites,
                    layer.pattern().value().assetId(), layer.color(), RenderTypes::armorTranslucent, outlineColor);
        }
    }

    private static <S> void submitLayer(PoseStack poseStack, OrderedSubmitNodeCollector collector, int lightCoords,
                                        Model<? super S> model, S state, SpriteGetter sprites,
                                        Identifier bannerAsset, DyeColor color,
                                        Function<Identifier, RenderType> renderType, int outlineColor) {
        SpriteId sprite = spriteOf(bannerAsset);
        collector.submitModel(model, state, poseStack, sprite.renderType(renderType), lightCoords,
                OverlayTexture.NO_OVERLAY, color.getTextureDiffuseColor(), sprites.get(sprite), outlineColor, null);
    }

    private static SpriteId spriteOf(Identifier bannerAsset) {
        return SPRITES.computeIfAbsent(bannerAsset,
                a -> new SpriteId(Sheets.BANNER_SHEET, ModClientDynamicResources.patternLayer(a)));
    }
}
