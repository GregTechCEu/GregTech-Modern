package com.gregtechceu.gtceu.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.data.loot.NumberProviderShortcuts.*;
import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
import static net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.*;
import static net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.*;
import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.*;
import static net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.*;
import static net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator.*;

public class GTLootTables extends LootTableProvider {

    public GTLootTables(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST)
        ));
    }

    public static class ChestLoot implements LootTableSubProvider {

        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> provider) {
        }
    }
}
