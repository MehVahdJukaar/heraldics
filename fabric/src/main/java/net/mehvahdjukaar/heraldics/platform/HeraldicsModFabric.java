package net.mehvahdjukaar.heraldics.platform;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.heraldics.HeraldicsMod;

public class HeraldicsModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        HeraldicsMod.init();
    }


}
