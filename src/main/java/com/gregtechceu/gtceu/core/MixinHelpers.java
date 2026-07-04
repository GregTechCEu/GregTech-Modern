package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.Map;

@SuppressWarnings("deprecation")
public class MixinHelpers {

    private static final VanillaBlockLoot BLOCK_LOOT = new VanillaBlockLoot();

    public static void generateGTDynamicLoot(Map<ResourceLocation, LootTable> lootTables) {
        GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType type = TagPrefix.ORES.get(prefix);
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(),
                            "blocks/" + blockEntry.getId().getPath());
                    Block block = blockEntry.get();

                    ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);
                    int oreMultiplier = type.isDoubleDrops() ? 2 : 1;

                    LootTable.Builder builder = BlockLootSubProvider.createSilkTouchDispatchTable(block,
                            BLOCK_LOOT.applyExplosionDecay(block,
                                    LootItem.lootTableItem(dropItem.getItem())
                                            .apply(SetItemCountFunction
                                                    .setCount(ConstantValue.exactly(oreMultiplier)))));
                    // disable fortune for balance reasons. (for now, until we can think of a better solution.)
                    // .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));

                    LootPool.Builder pool = LootPool.lootPool();
                    boolean isEmpty = true;
                    for (MaterialStack secondaryMaterial : prefix.secondaryMaterials()) {
                        if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                            ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                            pool.add(LootItem.lootTableItem(dustStack.getItem())
                                    .when(BlockLootSubProvider.HAS_NO_SILK_TOUCH)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                    .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                    .apply(ApplyExplosionDecay.explosionDecay()));
                            isEmpty = false;
                        }
                    }
                    if (!isEmpty) {
                        builder.withPool(pool);
                    }
                    lootTables.put(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build());
                    ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
                });
            } else {
                MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
            }
        });
        GTMaterialBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTMaterialBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTMaterialBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTMaterialBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            LootTable.Builder builder = BLOCK_LOOT
                    .createSingleItemTable(ChemicalHelper.get(TagPrefix.dustTiny, material).getItem(),
                            UniformGenerator.between(3, 5))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE));
            lootTables.put(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build());
            ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
        });
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
            ((BlockBehaviourAccessor) block).setDrops(lootTableId);
            lootTables.put(lootTableId,
                    BLOCK_LOOT.createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }

    public static void addMaterialBlockLootTables(Map<ResourceLocation, LootTable> lootTables, TagPrefix prefix,
                                                  Map<Material, ? extends BlockEntry<? extends Block>> map) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
            lootTables.put(lootTableId,
                    BLOCK_LOOT.createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value != null) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
            if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
                gtExtensions.setFlowingTexture(value.getBuilder().flowing());
                gtExtensions.setStillTexture(value.getBuilder().still());
            }
        }
    }
}
