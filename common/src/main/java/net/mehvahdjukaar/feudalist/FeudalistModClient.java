package net.mehvahdjukaar.feudalist;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.feudalist.client.TabardArmorModel;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class FeudalistModClient {

    public static final ModelLayerLocation TABARD_ARMOR_LAYER =
            new ModelLayerLocation(FeudalistMod.res("tabard_armor"), "main");

    private static TabardArmorModel tabardArmorModel;

    public static void init() {
        ClientHelper.addModelLayerRegistration(e -> e.register(TABARD_ARMOR_LAYER, TabardArmorModel::createLayer));
        //the baked parts die with the model set, so throw ours away on reload and bake it again on demand
        ClientHelper.addClientReloadListener(
                () -> (ResourceManagerReloadListener) manager -> tabardArmorModel = null,
                FeudalistMod.res("tabard_armor_model"));
        registerArmorRenderers();
    }

    public static TabardArmorModel getTabardArmorModel() {
        if (tabardArmorModel == null) {
            tabardArmorModel = new TabardArmorModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TABARD_ARMOR_LAYER));
        }
        return tabardArmorModel;
    }

    public static ResourceLocation getTabardArmorTexture() {
        return FeudalistMod.TABARD_ARMOR.get().layers().getFirst().texture(false);
    }

    @PlatformImpl
    public static void registerArmorRenderers() {
        throw new AssertionError();
    }
}
