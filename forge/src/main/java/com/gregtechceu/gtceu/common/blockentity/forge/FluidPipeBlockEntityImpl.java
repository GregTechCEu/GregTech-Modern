package com.gregtechceu.gtceu.common.blockentity.forge;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtlib.side.fluid.forge.FluidTransferHelperImpl;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidPipeBlockEntityImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlockEntityImpl extends FluidPipeBlockEntity {
    public FluidPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static FluidPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new FluidPipeBlockEntityImpl(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var handler = getFluidHandler(side);
            if (handler != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> FluidTransferHelperImpl.toFluidHandler(handler)));
            }
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> cableBlockEntityBlockEntityType) {
    }
}
