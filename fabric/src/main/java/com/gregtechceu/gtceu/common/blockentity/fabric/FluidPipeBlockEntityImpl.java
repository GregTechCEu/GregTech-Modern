package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidPipeBlockEntityImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlockEntityImpl extends FluidPipeBlockEntity{

    public FluidPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static FluidPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new FluidPipeBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> type) {
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            final var fluidTransfer = blockEntity.getFluidHandler(direction);
            return fluidTransfer == null ? null : new Storage<FluidVariant>() {

                @Override
                public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                    return fluidTransfer.fill(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt()), false);
                }

                @Override
                public long simulateInsert(FluidVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
                    return fluidTransfer.fill(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt()), true);
                }

                @Override
                public boolean supportsExtraction() {
                    return false;
                }

                @Override
                public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                    return 0;
                }

                @Override
                public Iterator<StorageView<FluidVariant>> iterator() {
                    Collection<StorageView<FluidVariant>> list = List.of(new StorageView<>() {
                        @Override
                        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                            return 0;
                        }

                        @Override
                        public boolean isResourceBlank() {
                            return true;
                        }

                        @Override
                        public FluidVariant getResource() {
                            return FluidVariant.blank();
                        }

                        @Override
                        public long getAmount() {
                            return 0;
                        }

                        @Override
                        public long getCapacity() {
                            return 0;
                        }
                    });
                    return list.iterator();
                }
            };
        }, type);
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity, type);
    }
}
