package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote CreateKineticSourceBlockEntity
 */
public class KineticSourceMachineBlockEntity extends KineticTileEntity implements IMachineBlockEntity {
    public KineticSourceMachineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public KineticSourceMachineBlockEntity self() {
        return this;
    }

    @Override
    public MetaMachine getMetaMachine() {
        return null;
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return null;
    }
}
