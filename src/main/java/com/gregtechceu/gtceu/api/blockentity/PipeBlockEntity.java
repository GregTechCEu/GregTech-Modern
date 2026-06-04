package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pipenet.*;
import com.gregtechceu.gtceu.api.sync_system.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PipeBlockEntity<PipeType extends Enum<PipeType> & IPipeVariant<NodeDataType>, NodeDataType>
                                     extends ManagedSyncBlockEntity
                                     implements ITickSubscription, IPaintable, IGregtechBlockEntity, IToolGridHighlight,
                                     ICopyable {

    public static final int ALL_OPENED = 0b111111;
    public static final int ALL_CLOSED = 0b000000;

    private final long offset = GTValues.RNG.nextInt(20);

    @Getter
    @SyncToClient
    @SaveField(nbtKey = "cover")
    protected final PipeCoverContainer coverContainer;

    @Getter
    @SyncToClient
    @SaveField
    @RerenderOnChanged
    protected int connections = ALL_CLOSED;
    @SyncToClient
    @SaveField
    @RerenderOnChanged
    private int blockedConnections = ALL_CLOSED;

    @Getter
    private NodeDataType nodeData;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    @Getter
    private int paintingColor = -1;

    @RerenderOnChanged
    @SyncToClient
    @SaveField
    private Material frameMaterial = GTMaterials.NULL;
    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.coverContainer = new PipeCoverContainer(this);
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
        this.nodeData = getPipeBlock().createProperties();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    public long getOffsetTimer() {
        return level == null ? offset : (level.getServer().getTickCount() + offset);
    }

    @Override
    public void onLoad() {
        coverContainer.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, () -> {
                // Initialise pipenet on first tick

                //getPipeBlock().getWorldPipeNet(serverLevel).addNode(getBlockPos(), getNodeData(), activeConnections, isActiveNode);
            }));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        coverContainer.onUnload();

        // Remove segment from pipenet

        //if (getPipeNet() != null) getPipeNet().removeNode(getBlockPos());
    }

    public void setPaintingColor(int col) {
        paintingColor = col;
        syncDataHolder.markClientSyncFieldDirty("paintingColor");
    }

    public void setFrameMaterial(Material mat) {
        frameMaterial = mat;
        syncDataHolder.markClientSyncFieldDirty("frameMaterial");
    }

    @Override
    public @UnknownNullability Level getLevel() {
        return super.getLevel();
    }

    @Contract(pure = true)
    public Material getFrameMaterial() {
        // backwards compat
        // noinspection ConstantValue
        if (frameMaterial == null) {
            frameMaterial = GTMaterials.NULL;
        }
        return frameMaterial;
    }

    @SuppressWarnings("unchecked")
    public PipeBlock<PipeType, NodeDataType> getPipeBlock() {
        return (PipeBlock<PipeType, NodeDataType>) getBlockState().getBlock();
    }

    public PipeNetworkType getNetworkType() {
        return getPipeBlock().getNetworkType();
    }

    public int getBlockedConnections() {
        return canHaveBlockedFaces() ? blockedConnections : 0;
    }

    /**
     * If pipe is set to block connection from the specific side
     *
     * @param side face
     */
    public boolean isBlocked(Direction side) {
        return PipeBlockEntity.isFaceBlocked(getBlockedConnections(), side);
    }

    /**
     * If node is connected to the specific side
     *
     * @param side face
     */
    public boolean isConnected(Direction side) {
        return PipeBlockEntity.isConnected(getConnections(), side);
    }

    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
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
        super.serverTick();
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        for (var iter = serverTicks.iterator(); iter.hasNext();) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //////////////////////////////////////
    // ******* Pipe Connections *******//
    //////////////////////////////////////

    // if a face is blocked it will still render as connected, but it won't be able to receive stuff from that direction
    public boolean canHaveBlockedFaces() {
        return true;
    }

    /**
     * get connections for rendering and collision.
     */
    public int getVisualConnections() {
        var visualConnections = connections;
        for (var side : GTUtil.DIRECTIONS) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover != null && cover.canPipePassThrough()) {
                visualConnections = visualConnections | (1 << side.ordinal());
            }
        }
        return visualConnections;
    }

    public void setBlocked(Direction side, boolean isBlocked) {
        if (level instanceof ServerLevel serverLevel && canHaveBlockedFaces()) {

            this.blockedConnections = withSideConnection(blockedConnections, side, isBlocked);
            syncDataHolder.markClientSyncFieldDirty("blockedConnections");

            // Update routing for this segment
            // Split segment here and create a new edge?

            /*LevelPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(serverLevel);
            PipeNet<?> net = worldPipeNet.getNetFromPos(getBlockPos());
            if (net != null) {
                net.onPipeConnectionsUpdate();
            }*/
        }
    }

    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        // fix desync between two connections.
        // Can happen if a pipe side is blocked, and a new pipe is placed next to it.
        if (getLevel() == null || isRemote()) return;

        if (!getLevel().isClientSide) {
            if (isConnected(side) == connected) {
                return;
            }
            BlockEntity tile = getNeighbor(side);
            // block connections if Pipe Types do not match
            if (connected &&
                    tile instanceof PipeBlockEntity<?, ?> pipeTile &&
                    pipeTile.getPipeType().getClass() != this.getPipeType().getClass()) {
                return;
            }

            if (!connected) {
                var cover = getCoverContainer().getCoverAtSide(side);
                if (cover != null && cover.canPipePassThrough()) return;
            }

            this.connections = withSideConnection(connections, side, connected);
            syncDataHolder.markClientSyncFieldDirty("connections");

            if (!fromNeighbor && tile instanceof PipeBlockEntity<?, ?> pipe) {
                Direction oppositeSide = side.getOpposite();
                boolean neighbourOpen = pipe.isConnected(oppositeSide);
                if (isConnected(side) == neighbourOpen) {
                    return;
                }
                if (!neighbourOpen || pipe.getCoverContainer().getCoverAtSide(oppositeSide) == null) {
                    pipe.setConnection(oppositeSide, !neighbourOpen, true);
                }
            }


            // Update pipenet to add connection to new segment

            //LevelPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet((ServerLevel) getLevel());
            //worldPipeNet.updateBlockedConnections(getBlockPos(), side, !connected);

            // notify neighbor of change so Auto Output updates its ticking status
            getLevel().neighborChanged(getBlockPos().relative(side), getPipeBlock(), getBlockPos());

        }
    }

    public boolean tryConnectToAdjacent(Direction side, boolean fromNeighbor) {
        if (isConnected(side)) return true;
        if (canConnect(side)) setConnection(side, true, fromNeighbor);
        return true;
    }

    public boolean canConnect(Direction facing) {
        if (!getCoverContainer().canPipePassThrough(facing)) return false;

        BlockEntity other = getNeighbor(facing);

        if (other instanceof PipeBlockEntity<?, ?> node) {
            if (!node.getCoverContainer().canPipePassThrough(facing.getOpposite())) return false;
            return canPipesConnect(facing, (PipeBlockEntity<PipeType, NodeDataType>) other);
        }

        return canPipeConnectToBlock(facing, getLevel().getBlockState(getBlockPos().relative(facing)).getBlock(), other);
    }

    public abstract boolean canPipesConnect(Direction side,
                                            PipeBlockEntity<PipeType, NodeDataType> other);

    public abstract boolean canPipeConnectToBlock(Direction side, Block block,
                                                  @Nullable BlockEntity blockEntity);

    protected int withSideConnection(int blockedConnections, Direction side, boolean connected) {
        int index = 1 << side.ordinal();
        if (connected) {
            return blockedConnections | index;
        } else {
            return blockedConnections & ~index;
        }
    }

    public void onNeighborChanged(Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        Direction facing = GTUtil.getFacingToNeighbor(getBlockPos(), neighborPos);
        if (facing == null) return;

        if (!ConfigHolder.INSTANCE.machines.gt6StylePipesCables) {
            boolean open = isConnected(facing);
            boolean canConnect = canConnect(facing);
            if (!open && canConnect && getBlockState().getBlock() != neighborBlock)
                setConnection(facing, true, false);
            if (open && !canConnect)
                setConnection(facing, false, false);
        }

        getCoverContainer().onNeighborChanged(neighborBlock, neighborPos, isMoving);
    }

    public int getNumConnections() {
        int count = 0;
        int connections = getConnections();
        while (connections > 0) {
            count++;
            connections = connections & (connections - 1);
        }
        return count;
    }

    public PipeType getPipeType() {
        return getPipeBlock().pipeType;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        if (toolTypes.contains(getPipeBlock().getPipeTuneTool())) return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, pos, state, held, toolTypes)) return true;
        }
        return false;
    }

    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock ? GuiTextures.TOOL_PIPE_CONNECT : GuiTextures.TOOL_PIPE_BLOCK;
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(getPipeBlock().getPipeTuneTool())) {
            if (player.isShiftKeyDown() && this.canHaveBlockedFaces()) {
                return getPipeTexture(isBlocked(side));
            } else {
                return getPipeTexture(isConnected(side));
            }
        }
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, pos, state, toolTypes, side);
        }
        return null;
    }

    public Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        // the side hit from the machine grid
        var player = context.getPlayer();
        var toolType = context.getToolType();
        var gridSide = context.getGridSide();

        if (player == null) return Pair.of(null, InteractionResult.PASS);

        // Prioritize covers
        CoverBehavior cover = getCoverContainer().getCoverAtSide(context.getGridSide());
        if (cover != null) {
            var result = cover.onToolClick(context);
            if (result.getSecond() != InteractionResult.PASS) return result;

            if (toolType.contains(GTToolType.CROWBAR)) {
                getCoverContainer().removeCover(context.getGridSide(), player);
                return Pair.of(GTToolType.CROWBAR, InteractionResult.sidedSuccess(isRemote()));
            }
        }

        if (toolType.contains(getPipeBlock().getPipeTuneTool())) {
            if (player.isShiftKeyDown() && this.canHaveBlockedFaces()) {
                boolean isBlocked = this.isBlocked(gridSide);
                this.setBlocked(gridSide, !isBlocked);
            } else {
                boolean isOpen = this.isConnected(gridSide);
                this.setConnection(gridSide, !isOpen, false);
            }
            return Pair.of(getPipeBlock().getPipeTuneTool(), InteractionResult.sidedSuccess(isRemote()));
        } else if (toolType.contains(GTToolType.CROWBAR)) {
            if (!frameMaterial.isNull()) {
                Block.popResource(context.getLevel(), this.getBlockPos(),
                        GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, frameMaterial).asStack());
                frameMaterial = GTMaterials.NULL;
                return Pair.of(GTToolType.CROWBAR, InteractionResult.sidedSuccess(isRemote()));
            }
        }

        return Pair.of(null, InteractionResult.PASS);
    }

    @Override
    public int getDefaultPaintingColor() {
        return this.getPipeBlock() instanceof MaterialPipeBlock<?, ?> materialPipeBlock ?
                materialPipeBlock.material.getMaterialRGB() : 0xFFFFFF;
    }

    public static boolean isFaceBlocked(int blockedConnections, Direction side) {
        return (blockedConnections & (1 << side.ordinal())) > 0;
    }

    public static boolean isConnected(int connections, Direction side) {
        return (connections & (1 << side.ordinal())) > 0;
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        return ICopyable.super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        ICopyable.super.pasteConfig(player, tag);
    }

    @Override
    public List<ItemStack> getItemsRequiredToPaste() {
        return coverContainer.getItemsRequiredToPaste();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(GTModelProperties.LEVEL, getLevel())
                .with(GTModelProperties.POS, getBlockPos())
                .with(GTModelProperties.PIPE_CONNECTION_MASK, getVisualConnections())
                .with(GTModelProperties.PIPE_BLOCKED_MASK, getBlockedConnections())
                .build();
    }
}
