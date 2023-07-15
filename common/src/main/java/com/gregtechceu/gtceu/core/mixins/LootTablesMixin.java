package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LootTables.class)
public abstract class LootTablesMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void gtceu$injectLootTables(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci, ImmutableMap.Builder<ResourceLocation, LootTable> lootTables) {
        if (Platform.isDatagen()) return;

        GTBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
                    Block block = blockEntry.get();

                    if ((prefix != TagPrefix.oreNetherrack && prefix != TagPrefix.oreEndstone) && !ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes) {
                        TagPrefix orePrefix = TagPrefix.ORES.get(prefix).isNether() ? TagPrefix.ORES.get(prefix).stoneType().get().is(CustomTags.ENDSTONE_ORE_REPLACEABLES) ? TagPrefix.oreEndstone : TagPrefix.oreNetherrack : TagPrefix.ore;
                        block = ChemicalHelper.getBlock(orePrefix, material);
                    }

                    int oreMultiplier = TagPrefix.ORES.get(prefix).isNether() ? 2 : 1;
                    LootTable.Builder builder = BlockLoot.createSilkTouchDispatchTable(block,
                            BlockLoot.applyExplosionDecay(block,
                                    LootItem.lootTableItem(ChemicalHelper.get(TagPrefix.rawOre, material).getItem())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, material.getProperty(PropertyKey.ORE).getOreMultiplier() * oreMultiplier))))
                                            .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));

                    Material outputDustMat = GTRegistries.MATERIALS.get(FormattingUtil.toLowerCaseUnder(prefix.name));
                    if (outputDustMat != null) {
                        builder.withPool(LootPool.lootPool().add(
                                LootItem.lootTableItem(ChemicalHelper.get(TagPrefix.dust, outputDustMat).getItem())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                                        .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                        .apply(ApplyExplosionDecay.explosionDecay())));
                    }
                    lootTables.put(lootTableId, builder.build());
                    ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
                });
            } else {
                MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
            }
        });
        GTBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        /* todo item pipes
        GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
         */
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
            ((BlockBehaviourAccessor)block).setDrops(lootTableId);
            lootTables.put(lootTableId, BlockLoot.createSingleItemTable(block).build());
        });
    }
}
