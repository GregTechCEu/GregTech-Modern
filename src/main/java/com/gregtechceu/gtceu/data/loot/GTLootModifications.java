package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.CompletableFuture;

import static com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition.*;

public class GTLootModifications extends GlobalLootModifierProvider {

    private static final LootItemCondition[] LOOT_CONFIG_ENABLED_CONDITION = { addLootConfigEnabled().build() };

    public GTLootModifications(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, GTCEu.MOD_ID);
    }

    @Override
    protected void start() {
        addAddTableModifier(BuiltInLootTables.SPAWN_BONUS_CHEST, GTLootTables.SPAWN_BONUS_CHEST_EXTRA);
        addAddTableModifier(BuiltInLootTables.SIMPLE_DUNGEON, GTLootTables.SIMPLE_DUNGEON_EXTRA);
        addAddTableModifier(BuiltInLootTables.DESERT_PYRAMID, GTLootTables.DESERT_PYRAMID_EXTRA);
        addAddTableModifier(BuiltInLootTables.JUNGLE_TEMPLE, GTLootTables.JUNGLE_TEMPLE_EXTRA);
        addAddTableModifier(BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER, GTLootTables.JUNGLE_TEMPLE_DISPENSER_EXTRA);
        addAddTableModifier(BuiltInLootTables.ABANDONED_MINESHAFT, GTLootTables.ABANDONED_MINESHAFT_EXTRA);
        addAddTableModifier(BuiltInLootTables.VILLAGE_WEAPONSMITH, GTLootTables.VILLAGE_WEAPONSMITH_EXTRA);
        addAddTableModifier(BuiltInLootTables.STRONGHOLD_CROSSING, GTLootTables.STRONGHOLD_CROSSING_EXTRA);
        addAddTableModifier(BuiltInLootTables.STRONGHOLD_CORRIDOR, GTLootTables.STRONGHOLD_CORRIDOR_EXTRA);
    }

    protected void addAddTableModifier(ResourceKey<LootTable> targetLootTableId,
                                       ResourceKey<LootTable> addedLootTableId) {
        final LootItemCondition[] conditions = ArrayUtils.add(LOOT_CONFIG_ENABLED_CONDITION,
                LootTableIdCondition.builder(targetLootTableId.location()).build());
        add(addedLootTableId.location().getPath(), new AddTableLootModifier(conditions, addedLootTableId));
    }
}
