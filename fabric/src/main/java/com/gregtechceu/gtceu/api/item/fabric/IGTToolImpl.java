package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.item.IGTTool;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IGTToolImpl extends IGTTool {
    @Override
    default void definition$init() {
        getToolStats().getBehaviors().forEach(behavior -> behavior.init(this));
    }

    static boolean definition$isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (stack.getItem() instanceof IGTTool gtTool) {
            int toolMiningLevel = gtTool.getTier().getLevel();
            if (toolMiningLevel < MiningLevelManager.getRequiredMiningLevel(state)) {
                return false;
            }
            if (toolMiningLevel < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
                return false;
            } else if (toolMiningLevel < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
                return false;
            } else {
                return toolMiningLevel < 1 && state.is(BlockTags.NEEDS_STONE_TOOL) ? false : gtTool.getToolClasses(stack).stream().anyMatch(type -> type.harvestTags.stream().anyMatch(state::is));
            }
        }
        return false;
    }
}
