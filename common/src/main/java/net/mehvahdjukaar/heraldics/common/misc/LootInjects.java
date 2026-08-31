package net.mehvahdjukaar.heraldics.common.misc;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.Set;

public class LootInjects {

    private static final Identifier CHAINMAIL_HORSE_ARMOR_POOL = HeraldicsMod.res("inject/chainmail_horse_armor");

    private static final Set<Identifier> CHAINMAIL_HORSE_ARMOR_TABLES = Set.of(
            BuiltInLootTables.VILLAGE_WEAPONSMITH.identifier(),
            BuiltInLootTables.PILLAGER_OUTPOST.identifier(),
            BuiltInLootTables.STRONGHOLD_CORRIDOR.identifier(),
            BuiltInLootTables.SIMPLE_DUNGEON.identifier());

    public static void init() {
        RegHelper.addLootTableInjects(event -> {
            if (CHAINMAIL_HORSE_ARMOR_TABLES.contains(event.getTable())) {
                event.addTableReference(CHAINMAIL_HORSE_ARMOR_POOL);
            }
        });
    }
}
