package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.pipenet.PipeCoverContainer;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.syncdata.EnhancedFieldManagedStorage;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote PipeBlockEntity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PipeBlockEntity<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends IAttachData> extends BlockEntity implements IPipeNode<PipeType, NodeDataType>, IEnhancedManaged, IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity, IToolGridHighLight, IToolable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PipeBlockEntity.class);
    @Getter
    private final EnhancedFieldManagedStorage syncStorage = new EnhancedFieldManagedStorage(this);
    private final long offset = GTValues.RNG.nextInt(20);

    @Getter
    @DescSynced
    @Persisted(key = "cover")
    protected final PipeCoverContainer coverContainer;

    @Setter
    @DescSynced
    @Persisted
    @RequireRerender
    protected int connections = Node.ALL_CLOSED;

    @Persisted @DescSynced @RequireRerender
    @Getter @Setter
    private int paintingColor = -1;

    @Persisted @DescSynced @RequireRerender
    private String frameMaterial;

    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.coverContainer = new PipeCoverContainer(this);      
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    public void scheduleRenderUpdate() {
        IPipeNode.super.scheduleRenderUpdate();
    }

    @Override
    public IManagedStorage getRootStorage() {
        return syncStorage;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        setChanged();
    }


    @Override
    public long getOffsetTimer() {
        return level == null ? offset : (level.getGameTime() + offset);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        coverContainer.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        coverContainer.onLoad();
    }

    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(BlockProperties.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(BlockProperties.SERVER_TICK, true);
                    setBlockState(blockState);
                    serverLevel.getServer().tell(new TickTask(0, () -> serverLevel.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, true))));
                }
            }
            return subscription;
        }
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    public final void serverTick() {
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        var iter = serverTicks.iterator();
        while (iter.hasNext()) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty()) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, false));
        }
    }

    //////////////////////////////////////
    //*******     Pipe Status    *******//
    //////////////////////////////////////

    @Override
    public boolean isBlocked(Direction side) {
        return (connections & 1 << side.ordinal()) == 0;
    }

    @Override
    public void setBlocked(Direction side, boolean isBlocked) {
        if (level instanceof ServerLevel serverLevel) {
            if (!isBlocked) {
                connections |= 1 << side.ordinal();
            } else {
                connections &= ~(1 << side.ordinal());
            }
            getPipeBlock().getWorldPipeNet(serverLevel).updateBlockedConnections(getBlockPos(), side, isBlocked);
            updateConnections();
            notifyBlockUpdate();
        }
    }

    @Override
    public int getVisualConnections() {
        var visualConnections = connections;
        for (var side : Direction.values()) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover != null && cover.blockPipePassThrough()) {
                visualConnections = visualConnections | (1 << side.ordinal());
            }
        }
        return visualConnections;
    }


    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, GTToolType toolType) {
        if (canToolTunePipe(toolType) || toolType == GTToolType.SCREWDRIVER) return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, held, toolType)) return true;
        }
        return false;
    }

    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock? GuiTextures.TOOL_PIPE_CONNECT : GuiTextures.TOOL_PIPE_BLOCK;
    }

    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (canToolTunePipe(toolType)) {
            return getPipeTexture(isBlocked(side));
        }
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, toolType, side);
        }
        return null;
    }

    @Override
    public InteractionResult onToolClick(@NotNull GTToolType toolType, ItemStack itemStack, UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return InteractionResult.PASS;

        var hand = context.getHand();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSide == null ? null : coverContainer.getCoverAtSide(gridSide);
        if (gridSide == null) gridSide = hitResult.getDirection();

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolType == GTToolType.SCREWDRIVER) {
            if (coverBehavior != null) {
                return coverBehavior.onScrewdriverClick(playerIn, hand, hitResult);
            }
        } else if (toolType == GTToolType.SOFT_MALLET) {
            if (coverBehavior != null) {
                return coverBehavior.onSoftMalletClick(playerIn, hand, hitResult);
            }
        } else if (canToolTunePipe(toolType)) {
            setBlocked(gridSide, !isBlocked(gridSide));
            // try to connect to the next node.
            if (!isBlocked(gridSide)) {
                var node = getPipeBlock().getPileTile(getPipeLevel(), getPipePos().relative(gridSide));
                if (node != null && node.isBlocked(gridSide.getOpposite())) { // if is a pipe node
                    node.setBlocked(gridSide.getOpposite(), false);
                }
            }
            return InteractionResult.CONSUME;
        } else if (toolType == GTToolType.CROWBAR) {
            if (coverBehavior != null) {
                if (!isRemote()) {
                    getCoverContainer().removeCover(gridSide);
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    protected boolean canToolTunePipe(GTToolType toolType) {
        return toolType == GTToolType.WRENCH;
    }

    @Override
    public int getDefaultPaintingColor() {
        return this.getPipeBlock() instanceof MaterialPipeBlock<?,?,?> materialPipeBlock ? materialPipeBlock.material.getMaterialRGB() : IPipeNode.super.getDefaultPaintingColor();
    }

    @Nullable
    @Override
    public Material getFrameMaterial() {
        return frameMaterial == null ? null : GTRegistries.MATERIALS.get(frameMaterial);
    }

}
