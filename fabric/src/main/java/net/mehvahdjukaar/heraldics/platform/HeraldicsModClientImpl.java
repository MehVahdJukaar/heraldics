package net.mehvahdjukaar.heraldics.platform;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.HeraldicsModClient;
import net.mehvahdjukaar.heraldics.client.TabardArmorModel;
import net.mehvahdjukaar.heraldics.client.TabardArmorRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class HeraldicsModClientImpl {

    public static void registerArmorRenderers() {
        //waits for setup since it needs the item instances
        ClientHelper.addClientSetup(() -> ArmorRenderer.register(
                (poseStack, buffer, stack, entity, slot, light, contextModel) -> {
                    TabardArmorModel model = HeraldicsModClient.getTabardArmorModel();
                    model.setupFrom(contextModel);
                    ArmorRenderer.renderPart(poseStack, buffer, light, stack, model,
                            HeraldicsModClient.getTabardArmorTexture());
                    TabardArmorRenderer.renderPatterns(poseStack, buffer, light, stack, model);
                },
                HeraldicsMod.TABARD_CHESTPLATE.get()));
    }
}
