package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DungeonLootLoader {

    private DungeonLootLoader() {}

    public static void init() {
        if (ConfigHolder.INSTANCE.worldgen.addLoot || ConfigHolder.INSTANCE.worldgen.increaseDungeonLoot) {
            GTCEu.LOGGER.info("Registering dungeon loot...");
            ChestGenHooks.init();
        }
        if (ConfigHolder.INSTANCE.worldgen.addLoot) {
            ChestGenHooks.addItem(BuiltInLootTables.SPAWN_BONUS_CHEST, GTItems.BOTTLE_PURPLE_DRINK.asStack(), 8, 16, 2);

            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTItems.BOTTLE_PURPLE_DRINK.asStack(), 4, 8, 80);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Silver), 1, 6, 120);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Lead), 1, 6, 30);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Steel), 1, 6, 60);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze), 1, 6, 60);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Manganese), 1, 6, 60);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 1, 6, 10);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Emerald), 1, 6, 20);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, ChemicalHelper.get(TagPrefix.gem, GTMaterials.Ruby),
                    1, 6, 20);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Sapphire), 1, 6, 20);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GreenSapphire), 1, 6, 20);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Olivine), 1, 6, 20);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetRed), 1, 6, 40);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetYellow), 1, 6, 40);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neodymium), 1, 6, 40);
            ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Chromium), 1, 3, 40);

            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Silver), 4, 16, 12);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Platinum), 2, 8, 4);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID, ChemicalHelper.get(TagPrefix.gem, GTMaterials.Ruby),
                    2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Sapphire), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GreenSapphire), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Olivine), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetRed), 2, 8, 4);
            ChestGenHooks.addItem(BuiltInLootTables.DESERT_PYRAMID,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetYellow), 2, 8, 4);

            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    GTItems.ZERO_POINT_MODULE.get().getChargedStack(Long.MAX_VALUE), 1, 1, 1);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze), 4, 16, 12);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, ChemicalHelper.get(TagPrefix.gem, GTMaterials.Ruby),
                    2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Sapphire), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GreenSapphire), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Olivine), 2, 8, 2);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetRed), 2, 8, 4);
            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetYellow), 2, 8, 4);

            ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER, new ItemStack(Items.FIRE_CHARGE, 1), 2, 8,
                    30);

            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Silver), 1, 4, 12);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Lead), 1, 4, 3);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Steel), 1, 4, 6);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze), 1, 4, 6);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Sapphire), 1, 4, 2);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GreenSapphire), 1, 4, 2);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Olivine), 1, 4, 2);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetRed), 1, 4, 4);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.GarnetYellow), 1, 4, 4);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Ruby), 1, 4, 2);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.gem, GTMaterials.Emerald), 1, 4, 2);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 3, 12, 1);
            ChestGenHooks.addItem(BuiltInLootTables.ABANDONED_MINESHAFT,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 1, 4, 1);

            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Chromium), 1, 4, 6);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neodymium), 2, 8, 6);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Manganese), 2, 8, 12);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Steel), 4, 12, 12);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze), 4, 12, 12);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Brass), 4, 12, 12);
            ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 4, 12, 1);

            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 4, 8, 6);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Steel), 8, 16, 12);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Bronze), 8, 16, 12);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Manganese), 4, 8, 12);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neodymium), 4, 8, 6);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CROSSING,
                    ChemicalHelper.get(TagPrefix.dust, GTMaterials.Chromium), 2, 4, 6);

            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CORRIDOR,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 2, 8, 6);
            ChestGenHooks.addItem(BuiltInLootTables.STRONGHOLD_CORRIDOR,
                    ChemicalHelper.get(TagPrefix.ingot, GTMaterials.DamascusSteel), 3, 12, 6);
        }
        if (ConfigHolder.INSTANCE.worldgen.increaseDungeonLoot) {
            ChestGenHooks.addRolls(BuiltInLootTables.SPAWN_BONUS_CHEST, 2, 4);
            ChestGenHooks.addRolls(BuiltInLootTables.SIMPLE_DUNGEON, 1, 3);
            ChestGenHooks.addRolls(BuiltInLootTables.DESERT_PYRAMID, 2, 4);
            ChestGenHooks.addRolls(BuiltInLootTables.JUNGLE_TEMPLE, 4, 8);
            ChestGenHooks.addRolls(BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER, 0, 2);
            ChestGenHooks.addRolls(BuiltInLootTables.ABANDONED_MINESHAFT, 1, 3);
            ChestGenHooks.addRolls(BuiltInLootTables.VILLAGE_WEAPONSMITH, 2, 6);
            ChestGenHooks.addRolls(BuiltInLootTables.STRONGHOLD_CROSSING, 2, 4);
            ChestGenHooks.addRolls(BuiltInLootTables.STRONGHOLD_CORRIDOR, 2, 4);
            ChestGenHooks.addRolls(BuiltInLootTables.STRONGHOLD_LIBRARY, 4, 8);
        }
    }
}
