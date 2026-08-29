package net.mehvahdjukaar.feudalist.dynamicpack;

import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.feudalist.client.TabardArmorModel;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureCollager;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the tabard's textures: the armor piece, our cloth panels over vanilla chainmail, and one armor
 * layer per banner pattern, the flag shrunk onto the torso panels and flaps so it can be tinted with the
 * layer color like a banner is.
 */
public class ModClientDynamicResources extends DynamicClientResourceProvider {

    private static final String BANNER_FOLDER = "entity/banner";

    private static final ResourceLocation CHAINMAIL_LAYER =
            ResourceLocation.withDefaultNamespace("models/armor/chainmail_layer_1");
    private static final ResourceLocation TABARD_LAYER = FeudalistMod.res("models/armor/tabard_layer_1");
    //the panels the banner ends up on, drawn by hand. everything they leave uncovered is chainmail
    private static final ResourceLocation TABARD_CLOTH = FeudalistMod.res("models/armor/tabard_cloth");

    //vanilla lays head, torso and arms of an armor piece on a 64x32 sheet
    private static final int ARMOR_LAYER_WIDTH = 64;
    private static final int ARMOR_LAYER_HEIGHT = 32;

    private static final int BANNER_TEXTURE_SIZE = 64;
    //front face of the banner flag, a 20x40x1 box at the texture origin. the back of it is this mirrored
    private static final Rect2D FLAG_FACE = new Rect2D(1, 1, 20, 40);

