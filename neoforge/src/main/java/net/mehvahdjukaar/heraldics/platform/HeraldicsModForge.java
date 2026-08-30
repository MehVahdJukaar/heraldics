package net.mehvahdjukaar.heraldics.platform;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(HeraldicsMod.MOD_ID)
public class HeraldicsModForge {

    public HeraldicsModForge(IEventBus bus) {
        HeraldicsMod.init();

    }

}
