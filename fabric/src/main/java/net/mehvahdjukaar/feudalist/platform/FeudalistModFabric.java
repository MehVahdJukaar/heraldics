package net.mehvahdjukaar.feudalist.platform;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.feudalist.FeudalistMod;

public class FeudalistModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FeudalistMod.init();
    }


}
