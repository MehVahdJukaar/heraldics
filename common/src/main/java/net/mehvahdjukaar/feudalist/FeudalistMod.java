package net.mehvahdjukaar.feudalist;


import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class FeudalistMod {

    public static final String MOD_ID = "feudalist";
    public static final Logger LOGGER = LogManager.getLogger("Feudalist");

    public static ResourceLocation res(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static final List<Supplier<Block>> BLOCKS = new ArrayList<>();

    // add blocks below here

    public static final Supplier<Block> FANCY_STONE = regBlock("fancy_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .destroyTime(2)
            )
    );

    public static final Map<RegHelper.VariantType, Supplier<Block>> FLAGSTONE_BRICKS =
            regBlockSet("flagstone_bricks",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
                            .destroyTime(1)
                            .sound(SoundType.NETHER_BRICKS)

            );

    // end blocks


    public static final RegSupplier<CreativeModeTab> TAB = RegHelper.registerCreativeModeTab(
            res("feudalist_tab"), b -> {
                b.icon(() -> FANCY_STONE.get().asItem().getDefaultInstance())
                        .title(Component.translatable("aa"));
            }
    );

    private static RegSupplier<Block> regBlock(String id, Supplier<Block> blockSupplier) {
        var s = RegHelper.registerBlockWithItem(
                res(id), blockSupplier);
        BLOCKS.add(s);
        return s;
    }

    private static EnumMap<RegHelper.VariantType, Supplier<Block>> regBlockSet(String id, BlockBehaviour.Properties prop) {
        var s = RegHelper.registerFullBlockSet(res(id), prop);
        BLOCKS.addAll(s.values());
        return s;
    }

    public static void init() {
        RegHelper.addItemsToTabsRegistration(itemToTabEvent -> {
            itemToTabEvent.add((ResourceKey<CreativeModeTab>) TAB.getKey(),
                    BLOCKS.stream()
                            .map(Supplier::get)
                            .toArray(Block[]::new));

        });

    }

}