    //the flag is way taller than the tabard, so it gets split over the chest and the flap below it
    private static final int FLAG_ROWS_ON_CHEST = Math.round(FLAG_FACE.height()
            * (float) TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE));

    private static final TextureCollager CHAINMAIL_ONTO_TABARD = mapChainmailOntoTabard();
    private static final TextureCollager CLOTH_OVER_CHAINMAIL = layClothOverChainmail();
    //rects are in the 64x64 layouts, the collager rescales them to whatever the packs actually use
    private static final TextureCollager FLAG_ONTO_PANELS = mapFlagOntoPanels();
    private static final TextureCollager PANELS_ONTO_SHOULDERS = spreadPanelsOverShoulders();

    public ModClientDynamicResources() {
        super(FeudalistMod.res("generated_pack"), PackGenerationStrategy.CACHED);
    }

    /**
     * Location of the armor layer generated for a banner pattern, relative as texture helpers want it.
     */
    public static ResourceLocation patternLayer(ResourceLocation bannerAsset) {
        return FeudalistMod.res("models/armor/tabard/" + bannerAsset.getNamespace() + "/" + bannerAsset.getPath());
    }

    /**
     * The tabard model keeps vanilla's texture offsets for the torso and the arms, so the chainmail sheet
     * goes over as it is. The flaps hang below it and get nothing, they are cloth all the way.
     */
    private static TextureCollager mapChainmailOntoTabard() {
        return TextureCollager.builder(ARMOR_LAYER_WIDTH, ARMOR_LAYER_HEIGHT,
                        TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE)
                .copyFrom(0, 0, ARMOR_LAYER_WIDTH, ARMOR_LAYER_HEIGHT).to(0, 0)
                .build();
    }

    private static TextureCollager layClothOverChainmail() {
        int size = TabardArmorModel.TEXTURE_SIZE;
        return TextureCollager.builder(size, size, size, size)
                .copyFrom(0, 0, size, size).to(0, 0).blended()
                .build();
    }

    private static TextureCollager mapFlagOntoPanels() {
        Rect2D onChest = new Rect2D(FLAG_FACE.x(), FLAG_FACE.y(), FLAG_FACE.width(), FLAG_ROWS_ON_CHEST);
        Rect2D onFlap = new Rect2D(FLAG_FACE.x(), FLAG_FACE.y() + FLAG_ROWS_ON_CHEST,
                FLAG_FACE.width(), FLAG_FACE.height() - FLAG_ROWS_ON_CHEST);

        //the flag shrinks a lot here, box sampling is the only one that keeps the pattern readable.
        //the back panel's u runs the other way round the body, so mirroring the flag there makes both sides
        //show the same design, like a banner does
        return TextureCollager.builder(BANNER_TEXTURE_SIZE, BANNER_TEXTURE_SIZE,
                        TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE)
                .copyFrom(onChest).to(TabardArmorModel.BODY_FRONT_UV).boxScaling()
                .copyFrom(onFlap).to(TabardArmorModel.FRONT_FLAP_UV).boxScaling()
                .copyFrom(onChest).to(TabardArmorModel.BODY_BACK_UV).boxScaling().flippedX()
                .copyFrom(onFlap).to(TabardArmorModel.BACK_FLAP_UV).boxScaling().flippedX()
                .build();
    }

    /**
     * The two strips of top face on either side of the neck are cloth as well, so each shoulder stretches
     * out the end of the chest panel's top row it touches.
     */
    private static TextureCollager spreadPanelsOverShoulders() {
        Rect2D front = TabardArmorModel.BODY_FRONT_UV;
        Rect2D top = TabardArmorModel.BODY_TOP_UV;
        int strip = TabardArmorModel.SHOULDER_WIDTH;

        int size = TabardArmorModel.TEXTURE_SIZE;
        return TextureCollager.builder(size, size, size, size)
                .copyFrom(front.x(), front.y(), strip, 1)
                .to(top.x(), top.y(), strip, top.height())
                .copyFrom(front.x() + front.width() - strip, front.y(), strip, 1)
                .to(top.x() + top.width() - strip, top.y(), strip, top.height())
                .build();
    }

    @Override
    public boolean needsToRegenerate() {
        return super.needsToRegenerate() || PlatHelper.isDev();
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of();
    }

    @Override
    protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept(this::addTabardArmor);
        executor.accept(this::addTabardPatterns);
    }

    private void addTabardArmor(ResourceManager manager, ResourceSink sink) {
        sink.addTextureUnlessPresent(manager, TABARD_LAYER, () -> createArmorLayer(manager));
    }

    private static TextureImage createArmorLayer(ResourceManager manager) throws Exception {
        try (TextureImage chainmail = TextureImage.open(manager, CHAINMAIL_LAYER);
             TextureImage cloth = TextureImage.open(manager, TABARD_CLOTH)) {
            //the model wants a square sheet, at whatever resolution the pack's chainmail uses
            int size = chainmail.imageWidth() * TabardArmorModel.TEXTURE_SIZE / ARMOR_LAYER_WIDTH;
            TextureImage layer = TextureImage.createNew(size, size);
            CHAINMAIL_ONTO_TABARD.apply(chainmail, layer);
            CLOTH_OVER_CHAINMAIL.apply(cloth, layer);
            return layer;
        }
    }

    private void addTabardPatterns(ResourceManager manager, ResourceSink sink) {
        try {
            for (ResourceLocation pattern : ResType.TEXTURES.listRelative(manager, BANNER_FOLDER, false)) {
                //a hand drawn layer sitting at that path wins over anything we could come up with here
                sink.addTextureUnlessPresent(manager, patternLayer(bannerAssetOf(pattern)),
                        () -> createPatternLayer(manager, pattern));
            }
        } catch (Exception e) {
            FeudalistMod.LOGGER.error("Failed to generate tabard armor layers: ", e);
        }
    }

    private static TextureImage createPatternLayer(ResourceManager manager, ResourceLocation pattern) throws Exception {
        try (TextureImage flag = TextureImage.open(manager, pattern)) {
            //keep whatever resolution the pattern pack uses
            int size = flag.imageWidth() * TabardArmorModel.TEXTURE_SIZE / BANNER_TEXTURE_SIZE;
            TextureImage layer = TextureImage.createNew(size, size);
            FLAG_ONTO_PANELS.apply(flag, layer);
            PANELS_ONTO_SHOULDERS.apply(layer, layer);
            return layer;
        }
    }

    private static ResourceLocation bannerAssetOf(ResourceLocation pattern) {
        return pattern.withPath(p -> p.substring(BANNER_FOLDER.length() + 1));
    }
}
