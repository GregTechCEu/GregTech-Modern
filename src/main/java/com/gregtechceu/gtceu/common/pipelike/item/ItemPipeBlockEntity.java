package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;

import java.util.Objects;

public class ItemPipeBlockEntity extends PipeBlockEntity<ItemPipeType> {

    @Getter
    private final Object2IntMap<FacingPos> transferred = new Object2IntOpenHashMap<>();

    private int transferredItems = 0;
    private long timer = 0;

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, GTPipeNetworks.ITEM, pos, blockState);
    }

    public static ItemPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new ItemPipeBlockEntity(type, pos, blockState);
    }

    public long getLevelTime() {
        return hasLevel() ? Objects.requireNonNull(getLevel()).getGameTime() : 0L;
    }

    public static void onBlockEntityRegister(BlockEntityType<ItemPipeBlockEntity> itemPipeBlockEntityBlockEntityType) {}

    public boolean canAttachTo(Direction side) {
        if (level == null) return false;
        if (level.getBlockEntity(getBlockPos().relative(side)) instanceof ItemPipeBlockEntity) {
            return false;
        }
        return GTTransferUtils.hasAdjacentItemHandler(level, getBlockPos(), side);
    }

    public void resetTransferred() {
        transferred.clear();
    }

    /**
     * every time the transferred variable is accessed this method should be called
     * if 20 ticks passed since the last access it will reset it
     * this method is equal to
     * 
     * @code {
     *       if (++time % 20 == 0) {
     *       this.transferredItems = 0;
     *       }
     *       }
     *       <p/>
     *       if it was in a ticking TileEntity
     */
    private void updateTransferredState() {
        long currentTime = getLevelTime();
        long dif = currentTime - this.timer;
        if (dif >= 20 || dif < 0) {
            this.transferredItems = 0;
            this.timer = currentTime;
        }
    }

    public void addTransferredItems(int amount) {
        updateTransferredState();
        this.transferredItems += amount;
    }

    public int getTransferredItems() {
        updateTransferredState();
        return this.transferredItems;
    }
}
