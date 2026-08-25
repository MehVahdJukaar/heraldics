package net.mehvahdjukaar.feudalist.dynamicpack;

import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.feudalist.client.TabardArmorModel;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureCollager;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureOps;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Turns every banner pattern texture into an armor layer for the tabard chestplate.
 * The shape is taken from the pattern's alpha, squeezed onto the tabard faces, then used to cut
 * that same shape out of the tabard template.
 */
public class ModClientDynamicResources extends DynamicClientResourceProvider {

    private static final String BANNER_FOLDER = "textures/entity/banner/";
    private static final ResourceLocation TABARD_TEMPLATE = FeudalistMod.res("models/armor/tabard_layer_1");

    private static final int BANNER_TEXTURE_SIZE = 64;
    //the two faces of the banner flag, which is a 20x40x1 box at texture origin
    private static final Rect2D FLAG_FRONT = new Rect2D(1, 1, 20, 40);
    private static final Rect2D FLAG_BACK = new Rect2D(22, 1, 20, 40);

    //the flag is way taller than the tabard, so it gets split over the chest and the flap below it
    private static final int FLAG_ROWS_ON_BODY = Math.round(FLAG_FRONT.height()
            * (float) TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE));

    private static final TextureCollager FLAG_TO_TABARD = createFlagToTabardCollager();

    public ModClientDynamicResources() {
        super(FeudalistMod.res("generated_pack"), PackGenerationStrategy.CACHED);
    }

    /**
     * Location of the armor layer generated for a banner pattern, relative as texture helpers want it.
     */
    public static ResourceLocation patternLayer(ResourceLocation bannerAsset) {
        return FeudalistMod.res("models/armor/tabard/" + bannerAsset.getNamespace() + "/" + bannerAsset.getPath());
    }

    private static TextureCollager createFlagToTabardCollager() {
        var builder = TextureCollager.builder(BANNER_TEXTURE_SIZE, BANNER_TEXTURE_SIZE,
                TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE);
        addSide(builder, FLAG_FRONT, TabardArmorModel.BODY_FRONT_UV,
                TabardArmorModel.FRONT_FLAP_OUTER_UV, TabardArmorModel.FRONT_FLAP_INNER_UV);
        addSide(builder, FLAG_BACK, TabardArmorModel.BODY_BACK_UV,
                TabardArmorModel.BACK_FLAP_OUTER_UV, TabardArmorModel.BACK_FLAP_INNER_UV);
        return builder.build();
    }

    //both faces of a flap get the same strip. seen from the other side it reads mirrored, which is what cloth does
    private static void addSide(TextureCollager.Builder builder, Rect2D flag, Rect2D bodyUv, Rect2D... flapUvs) {
        Rect2D onBody = new Rect2D(flag.x(), flag.y(), flag.width(), FLAG_ROWS_ON_BODY);
        Rect2D onFlap = new Rect2D(flag.x(), flag.y() + FLAG_ROWS_ON_BODY,
                flag.width(), flag.height() - FLAG_ROWS_ON_BODY);

        builder.copyFrom(onBody).to(bodyUv).bilinearScaling();
        for (Rect2D flapUv : flapUvs) {
            builder.copyFrom(onFlap).to(flapUv).bilinearScaling();
        }
    }

    @Override
    protected void addDynamicTranslations(AfterLanguageLoadEvent event) {
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of();
    }

    @Override
    public boolean needsToRegenerate() {
        return super.needsToRegenerate() || PlatHelper.isDev();
    }

    @Override
    protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept(this::addTabardPatterns);
    }

    private void addTabardPatterns(ResourceManager manager, ResourceSink sink) {
        var banners = manager.listResources(BANNER_FOLDER, id -> id.getPath().endsWith(".png")
                && !id.getPath().substring(BANNER_FOLDER.length()).contains("/"));

        try (TextureImage template = TextureImage.open(manager, TABARD_TEMPLATE)) {
            for (ResourceLocation texture : banners.keySet()) {
                ResourceLocation asset = bannerAssetOf(texture);
                sink.addTextureUnlessPresent(manager, patternLayer(asset),
                        () -> createPatternLayer(manager, texture, template));
            }
        } catch (Exception e) {
            FeudalistMod.LOGGER.error("Failed to generate tabard armor layers: ", e);
        }
    }

    private static TextureImage createPatternLayer(ResourceManager manager, ResourceLocation bannerTexture,
                                                   TextureImage template) throws Exception {
        try (TextureImage banner = TextureImage.open(manager, bannerTexture.withPath(p -> p.replace("textures/", "")));
             TextureImage shape = TextureImage.createNew(template.imageWidth(), template.imageHeight())) {

            FLAG_TO_TABARD.apply(banner, shape);
            keepHalfCoveredPixels(shape);

            TextureImage layer = template.makeCopy();
            //inverted keeps what the mask covers, which is the pattern
            TextureOps.applyMaskInverted(layer, shape);
            return layer;
        }
    }

    //shrinking the flag smears its alpha over the neighbouring pixels, so drop whatever it barely touched
    private static void keepHalfCoveredPixels(TextureImage shape) {
        shape.forEachPixel(pixel -> {
            if (FastColor.ABGR32.alpha(pixel.getValue()) < 128) pixel.setValue(0);
        });
    }

    private static ResourceLocation bannerAssetOf(ResourceLocation bannerTexture) {
        return bannerTexture.withPath(p -> p.substring(BANNER_FOLDER.length(), p.length() - ".png".length()));
    }
}
