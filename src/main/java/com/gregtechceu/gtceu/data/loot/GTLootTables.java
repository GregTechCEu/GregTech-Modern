package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition.*;
import static com.gregtechceu.gtceu.common.loot.function.SetEUChargeFunction.setCharge;
import static com.gregtechceu.gtceu.data.loot.NumberProviderShortcuts.*;
import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
import static net.minecraft.world.level.storage.loot.LootTable.lootTable;
import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
import static net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.*;
import static net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.*;
import static net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator.*;
import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.*;
import static net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.*;

// spotless:off
public class GTLootTables extends LootTableProvider {

    public static final ResourceLocation SPAWN_BONUS_CHEST_EXTRA = GTCEu.id("chests/extra/spawn_bonus_chest");
    public static final ResourceLocation SIMPLE_DUNGEON_EXTRA = GTCEu.id("chests/extra/simple_dungeon");
    public static final ResourceLocation DESERT_PYRAMID_EXTRA = GTCEu.id("chests/extra/desert_pyramid");
    public static final ResourceLocation JUNGLE_TEMPLE_EXTRA = GTCEu.id("chests/extra/jungle_temple");
    public static final ResourceLocation JUNGLE_TEMPLE_DISPENSER_EXTRA = GTCEu.id("chests/extra/jungle_temple_dispenser");
    public static final ResourceLocation ABANDONED_MINESHAFT_EXTRA = GTCEu.id("chests/extra/abandoned_mineshaft");
    public static final ResourceLocation VILLAGE_WEAPONSMITH_EXTRA = GTCEu.id("chests/extra/village_weaponsmith");
    public static final ResourceLocation STRONGHOLD_CROSSING_EXTRA = GTCEu.id("chests/extra/stronghold_crossing");
    public static final ResourceLocation STRONGHOLD_CORRIDOR_EXTRA = GTCEu.id("chests/extra/stronghold_corridor");

