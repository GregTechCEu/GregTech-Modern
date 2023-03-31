package com.gregtechceu.gtceu.common.blockentity.forge;

import com.gregtechceu.gtceu.api.blockentity.forge.MetaMachineBlockEntityImpl;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineBlockEntityImpl
 */
public class KineticMachineBlockEntityImpl extends KineticMachineBlockEntity{
    protected KineticMachineBlockEntityImpl(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public static KineticMachineBlockEntity create(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        return new KineticMachineBlockEntityImpl(typeIn, pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var result = MetaMachineBlockEntityImpl.getCapability(getMetaMachine(), cap, side);
        return result == null ? super.getCapability(cap, side) : result;
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> blockEntityType) {
    }
}
