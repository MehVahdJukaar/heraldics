package net.mehvahdjukaar.heraldics;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.heraldics.client.BaseColorTint;
import net.mehvahdjukaar.heraldics.client.TabardArmorModel;
import net.mehvahdjukaar.heraldics.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class HeraldicsModClient {

    public static final ModelLayerLocation TABARD_ARMOR_LAYER =
            new ModelLayerLocation(HeraldicsMod.res("tabard_armor"), "main");

    private static TabardArmorModel tabardArmorModel;

    public static void init() {
        ItemTintSources.ID_MAPPER.put(HeraldicsMod.res("base_color"), BaseColorTint.MAP_CODEC);
        RegHelper.registerDynamicResourceProvider(new ModClientDynamicResources());
        ClientHelper.addModelLayerRegistration(e -> e.register(TABARD_ARMOR_LAYER, TabardArmorModel::createLayer));
        ClientHelper.addClientReloadListener(
                () -> (ResourceManagerReloadListener) manager -> tabardArmorModel = null,
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

    @PlatformImpl
    public static void registerArmorRenderers() {
        throw new AssertionError();
    }
}
