package net.mehvahdjukaar.heraldics.dynamicpack;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.client.TabardArmorModel;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureCollager;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ModClientDynamicResources extends DynamicClientResourceProvider {

    private static final String BANNER_FOLDER = "entity/banner";

    private static final Identifier CHAINMAIL_LAYER =
            Identifier.withDefaultNamespace("entity/equipment/humanoid/chainmail");
    private static final Identifier TABARD_LAYER = HeraldicsMod.res("entity/equipment/humanoid/tabard");
    private static final Identifier TABARD_CLOTH = HeraldicsMod.res("models/armor/tabard_cloth");

    private static final int ARMOR_LAYER_WIDTH = 64;
    private static final int ARMOR_LAYER_HEIGHT = 32;

    private static final int BANNER_TEXTURE_SIZE = 64;
    private static final Rect2D FLAG_FACE = new Rect2D(1, 1, 20, 40);

    private static final int FLAG_ROWS_ON_CHEST = Math.round(FLAG_FACE.height()
            * (float) TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE));

    private static final TextureCollager CHAINMAIL_ONTO_TABARD = mapChainmailOntoTabard();
    private static final TextureCollager CLOTH_OVER_CHAINMAIL = layClothOverChainmail();
    private static final TextureCollager FLAG_ONTO_PANELS = mapFlagOntoPanels();
    private static final TextureCollager PANELS_ONTO_SHOULDERS = spreadPanelsOverShoulders();

    public ModClientDynamicResources() {
        super(HeraldicsMod.res("generated_pack"), PackGenerationStrategy.CACHED);
    }

    public static Identifier patternLayer(Identifier bannerAsset) {
        return HeraldicsMod.res("models/armor/tabard/" + bannerAsset.getNamespace() + "/" + bannerAsset.getPath());
    }

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

        return TextureCollager.builder(BANNER_TEXTURE_SIZE, BANNER_TEXTURE_SIZE,
                        TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE)
                .copyFrom(onChest).to(TabardArmorModel.BODY_FRONT_UV).boxScaling()
                .copyFrom(onFlap).to(TabardArmorModel.FRONT_FLAP_UV).boxScaling()
                .copyFrom(onChest).to(TabardArmorModel.BODY_BACK_UV).boxScaling().flippedX()
                .copyFrom(onFlap).to(TabardArmorModel.BACK_FLAP_UV).boxScaling().flippedX()
                .build();
    }

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
            int size = chainmail.imageWidth() * TabardArmorModel.TEXTURE_SIZE / ARMOR_LAYER_WIDTH;
            TextureImage layer = TextureImage.createNew(size, size);
            CHAINMAIL_ONTO_TABARD.apply(chainmail, layer);
            CLOTH_OVER_CHAINMAIL.apply(cloth, layer);
            return layer;
        }
    }

    private void addTabardPatterns(ResourceManager manager, ResourceSink sink) {
        try {
            for (Identifier pattern : ResType.TEXTURES.listRelative(manager, BANNER_FOLDER, false)) {
                sink.addTextureUnlessPresent(manager, patternLayer(bannerAssetOf(pattern)),
                        () -> createPatternLayer(manager, pattern));
            }
        } catch (Exception e) {
            HeraldicsMod.LOGGER.error("Failed to generate tabard armor layers: ", e);
        }
    }

    private static TextureImage createPatternLayer(ResourceManager manager, Identifier pattern) throws Exception {
        try (TextureImage flag = TextureImage.open(manager, pattern)) {
            int size = flag.imageWidth() * TabardArmorModel.TEXTURE_SIZE / BANNER_TEXTURE_SIZE;
            TextureImage layer = TextureImage.createNew(size, size);
            FLAG_ONTO_PANELS.apply(flag, layer);
            PANELS_ONTO_SHOULDERS.apply(layer, layer);
            return layer;
        }
    }

    private static Identifier bannerAssetOf(Identifier pattern) {
        return pattern.withPath(p -> p.substring(BANNER_FOLDER.length() + 1));
    }
}