    public GTLootTables(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST)
        ));
    }

    /**
     * <h1>ALL FORMATTING IS INTENTIONAL! DO NOT CHANGE IT!!</h1>
     * @author screret
     */
    public static class ChestLoot implements LootTableSubProvider {

        // Additional chest loot tables to inject. Note that all roll amounts are lower than in 1.12 because these
        // aren't added into the loot tables' existing pools (they're new pools).
        // That means we must take some care to avoid overfilling loot chests.
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> provider) {
            // Negative roll values are skipped, so most rolls' minimums are weighed toward 0. That's intentional and
            // is done to limit the amount of items added to chests.

            provider.accept(SPAWN_BONUS_CHEST_EXTRA, lootTable()
                    .withPool(spawnBonusChestLoot().setRolls(between(0, 1)))
                    .withPool(spawnBonusChestLoot().setRolls(between(1, 2))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(SIMPLE_DUNGEON_EXTRA, lootTable()
                    // purple drink/sus record loot pool
                    // usually gives nothing, sometimes purple drink and very rarely the sus record
                    .withPool(lootPool()
                            .setRolls(between(-2, 1))
                            .add(lootTableItem(GTItems.BOTTLE_PURPLE_DRINK)
                                    .setWeight(15)
                                    .apply(setCount(between(4, 8))))
                            .add(lootTableItem(GTItems.SUS_RECORD)
                                    .setWeight(1)) // this should appear in 1/64 dungeon chests on average
                    )
                    // basic loot pool
                    .withPool(simpleDungeonLoot().setRolls(between(-1, 2)))
                    // same loot pool again but with the extra rolls condition this time
                    .withPool(simpleDungeonLoot().setRolls(between(-1, 2))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(DESERT_PYRAMID_EXTRA, lootTable()
                    .withPool(desertPyramidLoot().setRolls(between(-1, 3)))
                    .withPool(desertPyramidLoot().setRolls(between(-1, 3))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(JUNGLE_TEMPLE_EXTRA, lootTable()
                    .withPool(lootPool()
                            .setRolls(exactly(1))
                            .add(lootTableItem(GTItems.ZERO_POINT_MODULE).apply(setCharge(Long.MAX_VALUE)))
                            .when(randomChance(0.01f))
                    )
                    .withPool(jungleTempleLoot().setRolls(between(-1, 4)))
                    .withPool(jungleTempleLoot().setRolls(between(-1, 4))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(JUNGLE_TEMPLE_DISPENSER_EXTRA, lootTable()
                    .withPool(lootPool()
                            .setRolls(between(0, 1))
                            .add(lootTableItem(Items.FIRE_CHARGE)
                                    .apply(setCount(between(2, 8)))
                            )
                    )
            );

            provider.accept(ABANDONED_MINESHAFT_EXTRA, lootTable()
                    .withPool(abandonedMineshaftLoot().setRolls(between(-1, 2)))
                    .withPool(abandonedMineshaftLoot().setRolls(between(-1, 2))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(VILLAGE_WEAPONSMITH_EXTRA, lootTable()
                    .withPool(villageWeaponsmithLoot().setRolls(between(-1, 4)))
                    .withPool(villageWeaponsmithLoot().setRolls(between(-1, 4))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(STRONGHOLD_CROSSING_EXTRA, lootTable()
                    .withPool(strongholdCrossingLoot().setRolls(between(-1, 3)))
                    .withPool(strongholdCrossingLoot().setRolls(between(-1, 3))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );

            provider.accept(STRONGHOLD_CORRIDOR_EXTRA, lootTable()
                    .withPool(strongholdCorridorLoot().setRolls(between(-2, 2)))
                    .withPool(strongholdCorridorLoot().setRolls(between(-2, 2))
                            .when(increaseDungeonLootConfigEnabled())
                    )
            );
        }

        private static LootPool.Builder spawnBonusChestLoot() {
            return lootPool()
                    .add(lootTableItem(GTItems.BOTTLE_PURPLE_DRINK)
                            .apply(setCount(between(8, 16))));
        }

        private static LootPool.Builder simpleDungeonLoot() {
            return lootPool()
                    .add(lootTableItem(GTItems.BOTTLE_PURPLE_DRINK)
                            .setWeight(8)
                            .apply(setCount(between(4, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Silver))
                            .setWeight(12)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Lead))
                            .setWeight(3)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Steel))
                            .setWeight(6)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Bronze))
                            .setWeight(6)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Manganese))
                            .setWeight(6)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Emerald))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Ruby))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Sapphire))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GreenSapphire))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Olivine))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetRed))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetYellow))
                            .setWeight(2)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Neodymium))
                            .setWeight(4)
                            .apply(setCount(between(1, 6))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Chromium))
                            .setWeight(4)
                            .apply(setCount(between(1, 6))));
        }

        private static LootPool.Builder desertPyramidLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Silver))
                            .setWeight(12)
                            .apply(setCount(between(4, 16))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Platinum))
                            .setWeight(4)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Ruby))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Sapphire))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GreenSapphire))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Olivine))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetRed))
                            .setWeight(4)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetYellow))
                            .setWeight(4)
                            .apply(setCount(between(2, 8))));
        }

        private static LootPool.Builder jungleTempleLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Bronze))
                            .setWeight(12)
                            .apply(setCount(between(4, 16))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Ruby))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Sapphire))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GreenSapphire))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Olivine))
                            .setWeight(2)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetRed))
                            .setWeight(4)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetYellow))
                            .setWeight(4)
                            .apply(setCount(between(2, 8))));
        }

        private static LootPool.Builder abandonedMineshaftLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Silver))
                            .setWeight(12)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Lead))
                            .setWeight(3)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Steel))
                            .setWeight(6)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Bronze))
                            .setWeight(6)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Manganese))
                            .setWeight(6)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Sapphire))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GreenSapphire))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Olivine))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetRed))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.GarnetYellow))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Ruby))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.gem, GTMaterials.Emerald))
                            .setWeight(2)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(3, 12))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(1, 4))));
        }

        private static LootPool.Builder villageWeaponsmithLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Chromium))
                            .setWeight(6)
                            .apply(setCount(between(1, 4))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Neodymium))
                            .setWeight(6)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Manganese))
                            .setWeight(12)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Steel))
                            .setWeight(12)
                            .apply(setCount(between(4, 12))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Bronze))
                            .setWeight(12)
                            .apply(setCount(between(4, 12))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Brass))
                            .setWeight(12)
                            .apply(setCount(between(4, 12))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(4, 12))));
        }

        private static LootPool.Builder strongholdCrossingLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(6)
                            .apply(setCount(between(4, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Steel))
                            .setWeight(12)
                            .apply(setCount(between(8, 16))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Bronze))
                            .setWeight(12)
                            .apply(setCount(between(8, 16))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.Manganese))
                            .setWeight(12)
                            .apply(setCount(between(4, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Neodymium))
                            .setWeight(6)
                            .apply(setCount(between(4, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Chromium))
                            .setWeight(6)
                            .apply(setCount(between(2, 4))));
        }

        private static LootPool.Builder strongholdCorridorLoot() {
            return lootPool()
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(2, 8))))
                    .add(lootTableItem(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.DamascusSteel))
                            .setWeight(1)
                            .apply(setCount(between(3, 12))));
        }
    }
}
// spotless:on
