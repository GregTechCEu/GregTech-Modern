package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
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
            if ((prefix != TagPrefix.oreNetherrack && prefix != TagPrefix.oreEndstone) && !ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes && TagPrefix.ORES.containsKey(prefix)) {
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
                    ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
                    lootTables.put(blockEntry.getId(), BlockLoot.createSingleItemTable(ChemicalHelper.get(TagPrefix.ore, material).getItem()).build());
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
        /*
        GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
         */
    }
}
