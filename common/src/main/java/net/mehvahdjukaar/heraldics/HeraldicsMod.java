package net.mehvahdjukaar.heraldics;


import net.mehvahdjukaar.heraldics.common.blocks.PortcullisBlock;
import net.mehvahdjukaar.heraldics.common.items.crafting.TabardFromBannerRecipe;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


public class HeraldicsMod {

    public static final String MOD_ID = "heraldics";
    public static final Logger LOGGER = LogManager.getLogger("Heraldics");

    public static final boolean SUPP = PlatHelper.isModLoaded("supplementaries");

    public static Identifier res(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static final List<Supplier<? extends ItemLike>> TAB_CONTENT = new ArrayList<>();

    // add blocks below here

    public static final Map<RegHelper.VariantType, Supplier<Block>> FLAGSTONE_BRICKS =
            regBlockSet("flagstone_bricks",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
                            .destroyTime(1)
                            .sound(SoundType.NETHER_BRICKS)

            );

    public static final Map<RegHelper.VariantType, Supplier<Block>> SANDSTONE_MOSAIC =
            regBlockSet("sandstone_mosaic",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)
            );

    public static final Supplier<Block> PORTCULLIS = regBlock("portcullis", PortcullisBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .noOcclusion()
    );

    public static final Supplier<Block> CHISELED_PORTCULLIS = regBlock("chiseled_portcullis", PortcullisBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .noOcclusion()
    );

    public static final Supplier<Block> WROUGHT_IRON_FENCE = regBlock("wrought_iron_fence", IronBarsBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
    );

    public static final Supplier<Block> WROUGHT_IRON_RAILING = regBlock("wrought_iron_railing", IronBarsBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
    );

    // end blocks

    // add items below here

    //same stats as vanilla chainmail, just so our armor pieces can point at our own equipment assets
    public static final ArmorMaterial CHAINMAIL_ARMOR = chainmailLikeMaterial("chainmail");
    public static final ArmorMaterial TABARD_ARMOR = chainmailLikeMaterial("tabard");

    public static final Supplier<Item> CHAINMAIL_HORSE_ARMOR = regItemNotInTab("chainmail_horse_armor",
            p -> new Item(p.horseArmor(CHAINMAIL_ARMOR)));

    public static final Supplier<Item> TABARD_CHESTPLATE = regItemNotInTab("tabard_chestplate",
            p -> new Item(p.humanoidArmor(TABARD_ARMOR, ArmorType.CHESTPLATE)));

    public static final Supplier<Item> TABARD_HORSE_ARMOR = regItemNotInTab("tabard_horse_armor",
            p -> new Item(p.horseArmor(TABARD_ARMOR)));

    // end items

    public static final Supplier<RecipeSerializer<TabardFromBannerRecipe>> TABARD_FROM_BANNER =
            RegHelper.registerSpecialRecipe(res("tabard_from_banner"), TabardFromBannerRecipe::new);


    public static final RegSupplier<CreativeModeTab> TAB = RegHelper.registerCreativeModeTab(
            res("heraldics_tab"), b -> {
                b.icon(() -> TABARD_CHESTPLATE.get().getDefaultInstance())
                        .title(Component.translatable("itemGroup.heraldics"));
            }
    );

    private static <T extends Block> RegSupplier<T> regBlock(String id, Function<BlockBehaviour.Properties, T> factory,
                                                             BlockBehaviour.Properties properties) {
        var s = RegHelper.registerBlockWithItem(res(id), factory, properties);
        TAB_CONTENT.add(s);
        return s;
    }

    private static EnumMap<RegHelper.VariantType, Supplier<Block>> regBlockSet(String id, BlockBehaviour.Properties prop) {
        var s = RegHelper.registerFullBlockSet(res(id), prop);
        TAB_CONTENT.addAll(s.values());
        return s;
    }

    private static <T extends Item> RegSupplier<T> regItem(String id, Function<Item.Properties, T> factory) {
        return regItem(id, factory, new Item.Properties());
    }

    private static <T extends Item> RegSupplier<T> regItemNotInTab(String id, Function<Item.Properties, T> factory) {
        return RegHelper.registerItem(res(id), factory, new Item.Properties());
    }

    private static <T extends Item> RegSupplier<T> regItem(String id, Function<Item.Properties, T> factory,
                                                           Item.Properties properties) {
        var s = RegHelper.registerItem(res(id), factory, properties);
        TAB_CONTENT.add(s);
        return s;
    }

    private static ArmorMaterial chainmailLikeMaterial(String id) {
        ArmorMaterial chain = ArmorMaterials.CHAINMAIL;
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, res(id));
        return new ArmorMaterial(chain.durability(), chain.defense(), chain.enchantmentValue(), chain.equipSound(),
                chain.toughness(), chain.knockbackResistance(), chain.repairIngredient(), asset);
    }

    public static void init() {
        RegHelper.addItemsToTabsRegistration(itemToTabEvent -> {
            itemToTabEvent.add(TAB.getKey(),
                    TAB_CONTENT.stream()
                            .map(Supplier::get)
                            .toArray(ItemLike[]::new));

        });

        if (PlatHelper.getPhysicalSide().isClient()) {
            HeraldicsModClient.init();
        }
    }

}
