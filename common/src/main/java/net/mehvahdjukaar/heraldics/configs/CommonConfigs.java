package net.mehvahdjukaar.heraldics.configs;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> ALWAYS_HAND_PUSHABLE;
    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(HeraldicsMod.MOD_ID, ConfigType.COMMON);
        builder.push("portcullis");
        ALWAYS_HAND_PUSHABLE = builder
                .comment("Lets you raise and lower a portcullis by hand even when Supplementaries is on. Off by default so its winch stays the way to move one")
                .define("always_hand_pushable", false);
        builder.pop();
        CONFIG = builder.build();
    }

    public static void init() {
    }
}
