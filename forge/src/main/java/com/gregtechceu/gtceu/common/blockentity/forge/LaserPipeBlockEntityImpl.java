package com.gregtechceu.gtceu.common.blockentity.forge;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaserPipeBlockEntityImpl extends LaserPipeBlockEntity {
    protected LaserPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static LaserPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new LaserPipeBlockEntityImpl(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_LASER) {
            if (getLevel().isClientSide())
                return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> clientCapability));

            if (handlers.isEmpty()) {
                initHandlers();
            }
            checkNetwork();
            return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> handlers.getOrDefault(side, defaultHandler)));
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    public static void onBlockEntityRegister(BlockEntityType<LaserPipeBlockEntity> cableBlockEntityBlockEntityType) {

    }
}
