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

    private static final Identifier TABARD_HORSE_MAIL_LAYER = HeraldicsMod.res("entity/equipment/horse_body/tabard_mail");
    private static final Identifier TABARD_HORSE_MAIL = HeraldicsMod.res("models/armor/tabard_horse_mail");
    private static final Identifier TABARD_HORSE_CLOTH = HeraldicsMod.res("entity/equipment/horse_body/tabard");

    private static final int ARMOR_LAYER_WIDTH = 64;
    private static final int ARMOR_LAYER_HEIGHT = 32;

    private static final int BANNER_TEXTURE_SIZE = 64;
    private static final Rect2D FLAG_FACE = new Rect2D(1, 1, 20, 40);

    private static final int FLAG_ROWS_ON_CHEST = Math.round(FLAG_FACE.height()
            * (float) TabardArmorModel.BODY_FACE_SIZE
            / (TabardArmorModel.BODY_FACE_SIZE + TabardArmorModel.FLAP_FACE_SIZE));

    private static final int HORSE_LAYER_SIZE = 64;
    private static final int PANEL_WIDTH = 7;
    private static final int PANEL_HEIGHT = 14;
    private static final int PANEL_TOP = 22;
    private static final int FRONT_LEFT_PANEL_X = 31;
    private static final int FRONT_RIGHT_PANEL_X = 16;
    private static final int REAR_LEFT_PANEL_X = 48;
    private static final int REAR_RIGHT_PANEL_X = 63;
    private static final int[] PANEL_X =
            {FRONT_LEFT_PANEL_X, FRONT_RIGHT_PANEL_X, REAR_LEFT_PANEL_X, REAR_RIGHT_PANEL_X};
    private static final int[] NEIGHBOUR_X = {-1, 1, 0, 0};
    private static final int[] NEIGHBOUR_Y = {0, 0, -1, 1};

    private static final TextureCollager CHAINMAIL_ONTO_TABARD = mapChainmailOntoTabard();
    private static final TextureCollager CLOTH_OVER_MAIL = layClothOverMail();
    private static final TextureCollager FLAG_ONTO_PANELS = mapFlagOntoPanels();
    private static final TextureCollager PANELS_ONTO_SHOULDERS = spreadPanelsOverShoulders();
    private static final TextureCollager FLAG_ONTO_DRAPE = mapFlagOntoDrape();
    private static final TextureCollager DRAPE_ONTO_OTHER_PANELS = spreadDrapeOverOtherPanels();

    public ModClientDynamicResources() {
        super(HeraldicsMod.res("generated_pack"), PackGenerationStrategy.CACHED);
    }

    public static Identifier patternLayer(Identifier bannerAsset) {
        return patternLayer("models/armor/tabard/", bannerAsset);
    }

    public static Identifier horsePatternLayer(Identifier bannerAsset) {
        return patternLayer("models/armor/tabard_horse/", bannerAsset);
    }

    private static Identifier patternLayer(String folder, Identifier bannerAsset) {
        return HeraldicsMod.res(folder + bannerAsset.getNamespace() + "/" + bannerAsset.getPath());
    }

    private static TextureCollager mapChainmailOntoTabard() {
        return TextureCollager.builder(ARMOR_LAYER_WIDTH, ARMOR_LAYER_HEIGHT,
                        TabardArmorModel.TEXTURE_SIZE, TabardArmorModel.TEXTURE_SIZE)
                .copyFrom(0, 0, ARMOR_LAYER_WIDTH, ARMOR_LAYER_HEIGHT).to(0, 0)
                .build();
    }

    private static TextureCollager layClothOverMail() {
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

    private static TextureCollager mapFlagOntoDrape() {
        return TextureCollager.builder(BANNER_TEXTURE_SIZE, BANNER_TEXTURE_SIZE, HORSE_LAYER_SIZE, HORSE_LAYER_SIZE)
                .copyFrom(FLAG_FACE)
                .to(FRONT_LEFT_PANEL_X, PANEL_TOP, PANEL_WIDTH, PANEL_HEIGHT).boxScaling()
                .build();
    }

    private static TextureCollager spreadDrapeOverOtherPanels() {
        TextureCollager.Builder builder = TextureCollager.builder(HORSE_LAYER_SIZE, HORSE_LAYER_SIZE,
                HORSE_LAYER_SIZE, HORSE_LAYER_SIZE);
        copyPanel(builder, FRONT_RIGHT_PANEL_X, true);
        copyPanel(builder, REAR_LEFT_PANEL_X, true);
        copyPanel(builder, REAR_RIGHT_PANEL_X, false);
        return builder.build();
    }

    private static void copyPanel(TextureCollager.Builder builder, int x, boolean mirrored) {
        int beforeSeam = Math.min(PANEL_WIDTH, HORSE_LAYER_SIZE - x);
        copyPanelSlice(builder, 0, beforeSeam, x, mirrored);
        if (beforeSeam < PANEL_WIDTH) {
            copyPanelSlice(builder, beforeSeam, PANEL_WIDTH - beforeSeam, 0, mirrored);
        }
    }

    private static void copyPanelSlice(TextureCollager.Builder builder, int column, int width, int x, boolean mirrored) {
        int sourceColumn = mirrored ? PANEL_WIDTH - column - width : column;
        builder.copyFrom(FRONT_LEFT_PANEL_X + sourceColumn, PANEL_TOP, width, PANEL_HEIGHT).to(x, PANEL_TOP);
        if (mirrored) {
            builder.flippedX();
        }
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
        executor.accept(this::addHorseTabardMail);
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
            CLOTH_OVER_MAIL.apply(cloth, layer);
            return layer;
        }
    }

    private void addHorseTabardMail(ResourceManager manager, ResourceSink sink) {
        sink.addTextureUnlessPresent(manager, TABARD_HORSE_MAIL_LAYER, () -> createHorseMailLayer(manager));
    }

    private static TextureImage createHorseMailLayer(ResourceManager manager) throws Exception {
        try (TextureImage mail = TextureImage.open(manager, TABARD_HORSE_MAIL);
             TextureImage cloth = TextureImage.open(manager, TABARD_HORSE_CLOTH)) {
            TextureImage layer = mail.makeCopy();
            cutClothFromMail(layer, cloth, 0, 54, 0, 22, 64, 10);
            cutClothFromMail(layer, cloth, 22, 32, 22, 0, 10, 22);
            cutClothFromMail(layer, cloth, 32, 32, 32, 0, 10, 22);
            return layer;
        }
    }

    private static void cutClothFromMail(TextureImage mail, TextureImage cloth, int mailX, int mailY,
                                         int clothX, int clothY, int width, int height) {
        int scale = Math.max(1, mail.imageWidth() / HORSE_LAYER_SIZE);
        for (int y = 0; y < height * scale; y++) {
            for (int x = 0; x < width * scale; x++) {
                if (isSolid(sampleCloth(cloth, clothX * scale + x, clothY * scale + y, mail.imageWidth()))) {
                    mail.setPixel(mailX * scale + x, mailY * scale + y, 0);
                }
            }
        }
    }

    private void addTabardPatterns(ResourceManager manager, ResourceSink sink) {
        try {
            for (Identifier pattern : ResType.TEXTURES.listRelative(manager, BANNER_FOLDER, false)) {
                Identifier bannerAsset = bannerAssetOf(pattern);
                sink.addTextureUnlessPresent(manager, patternLayer(bannerAsset),
                        () -> createPatternLayer(manager, pattern));
                sink.addTextureUnlessPresent(manager, horsePatternLayer(bannerAsset),
                        () -> createHorsePatternLayer(manager, pattern));
            }
        } catch (Exception e) {
            HeraldicsMod.LOGGER.error("Failed to generate tabard armor layers: ", e);
        }
    }

    private static TextureImage createPatternLayer(ResourceManager manager, Identifier pattern) throws Exception {
        try (TextureImage flag = TextureImage.open(manager, pattern);
             TextureImage cloth = TextureImage.open(manager, TABARD_CLOTH)) {
            int size = flag.imageWidth() * TabardArmorModel.TEXTURE_SIZE / BANNER_TEXTURE_SIZE;
            TextureImage layer = TextureImage.createNew(size, size);
            FLAG_ONTO_PANELS.apply(flag, layer);
            PANELS_ONTO_SHOULDERS.apply(layer, layer);
            applyClothFabric(layer, cloth);
            return layer;
        }
    }

    private static TextureImage createHorsePatternLayer(ResourceManager manager, Identifier pattern) throws Exception {
        try (TextureImage flag = TextureImage.open(manager, pattern);
             TextureImage cloth = TextureImage.open(manager, TABARD_HORSE_CLOTH)) {
            int size = flag.imageWidth() * HORSE_LAYER_SIZE / BANNER_TEXTURE_SIZE;
            TextureImage layer = TextureImage.createNew(size, size);
            FLAG_ONTO_DRAPE.apply(flag, layer);
            DRAPE_ONTO_OTHER_PANELS.apply(layer, layer);
            spreadPanelsOverRestOfCloth(layer, cloth);
            applyClothFabric(layer, cloth);
            return layer;
        }
    }

    private static void spreadPanelsOverRestOfCloth(TextureImage layer, TextureImage cloth) {
        int size = layer.imageWidth();
        int scale = Math.max(1, size / HORSE_LAYER_SIZE);
        int[] queue = new int[size * size];
        int[] colors = new int[size * size];
        boolean[] taken = new boolean[size * size];
        int tail = 0;
        for (int panelX : PANEL_X) {
            for (int dy = 0; dy < PANEL_HEIGHT * scale; dy++) {
                for (int dx = 0; dx < PANEL_WIDTH * scale; dx++) {
                    int x = (panelX * scale + dx) % size;
                    int y = PANEL_TOP * scale + dy;
                    int index = x + y * size;
                    taken[index] = true;
                    int color = layer.getPixel(x, y);
                    if (hasAlpha(color)) {
                        colors[index] = color;
                        queue[tail++] = index;
                    }
                }
            }
        }
        //the flood travels through empty space too, but only paints where the cloth is solid, so
        //cloth islands cut off by holes still get the closest panel color
        for (int head = 0; head < tail; head++) {
            int index = queue[head];
            int x = index % size;
            int y = index / size;
            for (int i = 0; i < NEIGHBOUR_X.length; i++) {
                int nx = x + NEIGHBOUR_X[i];
                int ny = y + NEIGHBOUR_Y[i];
                if (nx < 0 || ny < 0 || nx >= size || ny >= size) continue;
                int next = nx + ny * size;
                if (taken[next]) continue;
                taken[next] = true;
                colors[next] = colors[index];
                if (isSolid(sampleCloth(cloth, nx, ny, size))) {
                    layer.setPixel(nx, ny, colors[next]);
                }
                queue[tail++] = next;
            }
        }
    }

    //a pattern is only a scissor cutout: its alpha decides where it covers, the fabric look and
    //shading always come from the cloth underneath
    private static void applyClothFabric(TextureImage layer, TextureImage cloth) {
        int size = layer.imageWidth();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int color = layer.getPixel(x, y);
                if (!hasAlpha(color)) continue;
                int fabric = sampleCloth(cloth, x, y, size);
                if (!hasAlpha(fabric)) continue;
                layer.setPixel(x, y, (color & 0xFF000000) | (fabric & 0xFFFFFF));
            }
        }
    }

    private static int sampleCloth(TextureImage cloth, int x, int y, int layerSize) {
        return cloth.getPixel(x * cloth.imageWidth() / layerSize, y * cloth.imageHeight() / layerSize);
    }

    private static boolean isSolid(int color) {
        return (color >>> 24) == 0xFF;
    }

    private static boolean hasAlpha(int color) {
        return (color >>> 24) != 0;
    }

    private static Identifier bannerAssetOf(Identifier pattern) {
        return pattern.withPath(p -> p.substring(BANNER_FOLDER.length() + 1));
    }
}
