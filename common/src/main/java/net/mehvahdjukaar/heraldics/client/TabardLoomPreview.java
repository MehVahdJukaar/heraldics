package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TabardLoomPreview implements LoomItemRenderer {

    private static final ResourceLocation BASE_PATTERN = ResourceLocation.withDefaultNamespace("base");

    private static final int FLAG_X = 141;
    private static final int FLAG_Y = 8;
    private static final int FLAG_WIDTH = 20;
    private static final int FLAG_HEIGHT = 40;

    private static final int CHEST_ROWS = FLAG_HEIGHT * TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE);

    public static final Supplier<LoomItemRenderer> CHESTPLATE = fixed(new TabardLoomPreview(
            ModClientDynamicResources::patternLayer, TabardArmorModel.TEXTURE_SIZE,
            List.of(new Panel(TabardArmorModel.BODY_FRONT_UV, new Rect2D(0, 0, FLAG_WIDTH, CHEST_ROWS)),
                    new Panel(TabardArmorModel.FRONT_FLAP_UV,
                            new Rect2D(0, CHEST_ROWS, FLAG_WIDTH, FLAG_HEIGHT - CHEST_ROWS)))));

    public static final Supplier<LoomItemRenderer> HORSE_ARMOR = fixed(new TabardLoomPreview(
            ModClientDynamicResources::horsePatternLayer, ModClientDynamicResources.HORSE_LAYER_SIZE,
            List.of(new Panel(ModClientDynamicResources.HORSE_FLAG_PANEL,
                    new Rect2D(0, 0, FLAG_WIDTH, FLAG_HEIGHT)))));

    private record Panel(Rect2D from, Rect2D to) {
    }

    private final Function<ResourceLocation, ResourceLocation> layerLocation;
    private final int sheetSize;
    private final List<Panel> panels;
    private final Map<ResourceLocation, ResourceLocation> textures = new HashMap<>();

    private TabardLoomPreview(Function<ResourceLocation, ResourceLocation> layerLocation, int sheetSize,
                              List<Panel> panels) {
        this.layerLocation = layerLocation;
        this.sheetSize = sheetSize;
        this.panels = panels;
    }

    private static Supplier<LoomItemRenderer> fixed(TabardLoomPreview renderer) {
        return () -> renderer;
    }

    @Override
    public boolean render(GuiGraphics graphics, ItemStack bannerSlotStack, ItemStack result,
                          @Nullable BannerPatternLayers patterns, int leftPos, int topPos, float partialTicks) {
        DyeColor baseColor = result.get(DataComponents.BASE_COLOR);
        if (patterns == null || baseColor == null) return true;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawLayer(graphics, BASE_PATTERN, baseColor, leftPos, topPos);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            drawLayer(graphics, layer.pattern().value().assetId(), layer.color(), leftPos, topPos);
        }
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        return true;
    }

    private void drawLayer(GuiGraphics graphics, ResourceLocation bannerAsset, DyeColor color,
                           int leftPos, int topPos) {
        int tint = color.getTextureDiffuseColor();
        graphics.setColor(FastColor.ARGB32.red(tint) / 255f, FastColor.ARGB32.green(tint) / 255f,
                FastColor.ARGB32.blue(tint) / 255f, 1);
        ResourceLocation texture = textures.computeIfAbsent(bannerAsset,
                a -> layerLocation.apply(a).withPath(p -> "textures/" + p + ".png"));
        for (Panel panel : panels) {
            graphics.blit(texture, leftPos + FLAG_X + panel.to().x(), topPos + FLAG_Y + panel.to().y(),
                    panel.to().width(), panel.to().height(),
                    panel.from().x(), panel.from().y(), panel.from().width(), panel.from().height(),
                    sheetSize, sheetSize);
        }
    }
}
