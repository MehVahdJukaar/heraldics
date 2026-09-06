package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TabardLoomPreview implements LoomItemRenderer {

    private static final ResourceLocation BASE_PATTERN = ResourceLocation.withDefaultNamespace("base");

    private static final int FLAG_X = 141;
    private static final int FLAG_Y = 8;
    private static final int FLAG_WIDTH = 20;
    private static final int FLAG_HEIGHT = 40;

    private static final int ICON_X = 4;
    private static final int ICON_Y = 2;
    private static final int ICON_WIDTH = 5;
    private static final int ICON_HEIGHT = 10;

    private static final int CHEST_ROWS = FLAG_HEIGHT * TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE);

    public static final LoomItemRenderer CHESTPLATE = new TabardLoomPreview(
            TabardArmorRenderer.LAYER_TEXTURES, TabardArmorModel.TEXTURE_SIZE,
            List.of(new Panel(TabardArmorModel.BODY_FRONT_UV, new Rect2D(0, 0, FLAG_WIDTH, CHEST_ROWS)),
                    new Panel(TabardArmorModel.FRONT_FLAP_UV,
                            new Rect2D(0, CHEST_ROWS, FLAG_WIDTH, FLAG_HEIGHT - CHEST_ROWS))));

    public static final LoomItemRenderer HORSE_ARMOR = new TabardLoomPreview(
            TabardArmorRenderer.HORSE_LAYER_TEXTURES, ModClientDynamicResources.HORSE_LAYER_SIZE,
            List.of(new Panel(ModClientDynamicResources.HORSE_FLAG_PANEL,
                    new Rect2D(0, 0, FLAG_WIDTH, FLAG_HEIGHT))));

    private record Panel(Rect2D from, Rect2D to) {
    }

    private final Function<ResourceLocation, ResourceLocation> layerTexture;
    private final int sheetSize;
    private final List<Panel> panels;

    private TabardLoomPreview(Function<ResourceLocation, ResourceLocation> layerTexture, int sheetSize,
                              List<Panel> panels) {
        this.layerTexture = layerTexture;
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
        int x = leftPos + FLAG_X;
        int y = topPos + FLAG_Y;
        drawLayer(graphics, BASE_PATTERN, baseColor, x, y, FLAG_WIDTH, FLAG_HEIGHT);
        for (BannerPatternLayers.Layer layer : patterns.layers()) {
            drawLayer(graphics, layer.pattern().value().assetId(), layer.color(), x, y, FLAG_WIDTH, FLAG_HEIGHT);
        }
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        return true;
    }

    @Override
    public boolean renderPatternIcon(GuiGraphics graphics, ItemStack bannerSlotStack,
                                     Holder<BannerPattern> pattern, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawLayer(graphics, BASE_PATTERN, DyeColor.GRAY, x + ICON_X, y + ICON_Y, ICON_WIDTH, ICON_HEIGHT);
        drawLayer(graphics, pattern.value().assetId(), DyeColor.WHITE,
                x + ICON_X, y + ICON_Y, ICON_WIDTH, ICON_HEIGHT);
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        return true;
    }

    private void drawLayer(GuiGraphics graphics, ResourceLocation bannerAsset, DyeColor color,
                           int x, int y, int width, int height) {
        int tint = color.getTextureDiffuseColor();
        graphics.setColor(FastColor.ARGB32.red(tint) / 255f, FastColor.ARGB32.green(tint) / 255f,
                FastColor.ARGB32.blue(tint) / 255f, 1);
        ResourceLocation texture = layerTexture.apply(bannerAsset);
        for (Panel panel : panels) {
            Rect2D to = panel.to();
            graphics.blit(texture, x + to.x() * width / FLAG_WIDTH, y + to.y() * height / FLAG_HEIGHT,
                    to.width() * width / FLAG_WIDTH, to.height() * height / FLAG_HEIGHT,
                    panel.from().x(), panel.from().y(), panel.from().width(), panel.from().height(),
                    sheetSize, sheetSize);
        }
    }
}
