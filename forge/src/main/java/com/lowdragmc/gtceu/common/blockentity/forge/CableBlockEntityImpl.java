package com.lowdragmc.gtceu.common.blockentity.forge;

import com.lowdragmc.gtceu.api.capability.forge.GTCapabilities;
import com.lowdragmc.gtceu.common.blockentity.CableBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote CableBlockEntityImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlockEntityImpl extends CableBlockEntity {
    public CableBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static CableBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new CableBlockEntityImpl(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GTCapabilities.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(this::getEnergyContainer));
        } else if (cap == GTCapabilities.CAPABILITY_COVERABLE) {
            return GTCapabilities.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapabilities.CAPABILITY_TOOLABLE) {
            return GTCapabilities.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    public static void onBlockEntityRegister(BlockEntityType<CableBlockEntity> cableBlockEntityBlockEntityType) {
    }
}
