package com.lowdragmc.gtceu.api.machine;

import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IRPCBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Information about TileEntities.
 * <p/>
 */
public interface IMetaMachineBlockEntity extends IAsyncAutoSyncBlockEntity, IRPCBlockEntity, IAutoPersistBlockEntity {

    Level level();

    BlockPos pos();

    void notifyBlockUpdate();

    void scheduleRenderUpdate();

    MetaMachine getMetaMachine();

    long getOffsetTimer();
    long getOffset();

    MultiManagedStorage getRootStorage();

    MachineDefinition getDefinition();

}
