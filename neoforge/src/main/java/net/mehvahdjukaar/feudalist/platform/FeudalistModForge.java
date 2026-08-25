package net.mehvahdjukaar.feudalist.platform;

import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(FeudalistMod.MOD_ID)
public class FeudalistModForge {

    public FeudalistModForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        FeudalistMod.init();

    }

}
