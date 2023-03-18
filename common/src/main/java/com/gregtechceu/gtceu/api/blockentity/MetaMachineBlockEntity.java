package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachineBlockEntity
 */
public class MetaMachineBlockEntity extends BlockEntity implements IMetaMachineBlockEntity, IToolGridHighLight {
    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    @Getter
    public final MetaMachine metaMachine;
    private final long offset = GTValues.RNG.nextInt(20);

    public MetaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.metaMachine = getDefinition().createMetaMachine(this);
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    public MachineDefinition getDefinition() {
        if (getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.definition;
        } else {
            throw new IllegalStateException("MetaMachineBlockEntity is created for an un available block: " + getBlockState().getBlock());
        }
    }

    @Override
    public Level level() {
        return getLevel();
    }

    @Override
    public BlockPos pos() {
        return getBlockPos();
    }

    @Override
    public void notifyBlockUpdate() {
        if (level != null) {
            level.updateNeighborsAt(getBlockPos(), level.getBlockState(getBlockPos()).getBlock());
        }
    }

    @Override
    public void scheduleRenderUpdate() {
        var pos = pos();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, 1 << 3);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    @Override
    public long getOffsetTimer() {
        return level == null ? offset : (level.getGameTime() + offset);
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        metaMachine.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        metaMachine.onLoad();
    }

    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, GTToolType toolType) {
        return toolType == GTToolType.WRENCH || toolType == GTToolType.SCREWDRIVER;
    }

    @Override
    public boolean isSideUsed(Player player, GTToolType toolType, Direction side) {
        return metaMachine.isSideUsed(player, toolType, side);
    }
}
