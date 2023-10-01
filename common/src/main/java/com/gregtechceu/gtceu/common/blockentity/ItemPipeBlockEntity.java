package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.pipelike.item.ItemNetHandler;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeData;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeNet;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ItemPipeBlockEntity extends PipeBlockEntity<ItemPipeType, ItemPipeData> {
    protected WeakReference<ItemPipeNet> currentItemPipeNet = new WeakReference<>(null);

    @Getter
    protected final EnumMap<Direction, ItemNetHandler> handlers = new EnumMap<>(Direction.class);
    @Getter
    private final Map<FacingPos, Integer> transferred = new HashMap<>();
    @Getter
    protected ItemNetHandler defaultHandler;

    TickableSubscription serverTick;

    private int transferredItems = 0;
    private long timer = 0;

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.serverTick = subscribeServerTick(this::update);
    }

    @ExpectPlatform
    public static ItemPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onBlockEntityRegister(BlockEntityType<ItemPipeBlockEntity> cableBlockEntityBlockEntityType) {
        throw new AssertionError();
    }

    public void initHandlers() {
        ItemPipeNet net = getItemPipeNet();
        if (net == null) {
            return;
        }
        for (Direction facing : Direction.values()) {
            handlers.put(facing, new ItemNetHandler(net, this, facing));
        }
        defaultHandler = new ItemNetHandler(net, this, null);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            ItemPipeNet current = getItemPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (ItemNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof ItemPipeBlockEntity) {
                return false;
            }
            return ItemTransferHelper.getItemTransfer(level, getBlockPos().relative(side), side.getOpposite()) != null;
        }
        return false;
    }

    @Nullable
    public ItemPipeNet getItemPipeNet() {
        if (level instanceof ServerLevel serverLevel && getBlockState().getBlock() instanceof ItemPipeBlock fluidPipeBlock) {
            ItemPipeNet currentFluidPipeNet = this.currentItemPipeNet.get();
            if (currentFluidPipeNet != null && currentFluidPipeNet.isValid() && currentFluidPipeNet.containsNode(getBlockPos()))
                return currentFluidPipeNet; //return current net if it is still valid
            currentFluidPipeNet = fluidPipeBlock.getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
            if (currentFluidPipeNet != null) {
                this.currentItemPipeNet = new WeakReference<>(currentFluidPipeNet);
            }
        }
        return this.currentItemPipeNet.get();
    }

    public void update() {
        if (++timer % 20 == 0) {
            transferredItems = 0;
        }
    }

    public void transferItems(int amount) {
        transferredItems += amount;
    }

    public int getTransferredItems() {
        return transferredItems;
    }

    public void resetTransferred() {
        transferred.clear();
    }
}
