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
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureOps;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Consumer;

/**
 * Turns every banner pattern texture into an armor layer for the tabard chestplate.
 * The shape comes from the pattern's alpha, shrunk onto the tabard panels and then used to fade out
 * everything the pattern does not cover on the tabard template.
 */
public class ModClientDynamicResources extends DynamicClientResourceProvider {

    private static final String BANNER_FOLDER = "entity/banner";
    private static final ResourceLocation TABARD_TEMPLATE = FeudalistMod.res("models/armor/tabard_layer_1");

    private static final int BANNER_TEXTURE_SIZE = 64;
    //front face of the banner flag, a 20x40x1 box at the texture origin. the back of it is this mirrored
    private static final Rect2D FLAG_FACE = new Rect2D(1, 1, 20, 40);

    //the flag is way taller than the tabard, so it gets split over the chest and the flap below it
    private static final int FLAG_ROWS_ON_CHEST = Math.round(FLAG_FACE.height()
            * (float) TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE));

    //rects are in the 64x64 layouts, the collager rescales them to whatever the packs actually use
    private static final TextureCollager FLAG_ONTO_PANELS = mapFlagOntoPanels();
    private static final TextureCollager PANELS_ONTO_REST = spreadPanelsOverTheRest();

    public ModClientDynamicResources() {
        super(FeudalistMod.res("generated_pack"), PackGenerationStrategy.CACHED);
    }

    /**
     * Location of the armor layer generated for a banner pattern, relative as texture helpers want it.
     */
    public static ResourceLocation patternLayer(ResourceLocation bannerAsset) {
        return FeudalistMod.res("models/armor/tabard/" + bannerAsset.getNamespace() + "/" + bannerAsset.getPath());
    }

    private static TextureCollager mapFlagOntoPanels() {
        Rect2D onChest = new Rect2D(FLAG_FACE.x(), FLAG_FACE.y(), FLAG_FACE.width(), FLAG_ROWS_ON_CHEST);
        Rect2D onFlap = new Rect2D(FLAG_FACE.x(), FLAG_FACE.y() + FLAG_ROWS_ON_CHEST,
                FLAG_FACE.width(), FLAG_FACE.height() - FLAG_ROWS_ON_CHEST);

        //the flag shrinks a lot here, box sampling is the only one that keeps the pattern readable.
        //the back panel's u runs the other way round the body, so mirroring the flag there makes both sides
        //show the same design, like a banner does. both faces of a flap share a strip: read from the other
        //side it comes out mirrored, which is what a piece of cloth does anyway
        return TextureCollager.builder(BANNER_TEXTURE_SIZE, BANNER_TEXTURE_SIZE,
                        TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE)
                .copyFrom(onChest).to(TabardArmorModel.BODY_FRONT_UV).boxScaling()
                .copyFrom(onFlap).to(TabardArmorModel.FRONT_FLAP_OUTER_UV).boxScaling()
                .copyFrom(onFlap).to(TabardArmorModel.FRONT_FLAP_INNER_UV).boxScaling()
                .copyFrom(onChest).to(TabardArmorModel.BODY_BACK_UV).boxScaling().flippedX()
                .copyFrom(onFlap).to(TabardArmorModel.BACK_FLAP_OUTER_UV).boxScaling().flippedX()
                .copyFrom(onFlap).to(TabardArmorModel.BACK_FLAP_INNER_UV).boxScaling().flippedX()
                .build();
    }

    /**
     * A pattern that runs off the edge of a panel has to keep going, or the cloth looks like someone cut it
     * out with scissors. Every face around the panels takes the edge it touches and stretches it out.
     */
    private static TextureCollager spreadPanelsOverTheRest() {
        Rect2D front = TabardArmorModel.BODY_FRONT_UV;
        Rect2D back = TabardArmorModel.BODY_BACK_UV;

        //the four vertical corners of the torso, named after which way each one faces
        Rect2D frontLeft = front.column(front.width() - 1);
        Rect2D frontRight = front.column(0);
        Rect2D backLeft = back.column(0);
        Rect2D backRight = back.column(back.width() - 1);

        int size = TabardArmorModel.TEXTURE_SIZE;
        TextureCollager.Builder builder = TextureCollager.builder(size, size, size, size);
        //a side face bridges the two panels, so hand half of it to each
        splitInTwo(builder, TabardArmorModel.BODY_LEFT_SIDE_UV, frontLeft, backLeft);
        splitInTwo(builder, TabardArmorModel.BODY_RIGHT_SIDE_UV, backRight, frontRight);
        wrapArm(builder, TabardArmorModel.RIGHT_ARM_UV, frontRight, backRight);
        wrapArm(builder, TabardArmorModel.LEFT_ARM_UV, frontLeft, backLeft);

        //top and underside of the torso are laid out along the front panel's u. the back panel is the front
        //mirrored and its u runs the other way, so both ends of these faces want the same row anyway
        builder.copyFrom(front.row(0)).to(TabardArmorModel.BODY_TOP_UV);
        builder.copyFrom(front.row(front.height() - 1)).to(TabardArmorModel.BODY_UNDERSIDE_UV);
        return builder.build();
    }

    //an arm strip reads [side][front][side][back] and wraps round, so the front half of it starts halfway
    //into the first side face and the back half runs off the end and comes back at the start
    private static void wrapArm(TextureCollager.Builder builder, Rect2D arm, Rect2D fromFront, Rect2D fromBack) {
        int side = TabardArmorModel.ARM_SIZE;
        int half = side / 2;
        int top = arm.y() + side;

        builder.copyFrom(fromFront).to(arm.x() + half, top, 2 * side, fromFront.height());
        builder.copyFrom(fromBack).to(arm.x() + half + 2 * side, top, side + half, fromBack.height());
        builder.copyFrom(fromBack).to(arm.x(), top, half, fromBack.height());

        //cap on top of the shoulder. its rows run back to front, so it splits along y instead of x
        Rect2D cap = new Rect2D(arm.x() + side, arm.y(), side, side);
        int capHalf = cap.height() / 2;
        builder.copyFrom(fromBack.row(0)).to(cap.x(), cap.y(), cap.width(), capHalf);
        builder.copyFrom(fromFront.row(0)).to(cap.x(), cap.y() + capHalf, cap.width(), capHalf);
    }

    private static void splitInTwo(TextureCollager.Builder builder, Rect2D face, Rect2D firstHalf, Rect2D secondHalf) {
        int half = face.width() / 2;
        builder.copyFrom(firstHalf).to(face.x(), face.y(), half, face.height());
        builder.copyFrom(secondHalf).to(face.x() + half, face.y(), half, face.height());
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
        try (TextureImage template = TextureImage.open(manager, TABARD_TEMPLATE)) {
            for (ResourceLocation pattern : ResType.TEXTURES.listRelative(manager, BANNER_FOLDER, false)) {
                //a hand drawn layer sitting at that path wins over anything we could come up with here
                sink.addTextureUnlessPresent(manager, patternLayer(bannerAssetOf(pattern)),
                        () -> createPatternLayer(manager, pattern, template));
            }
        } catch (Exception e) {
            FeudalistMod.LOGGER.error("Failed to generate tabard armor layers: ", e);
        }
    }

    private static TextureImage createPatternLayer(ResourceManager manager, ResourceLocation pattern,
                                                   TextureImage template) throws Exception {
        try (TextureImage flag = TextureImage.open(manager, pattern);
             TextureImage coverage = TextureImage.createNew(template.imageWidth(), template.imageHeight())) {
            FLAG_ONTO_PANELS.apply(flag, coverage);
            PANELS_ONTO_REST.apply(coverage, coverage);

            //the template holds the colors, the pattern only says how much of each pixel it takes up
            TextureImage layer = template.makeCopy();
            TextureOps.multiplyAlpha(layer, coverage);
            return layer;
        }
    }

    private static ResourceLocation bannerAssetOf(ResourceLocation pattern) {
        return pattern.withPath(p -> p.substring(BANNER_FOLDER.length() + 1));
    }
}
