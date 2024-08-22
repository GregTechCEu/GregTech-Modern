package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class PipeActivableBlockEntity extends PipeBlockEntity implements IActivable {

    private static final int PIPE_ACTIVE = 2;

    @Getter
    private boolean active;

    public PipeActivableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            writeCustomData(PIPE_ACTIVE, buf -> buf.writeBoolean(active));
            markDirty();
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull FriendlyByteBuf buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == PIPE_ACTIVE) {
            boolean active = buf.readBoolean();
            if (this.active != active) {
                this.active = active;
                scheduleRenderUpdate();
            }
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull FriendlyByteBuf buf) {
        active = buf.readBoolean();
        super.receiveInitialSyncData(buf);
    }

    // do not save activeness to nbt, it should go away on world save & load.
}
