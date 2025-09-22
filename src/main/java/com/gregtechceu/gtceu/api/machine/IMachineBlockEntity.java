package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IRPCBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import org.jetbrains.annotations.NotNull;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Information about TileEntities.
 */
public interface IMachineBlockEntity extends IToolGridHighlight, IAsyncAutoSyncBlockEntity, IRPCBlockEntity,
                                     IAutoPersistBlockEntity, IPaintable, IForgeBlockEntity {

    ModelProperty<BlockAndTintGetter> MODEL_DATA_LEVEL = new ModelProperty<>();
    ModelProperty<BlockPos> MODEL_DATA_POS = new ModelProperty<>();

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Level level() {
        return self().getLevel();
    }

    default BlockPos pos() {
        return self().getBlockPos();
    }

    default void notifyBlockUpdate() {
        if (level() != null) {
            level().updateNeighborsAt(pos(), level().getBlockState(pos()).getBlock());
        }
    }

    default void scheduleRenderUpdate() {
        var pos = pos();
        if (level() != null) {
            var state = level().getBlockState(pos);
            if (level().isClientSide) {
                level().sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                self().requestModelDataUpdate();
            } else {
                level().blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    default @NotNull ModelData getModelData() {
        ModelData.Builder data = IForgeBlockEntity.super.getModelData().derive();
        getMetaMachine().updateModelData(data);
        return data.build();
    }

    default long getOffsetTimer() {
        if (level() == null) return getOffset();
        else if (level().isClientSide()) return GTValues.CLIENT_TIME + getOffset();

        var server = level().getServer();
        if (server != null) return server.getTickCount() + getOffset();
        return getOffset();
    }

    default MachineDefinition getDefinition() {
        if (self().getBlockState().getBlock() instanceof IMachineBlock machineBlock) {
            return machineBlock.getDefinition();
        } else {
            throw new IllegalStateException("MetaMachineBlockEntity is created for an un available block: " +
                    self().getBlockState().getBlock());
        }
    }

    MachineRenderState getRenderState();

    void setRenderState(MachineRenderState state);

    MetaMachine getMetaMachine();

    long getOffset();

    MultiManagedStorage getRootStorage();

    @Override
    default void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        IAutoPersistBlockEntity.super.saveCustomPersistedData(tag, forDrop);
        getMetaMachine().saveCustomPersistedData(tag, forDrop);
    }

    @Override
    default void loadCustomPersistedData(CompoundTag tag) {
        IAutoPersistBlockEntity.super.loadCustomPersistedData(tag);
        getMetaMachine().loadCustomPersistedData(tag);
    }

    @Override
    default int getPaintingColor() {
        return getMetaMachine().getPaintingColor();
    }

    @Override
    default void setPaintingColor(int color) {
        getMetaMachine().setPaintingColor(color);
    }

    @Override
    default int getDefaultPaintingColor() {
        return getMetaMachine().getDefaultPaintingColor();
    }
}
