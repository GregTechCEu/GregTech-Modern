package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ActivablePipeBlockEntity extends PipeBlockEntity implements IActivable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ActivablePipeBlockEntity.class, PipeBlockEntity.MANAGED_FIELD_HOLDER);

    @DescSynced
    @Getter
    @Setter
    private boolean active;

    public ActivablePipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // do not save activeness to nbt, it should go away on world save & load.
}
