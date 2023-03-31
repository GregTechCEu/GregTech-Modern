package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote IMachineBlock
 */
public interface IMachineBlock extends IBlockRendererProvider {
    default Block self() {
        return (Block) this;
    }

    MachineDefinition getDefinition();

    RotationState getRotationState();

    static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }

}
