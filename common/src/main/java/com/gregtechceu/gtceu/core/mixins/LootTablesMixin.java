package com.gregtechceu.gtceu.core.mixins;

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
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LootDataManager.class)
public abstract class LootTablesMixin {

    @Inject(method = "apply",
            at = @At(value = "HEAD"))
    public void gtceu$injectLootTables(Map<LootDataType<?>, Map<ResourceLocation, ?>> allElements, CallbackInfo ci) {
        if (Platform.isDatagen()) return;

        Map<ResourceLocation, LootTable> lootTables = (Map<ResourceLocation, LootTable>) allElements.get(LootDataType.TABLE);

        GTBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
                    Block block = blockEntry.get();

                    if ((prefix != TagPrefix.oreNetherrack && prefix != TagPrefix.oreEndstone) && !ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes) {
                        TagPrefix orePrefix = TagPrefix.ORES.get(prefix).isNether() ? TagPrefix.ORES.get(prefix).stoneType().get().is(CustomTags.ENDSTONE_ORE_REPLACEABLES) ? TagPrefix.oreEndstone : TagPrefix.oreNetherrack : TagPrefix.ore;
                        block = ChemicalHelper.getBlock(orePrefix, material);
                    }

                    ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);
                    int oreMultiplier = TagPrefix.ORES.get(prefix).isNether() ? 2 : 1;

                    LootTable.Builder builder = BlockLootSubProvider.createSilkTouchDispatchTable(block,
                            new VanillaBlockLoot().applyExplosionDecay(block,
                                    LootItem.lootTableItem(dropItem.getItem())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, material.getProperty(PropertyKey.ORE).getOreMultiplier() * oreMultiplier))))));
                                            //.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))); //disable fortune for balance reasons. (for now, until we can think of a better solution.)

                    Material outputDustMat = GTRegistries.MATERIALS.get(FormattingUtil.toLowerCaseUnder(prefix.name));
                    if (outputDustMat != null) {
                        builder.withPool(LootPool.lootPool().add(
                                LootItem.lootTableItem(ChemicalHelper.get(TagPrefix.dust, outputDustMat).getItem())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                                        .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                        .apply(ApplyExplosionDecay.explosionDecay())));
                    }
                    lootTables.put(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build());
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
        GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
            ((BlockBehaviourAccessor)block).setDrops(lootTableId);
            lootTables.put(lootTableId, new VanillaBlockLoot().createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }
}
