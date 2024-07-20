package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.pipelike.item.ItemNetHandler;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeNet;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

public class ItemPipeBlockEntity extends PipeBlockEntity<ItemPipeType, ItemPipeProperties> {

    protected WeakReference<ItemPipeNet> currentItemPipeNet = new WeakReference<>(null);

    @Getter
    private final EnumMap<Direction, ItemNetHandler> handlers = new EnumMap<>(Direction.class);
    @Getter
    private final Object2IntMap<FacingPos> transferred = new Object2IntOpenHashMap<>();
    @Getter
    private ItemNetHandler defaultHandler;
    // the ItemNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IItemTransfer clientCapability = new ItemStackTransfer(0);

    private int transferredItems = 0;
    private long timer = 0;

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static ItemPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new ItemPipeBlockEntity(type, pos, blockState);
    }

    public long getLevelTime() {
        return hasLevel() ? getLevel().getGameTime() : 0L;
    }

    public static void onBlockEntityRegister(BlockEntityType<ItemPipeBlockEntity> itemPipeBlockEntityBlockEntityType) {}

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            Level world = getLevel();
            if (world.isClientSide())
                return LazyOptional.empty();

            if (side != null && isConnected(side)) {
                ensureHandlersInitialized();
                checkNetwork();
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap,
                        LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(getHandler(side, true))));
            }
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    private void ensureHandlersInitialized() {
        if (getHandlers().isEmpty())
            initHandlers();
    }

    public void initHandlers() {
        ItemPipeNet net = getItemPipeNet();
        if (net == null) {
            return;
        }
        for (Direction facing : GTUtil.DIRECTIONS) {
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
        if (level == null) return false;
        if (level.getBlockEntity(getBlockPos().relative(side)) instanceof ItemPipeBlockEntity) {
            return false;
        }
        return ItemTransferHelper.getItemTransfer(level, getBlockPos().relative(side), side.getOpposite()) != null;
    }

    @Nullable
    public ItemPipeNet getItemPipeNet() {
        if (level instanceof ServerLevel serverLevel &&
                getBlockState().getBlock() instanceof ItemPipeBlock itemPipeBlock) {
            ItemPipeNet currentItemPipeNet = this.currentItemPipeNet.get();
            if (currentItemPipeNet != null && currentItemPipeNet.isValid() &&
                    currentItemPipeNet.containsNode(getBlockPos()))
                return currentItemPipeNet; // return current net if it is still valid
            currentItemPipeNet = itemPipeBlock.getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
            if (currentItemPipeNet != null) {
                this.currentItemPipeNet = new WeakReference<>(currentItemPipeNet);
            }
        }
        return this.currentItemPipeNet.get();
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

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.handlers.clear();
    }

    public IItemTransfer getHandler(@Nullable Direction side, boolean useCoverCapability) {
        ensureHandlersInitialized();

        ItemNetHandler handler = getHandlers().getOrDefault(side, getDefaultHandler());
        if (!useCoverCapability || side == null) return handler;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getItemTransferCap(handler) : handler;
    }
}
