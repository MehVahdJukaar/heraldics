package net.mehvahdjukaar.heraldics.integration;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.mehvahdjukaar.heraldics.common.items.crafting.SpecialRecipeDisplays;

@REIPluginClient
public class REICompat implements REIClientPlugin {

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (var holder : SpecialRecipeDisplays.createMixedStonesDisplays(registry.getRecipeManager())) {
            registry.add(DefaultCraftingDisplay.of(holder));
        }
    }
}
