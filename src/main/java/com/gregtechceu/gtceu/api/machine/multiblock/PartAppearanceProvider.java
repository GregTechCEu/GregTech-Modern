package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

/**
 * Multiblock part "lookalike state" override provider for formed multiblocks.
 * 
 * @see IBlockExtension#getAppearance
 */
@FunctionalInterface
public interface PartAppearanceProvider {

    PartAppearanceProvider DEFAULT = (controller, part, side) -> {
        return controller.getDefinition().getAppearance().get();
    };

    BlockState getPartAppearance(MultiblockControllerMachine controller, MultiblockPartMachine part, Direction side);
}
