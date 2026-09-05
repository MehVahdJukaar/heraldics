package net.mehvahdjukaar.heraldics.common.misc;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.Set;

public class LootInjects {

    private static final ResourceLocation CHAINMAIL_HORSE_ARMOR_POOL = HeraldicsMod.res("inject/chainmail_horse_armor");

    private static final Set<ResourceLocation> CHAINMAIL_HORSE_ARMOR_TABLES = Set.of(
            BuiltInLootTables.VILLAGE_WEAPONSMITH.location(),
            BuiltInLootTables.PILLAGER_OUTPOST.location(),
            BuiltInLootTables.STRONGHOLD_CORRIDOR.location(),
            BuiltInLootTables.SIMPLE_DUNGEON.location());

    public static void init() {
        RegHelper.addLootTableInjects(event -> {
            if (CHAINMAIL_HORSE_ARMOR_TABLES.contains(event.getTable())) {
                event.addTableReference(CHAINMAIL_HORSE_ARMOR_POOL);
            }
        });
    }
}
