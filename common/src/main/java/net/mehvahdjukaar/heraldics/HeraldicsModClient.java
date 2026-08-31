package net.mehvahdjukaar.heraldics;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.heraldics.client.TabardArmorModel;
import net.mehvahdjukaar.heraldics.client.TabardHorseArmorModel;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class HeraldicsModClient {

    public static final ModelLayerLocation TABARD_ARMOR_LAYER =
            new ModelLayerLocation(HeraldicsMod.res("tabard_armor"), "main");

    public static final ModelLayerLocation TABARD_HORSE_ARMOR_LAYER =
            new ModelLayerLocation(HeraldicsMod.res("tabard_horse_armor"), "main");

    private static TabardArmorModel tabardArmorModel;
    private static TabardHorseArmorModel tabardHorseArmorModel;

    public static void init() {
        RegHelper.registerDynamicResourceProvider(new ModClientDynamicResources());
        ClientHelper.addModelLayerRegistration(e -> {
            e.register(TABARD_ARMOR_LAYER, TabardArmorModel::createLayer);
            e.register(TABARD_HORSE_ARMOR_LAYER, TabardHorseArmorModel::createLayer);
        });
        ClientHelper.addClientReloadListener(
                () -> (ResourceManagerReloadListener) manager -> {
                    tabardArmorModel = null;
                    tabardHorseArmorModel = null;
                },
                HeraldicsMod.res("tabard_armor_model"));
        registerArmorRenderers();
    }

    public static TabardArmorModel getTabardArmorModel() {
        if (tabardArmorModel == null) {
            tabardArmorModel = new TabardArmorModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TABARD_ARMOR_LAYER));
        }
        return tabardArmorModel;
    }

    public static TabardHorseArmorModel getTabardHorseArmorModel() {
        if (tabardHorseArmorModel == null) {
            tabardHorseArmorModel = new TabardHorseArmorModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TABARD_HORSE_ARMOR_LAYER));
        }
        return tabardHorseArmorModel;
    }

    @PlatformImpl
    public static void registerArmorRenderers() {
        throw new AssertionError();
    }
}
