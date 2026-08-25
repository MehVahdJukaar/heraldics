package net.mehvahdjukaar.feudalist.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ModRenderTypes extends RenderType {

    //entity_no_outline with the same view offset the armor render types use. armor sits a hair closer to the
    //camera than the entity, so anything layered over it has to move by the same amount or it z-fights
    public static final Function<ResourceLocation, RenderType> ARMOR_PATTERN_LAYER = Util.memoize(texture -> {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_NO_OUTLINE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return create("feudalist_armor_pattern_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                1536, false, true, state);
    });

    public static RenderType armorPatternLayer(ResourceLocation texture) {
        return ARMOR_PATTERN_LAYER.apply(texture);
    }

    private ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }
}
