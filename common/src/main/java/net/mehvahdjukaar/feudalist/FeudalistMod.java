package net.mehvahdjukaar.feudalist;


import net.mehvahdjukaar.feudalist.common.items.TabardChestplateItem;
import net.mehvahdjukaar.feudalist.common.items.TabardHorseArmorItem;
import net.mehvahdjukaar.feudalist.common.items.crafting.TabardFromBannerRecipe;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
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

    private static final List<Supplier<? extends ItemLike>> TAB_CONTENT = new ArrayList<>();

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

    // add items below here

    //same stats as vanilla chainmail, just so our armor pieces can point at our own textures
    public static final RegSupplier<ArmorMaterial> CHAINMAIL_ARMOR = regChainmailLikeMaterial("chainmail");
    public static final RegSupplier<ArmorMaterial> TABARD_ARMOR = regChainmailLikeMaterial("tabard");

    public static final Supplier<AnimalArmorItem> CHAINMAIL_HORSE_ARMOR = regItem("chainmail_horse_armor",
            () -> new AnimalArmorItem(CHAINMAIL_ARMOR, AnimalArmorItem.BodyType.EQUESTRIAN, false,
                    new Item.Properties().stacksTo(1)));

    public static final Supplier<TabardChestplateItem> TABARD_CHESTPLATE = regItem("tabard_chestplate",
            () -> new TabardChestplateItem(TABARD_ARMOR,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15))));

    public static final Supplier<TabardHorseArmorItem> TABARD_HORSE_ARMOR = regItem("tabard_horse_armor",
            () -> new TabardHorseArmorItem(TABARD_ARMOR,
                    new Item.Properties().stacksTo(1)));

    // end items

    public static final Supplier<RecipeSerializer<TabardFromBannerRecipe>> TABARD_FROM_BANNER =
            RegHelper.registerSpecialRecipe(res("tabard_from_banner"), TabardFromBannerRecipe::new);


    public static final RegSupplier<CreativeModeTab> TAB = RegHelper.registerCreativeModeTab(
            res("feudalist_tab"), b -> {
                b.icon(() -> FANCY_STONE.get().asItem().getDefaultInstance())
                        .title(Component.translatable("aa"));
            }
    );

    private static RegSupplier<Block> regBlock(String id, Supplier<Block> blockSupplier) {
        var s = RegHelper.registerBlockWithItem(
                res(id), blockSupplier);
        TAB_CONTENT.add(s);
        return s;
    }

    private static EnumMap<RegHelper.VariantType, Supplier<Block>> regBlockSet(String id, BlockBehaviour.Properties prop) {
        var s = RegHelper.registerFullBlockSet(res(id), prop);
        TAB_CONTENT.addAll(s.values());
        return s;
    }

    private static <T extends Item> RegSupplier<T> regItem(String id, Supplier<T> itemSupplier) {
        var s = RegHelper.registerItem(res(id), itemSupplier);
        TAB_CONTENT.add(s);
        return s;
    }

    private static RegSupplier<ArmorMaterial> regChainmailLikeMaterial(String id) {
        ResourceLocation name = res(id);
        return RegHelper.register(name, () -> {
            ArmorMaterial chain = ArmorMaterials.CHAIN.value();
            return new ArmorMaterial(chain.defense(), chain.enchantmentValue(), chain.equipSound(),
                    chain.repairIngredient(), List.of(new ArmorMaterial.Layer(name)),
                    chain.toughness(), chain.knockbackResistance());
        }, Registries.ARMOR_MATERIAL);
    }

    public static void init() {
        RegHelper.addItemsToTabsRegistration(itemToTabEvent -> {
            itemToTabEvent.add((ResourceKey<CreativeModeTab>) TAB.getKey(),
                    TAB_CONTENT.stream()
                            .map(Supplier::get)
                            .toArray(ItemLike[]::new));

        });

        if (PlatHelper.getPhysicalSide().isClient()) {
            FeudalistModClient.init();
        }
    }

}
