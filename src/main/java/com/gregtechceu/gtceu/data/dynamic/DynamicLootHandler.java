package com.gregtechceu.gtceu.data.dynamic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.loot.DungeonLootLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.BiFunction;

@ApiStatus.Internal
public final class DynamicLootHandler {

    private DynamicLootHandler() {}

    public static void generateDynamicLoot(HolderLookup.Provider registries) {
        long startTime = System.currentTimeMillis();
        DynamicLootHandler.generateDynamicLoot0(registries);
        // Initialize dungeon loot additions
        DungeonLootLoader.init();

        GTCEu.LOGGER.info("GregTech dynamic loot table generation took {}ms", System.currentTimeMillis() - startTime);
    }

    private static void generateDynamicLoot0(final HolderLookup.Provider registries) {
        final VanillaBlockLoot helpers = new VanillaBlockLoot(registries);
        final DynamicOps<JsonElement> serializationContext = registries.createSerializationContext(JsonOps.INSTANCE);

        GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType oreType = TagPrefix.ORES.get(prefix);
                map.forEach((material, block) -> {
                    generateOreBlockLoot(prefix, material, block, oreType, helpers, serializationContext);
                });
            } else {
                addMaterialBlockLootTables(map, helpers, serializationContext);
            }
        });

        // spotless:off
        GTMaterialBlocks.CABLE_BLOCKS.rowMap().values().forEach((map) -> addMaterialBlockLootTables(map, helpers, serializationContext));
        GTMaterialBlocks.FLUID_PIPE_BLOCKS.rowMap().values().forEach((map) -> addMaterialBlockLootTables(map, helpers, serializationContext));
        GTMaterialBlocks.ITEM_PIPE_BLOCKS.rowMap().values().forEach((map) -> addMaterialBlockLootTables(map, helpers, serializationContext));
        addMaterialBlockLootTables(GTMaterialBlocks.SURFACE_ROCK_BLOCKS, serializationContext, (material, block) -> {
            Item tinyDust = ChemicalHelper.getItem(TagPrefix.dustTiny, material);
            if (tinyDust != Items.AIR) {
                return helpers.createSilkTouchDispatchTable(block.get(),
                        helpers.applyExplosionDecay(block,
                                LootItem.lootTableItem(tinyDust)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                        )
                );
            } else {
                // fallback if the tiny dust doesn't exist
                return helpers.createSingleItemTable(block.get());
            }
        });
        // spotless:on

        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation lootTableId = machine.getId().withPrefix("blocks/");
            ((BlockBehaviourAccessor) block).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));

            LootTable lootTable = helpers.createSingleItemTable(block)
                    .setParamSet(LootContextParamSets.BLOCK)
                    .build();
            GTDynamicDataPack.addLootTable(lootTableId, lootTable, serializationContext);
        });
    }

    private static void addMaterialBlockLootTables(Map<Material, ? extends BlockEntry<? extends Block>> map,
                                                   VanillaBlockLoot blockLoot,
                                                   DynamicOps<JsonElement> serializationContext) {
        addMaterialBlockLootTables(map, serializationContext,
                (material, block) -> blockLoot.createSingleItemTable(block.get()));
    }

    private static void addMaterialBlockLootTables(Map<Material, ? extends BlockEntry<? extends Block>> map,
                                                   DynamicOps<JsonElement> serializationContext,
                                                   BiFunction<Material, BlockEntry<?>, LootTable.Builder> lootTableBuilder) {
        map.forEach((material, block) -> {
            ResourceLocation lootTableId = block.getId().withPrefix("blocks/");
            ((BlockBehaviourAccessor) block.get()).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));

            LootTable lootTable = lootTableBuilder.apply(material, block)
                    .setParamSet(LootContextParamSets.BLOCK)
                    .build();
            GTDynamicDataPack.addLootTable(lootTableId, lootTable, serializationContext);
        });
    }

    private static void generateOreBlockLoot(TagPrefix prefix, Material material, BlockEntry<?> blockEntry,
                                             TagPrefix.OreType oreType,
                                             VanillaBlockLoot helpers, DynamicOps<JsonElement> serializationContext) {
        ResourceLocation lootTableId = blockEntry.getId().withPrefix("blocks/");
        Block block = blockEntry.get();
        ((BlockBehaviourAccessor) block).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));

        ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
        if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
        if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);

        int oreMultiplier = oreType.isDoubleDrops() ? 2 : 1;

        // spotless:off
        LootTable.Builder builder = helpers.createSilkTouchDispatchTable(block,
                helpers.applyExplosionDecay(block,
                        LootItem.lootTableItem(dropItem.getItem())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(oreMultiplier)))
                )
        );
        // disable fortune for balance reasons. (for now, until we can think of a better solution.)
        //.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        // spotless:on

        LootPool.Builder pool = LootPool.lootPool();
        boolean isEmpty = true;
        for (MaterialStack secondaryMaterial : prefix.secondaryMaterials()) {
            if (!secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                continue;
            }
            ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
            pool.add(LootItem.lootTableItem(dustStack.getItem())
                    .when(helpers.doesNotHaveSilkTouch())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                    // .apply(ApplyBonusCount.addUniformBonusCount(fortune))
                    .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                    .apply(ApplyExplosionDecay.explosionDecay()));
            isEmpty = false;
        }
        if (!isEmpty) {
            builder.withPool(pool);
        }

        LootTable lootTable = builder
                .setParamSet(LootContextParamSets.BLOCK)
                .build();
        GTDynamicDataPack.addLootTable(lootTableId, lootTable, serializationContext);
    }
}
