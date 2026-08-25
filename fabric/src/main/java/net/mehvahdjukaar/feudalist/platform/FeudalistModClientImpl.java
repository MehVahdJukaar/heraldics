package net.mehvahdjukaar.feudalist.platform;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.feudalist.FeudalistModClient;
import net.mehvahdjukaar.feudalist.client.TabardArmorModel;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class FeudalistModClientImpl {

    public static void registerArmorRenderers() {
        //waits for setup since it needs the item instances
        ClientHelper.addClientSetup(() -> ArmorRenderer.register(
                (poseStack, buffer, stack, entity, slot, light, contextModel) -> {
                    TabardArmorModel model = FeudalistModClient.getTabardArmorModel();
                    model.setupFrom(contextModel);
                    ArmorRenderer.renderPart(poseStack, buffer, light, stack, model,
                            FeudalistModClient.getTabardArmorTexture());
                },
                FeudalistMod.TABARD_CHESTPLATE.get()));
    }
}
