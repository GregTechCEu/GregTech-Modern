package com.gregtechceu.gtceu.core.mixins.sable;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.sable.SableAssemblyRotation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A machine assembled into a rotated contraption keeps its covers on their original faces unless they
 * are turned to match, so a filter or pump cover ends up pointing the wrong way once the contraption
 * settles. Sable locates this hook by testing the block with instanceof, which is why the machine has
 * to carry the listener here through a mixin rather than declaring it on the class directly.
 */
@Mixin(value = MetaMachineBlock.class, remap = false)
public abstract class MetaMachineBlockSubLevelMixin implements BlockSubLevelAssemblyListener {

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos,
                          BlockPos newPos) {
        MetaMachine machine = MetaMachine.getMachine(resultingLevel, newPos);
        if (machine != null) {
            SableAssemblyRotation.rotateCovers(machine.getCoverContainer(), SableAssemblyRotation.current(),
                    resultingLevel.registryAccess());
        }
    }
}
