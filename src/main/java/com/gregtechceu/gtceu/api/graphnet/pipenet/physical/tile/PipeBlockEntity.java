package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.NeighborCacheBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicRegistry;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IInsulatable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.client.particle.GTOverheatParticle;
import com.gregtechceu.gtceu.client.particle.GTParticleManager;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererPackage;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gregtechceu.gtceu.utils.TaskScheduler;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PipeBlockEntity extends NeighborCacheBlockEntity
                             implements IWorldPipeNetTile, ITickSubscription, IEnhancedManaged,
                             IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity, IToolGridHighLight, IToolable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PipeBlockEntity.class);
    @Getter
    public final IManagedStorage syncStorage = new FieldManagedStorage(this);

    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    public static final int UPDATE_PIPE_LOGIC = 0;

    private final Int2ObjectOpenHashMap<NetLogicData> netLogicDatas = new Int2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<NetLogicData.LogicDataListener> listeners = new ObjectOpenHashSet<>();

    // information that is only required for determining graph topology should be stored on the tile entity level,
    // while information interacted with during graph traversal should be stored on the NetLogicData level.

    @Persisted
    @DescSynced
    @Getter
    private byte connectionMask;
    @Persisted
    @DescSynced
    @Getter
    private byte renderMask;
    @Persisted
    @DescSynced
    @Getter
    private byte blockedMask;
    @Persisted
    @DescSynced
    private int paintingColor = -1;

    @Getter
    @DescSynced
    private @Nullable Material frameMaterial;

    private final List<TickableSubscription> serverTicks = new ArrayList<>();
    private final List<TickableSubscription> waitingToAdd = new ArrayList<>();

    @Persisted
    @DescSynced
    @Getter
    protected final PipeCoverHolder coverHolder = new PipeCoverHolder(this);
    private final Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> capabilities = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenCustomHashMap<WorldPipeNetNode, PipeCapabilityWrapper> netCapabilities = WorldPipeNet
            .getSensitiveHashMap();

    @Getter
    @Nullable
    private TemperatureLogic temperatureLogic;
    @OnlyIn(Dist.CLIENT)
    @Nullable
    private GTOverheatParticle overheatParticle;

    private final int offset = (int) (Math.random() * 20);

    private long nextDamageTime = 0;
    private long nextSoundTime = 0;

    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, true);
    }

    @Nullable
    public PipeBlockEntity getPipeNeighbor(Direction facing, boolean allowChunkloading) {
        BlockEntity tile = allowChunkloading ? getNeighbor(facing) : getNeighborNoChunkloading(facing);
        if (tile instanceof PipeBlockEntity pipe) return pipe;
        else return null;
    }

    public void getDrops(@NotNull List<ItemStack> drops, @NotNull BlockState state) {
        if (getFrameMaterial() != null)
            drops.add(GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, getFrameMaterial()).asStack());
    }

    @Override
    public void scheduleRenderUpdate() {
        super.scheduleRenderUpdate();
        requestModelDataUpdate();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // TODO I hate this so much can someone please make it so that covers go through getDrops()?
        getCoverHolder().dropAllCovers();
    }

    public ItemStack getMainDrop(@NotNull BlockState state) {
        return new ItemStack(state.getBlock(), 1);
    }

    public ItemStack getDrop() {
        return new ItemStack(getBlockType(), 1);
    }

    public long getOffsetTimer() {
        return Platform.getMinecraftServer().getTickCount() + offset;
    }

    public void placedBy(ItemStack stack, Player player) {}

    public IPipeStructure getStructure() {
        return getBlockType().getStructure();
    }

    // mask //

    public boolean canConnectTo(Direction facing) {
        return this.getStructure().canConnectTo(facing, connectionMask);
    }

    public void setConnected(Direction facing, boolean renderClosed) {
        this.connectionMask |= 1 << facing.ordinal();
        updateActiveStatus(facing, false);
        if (renderClosed) {
            this.renderMask |= 1 << facing.ordinal();
        } else {
            this.renderMask &= ~(1 << facing.ordinal());
        }
        scheduleRenderUpdate();
    }

    public void setDisconnected(Direction facing) {
        this.connectionMask &= ~(1 << facing.ordinal());
        this.renderMask &= ~(1 << facing.ordinal());
        updateActiveStatus(facing, false);
        scheduleRenderUpdate();
    }

    public boolean isConnected(Direction side) {
        return (this.connectionMask & 1 << side.ordinal()) > 0;
    }

    public boolean isConnectedCoverAdjusted(Direction facing) {
        CoverBehavior cover;
        return ((this.connectionMask & 1 << facing.ordinal()) > 0) ||
                (cover = getCoverHolder().getCoverAtSide(facing)) != null && cover.forcePipeRenderConnection();
    }

    public boolean renderClosed(Direction facing) {
        return (this.renderMask & 1 << facing.ordinal()) > 0;
    }

    public void setBlocked(Direction facing) {
        this.blockedMask |= (byte) (1 << facing.ordinal());
        scheduleRenderUpdate();
    }

    public void setUnblocked(Direction facing) {
        this.blockedMask &= (byte) ~(1 << facing.ordinal());
        scheduleRenderUpdate();
    }

    public boolean isBlocked(Direction facing) {
        return (this.blockedMask & 1 << facing.ordinal()) > 0;
    }

    public void setFrameMaterial(@Nullable Material frameMaterial) {
        this.frameMaterial = frameMaterial;
        scheduleRenderUpdate();
    }

    // paint //

    public int getPaintingColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    public void setPaintingColor(int paintingColor, boolean alphaSensitive) {
        if (!alphaSensitive) {
            paintingColor |= 0xFF000000;
        }
        this.paintingColor = paintingColor;
        scheduleRenderUpdate();
    }

    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    public int getDefaultPaintingColor() {
        return DEFAULT_COLOR;
    }

    // ticking //

    @Override
    public @Nullable TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isClientSide()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(BlockProperties.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(BlockProperties.SERVER_TICK, true);
                    setBlockState(blockState);
                    serverLevel.getServer().tell(new TickTask(0, () -> serverLevel.setBlockAndUpdate(getBlockPos(),
                            getBlockState().setValue(BlockProperties.SERVER_TICK, true))));
                }
            }
            return subscription;
        }
        return null;
    }

    @Override
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
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !this.isRemoved()) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, false));
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::initialize));
        }
    }

    // activeness //

    @Override
    public void onNeighborChanged(Block fromBlock, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(fromBlock, fromPos, isMoving);
        Direction facing = GTUtil.getFacingToNeighbor(this.getBlockPos(), fromPos);
        coverHolder.onNeighborChanged(fromBlock, fromPos, isMoving);
        updateActiveStatus(facing, false);
    }

    /**
     * Returns a map of facings to tile entities that should have at least one of the required capabilities.
     * 
     * @param node the node for this tile entity. Used to identify the capabilities to match.
     * @return a map of facings to tile entities.
     */
    public @NotNull EnumMap<Direction, BlockEntity> getTargetsWithCapabilities(WorldPipeNetNode node) {
        PipeCapabilityWrapper wrapper = netCapabilities.get(node);
        EnumMap<Direction, BlockEntity> caps = new EnumMap<>(Direction.class);
        if (wrapper == null) return caps;

        for (Direction facing : GTUtil.DIRECTIONS) {
            if (wrapper.isActive(facing)) {
                BlockEntity tile = getNeighbor(facing);
                if (tile == null) updateActiveStatus(facing, false);
                else caps.put(facing, tile);
            }
        }
        return caps;
    }

    public @Nullable BlockEntity getTargetWithCapabilities(WorldPipeNetNode node, Direction facing) {
        PipeCapabilityWrapper wrapper = netCapabilities.get(node);
        if (wrapper == null || !wrapper.isActive(facing)) return null;
        else return getNeighbor(facing);
    }

    @Override
    public PipeCapabilityWrapper getWrapperForNode(WorldPipeNetNode node) {
        return netCapabilities.get(node);
    }

    /**
     * Updates the pipe's active status based on the tile entity connected to the side.
     * 
     * @param facing            the side to check. Can be null, in which case all sides will be checked.
     * @param canOpenConnection whether the pipe is allowed to open a new connection if it finds a tile it can connect
     *                          to.
     */
    public void updateActiveStatus(@Nullable Direction facing, boolean canOpenConnection) {
        if (facing == null) {
            for (Direction side : GTUtil.DIRECTIONS) {
                updateActiveStatus(side, canOpenConnection);
            }
            return;
        }
        if (!this.isConnectedCoverAdjusted(facing) && !(canOpenConnection && canConnectTo(facing))) {
            setAllIdle(facing);
            return;
        }

        BlockEntity tile = getNeighbor(facing);
        if (tile == null || tile instanceof PipeBlockEntity) {
            setAllIdle(facing);
            return;
        }

        boolean oneActive = false;
        for (var netCapability : netCapabilities.entrySet()) {
            for (Capability<?> cap : netCapability.getValue().capabilities) {
                if (tile.getCapability(cap, facing.getOpposite()).isPresent()) {
                    oneActive = true;
                    netCapability.getValue().setActive(facing);
                    break;
                }
            }
        }
        if (canOpenConnection && oneActive) this.setConnected(facing, false);
    }

    private void setAllIdle(Direction facing) {
        for (var netCapability : netCapabilities.entrySet()) {
            netCapability.getValue().setIdle(facing);
        }
    }

    // capability //

    private void addCapabilities(IPipeCapabilityObject[] capabilities) {
        for (IPipeCapabilityObject capabilityObject : capabilities) {
            capabilityObject.setTile(this);
            for (Capability<?> capability : capabilityObject.getCapabilities()) {
                this.capabilities.put(capability, capabilityObject);
            }
        }
    }

    public <T> LazyOptional<T> getCapabilityCoverQuery(@NotNull Capability<T> capability, @Nullable Direction facing) {
        // covers have access to the capability objects no matter the connection status
        IPipeCapabilityObject object = capabilities.get(capability);
        return object == null ? null : object.getCapabilityForSide(capability, facing);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(capability, LazyOptional.of(this::getCoverHolder));
        }
        LazyOptional<T> pipeCapability;
        IPipeCapabilityObject object = capabilities.get(capability);
        if (object == null || (pipeCapability = object.getCapabilityForSide(capability, facing)) == null)
            pipeCapability = super.getCapability(capability, facing);

        CoverBehavior cover = facing == null ? null : getCoverHolder().getCoverAtSide(facing);
        if (cover == null) {
            if (facing == null || isConnected(facing)) {
                return pipeCapability;
            }
            return super.getCapability(capability, facing);
        }

        LazyOptional<T> coverCapability = cover.getCapability(capability, pipeCapability);
        if (coverCapability == pipeCapability) {
            if (isConnectedCoverAdjusted(facing)) {
                return pipeCapability;
            }
            return super.getCapability(capability, facing);
        }
        return coverCapability;
    }

    // data sync management //

    public NetLogicData getNetLogicData(int networkID) {
        return netLogicDatas.get(networkID);
    }

    public @NotNull PipeBlock getBlockType() {
        return (PipeBlock) this.getBlockState().getBlock();
    }

    @Override
    public void setLevel(@NotNull Level level) {
        if (level == this.getLevel()) return;
        super.setLevel(level);
    }

    protected void initialize() {
        if (!getLevel().isClientSide) {
            this.netLogicDatas.clear();
            this.capabilities.clear();
            this.netCapabilities.clear();
            this.listeners.forEach(NetLogicData.LogicDataListener::invalidate);
            this.listeners.clear();
            boolean firstNode = true;
            for (WorldPipeNetNode node : PipeBlock.getNodesForTile(this)) {
                this.addCapabilities(node.getNet().getNewCapabilityObjects(node));
                this.netCapabilities.put(node, new PipeCapabilityWrapper(this, node));
                int networkID = node.getNet().getNetworkID();
                netLogicDatas.put(networkID, node.getData());
                var listener = node.getData().createListener(
                        (e, r, f) -> writeCustomData(UPDATE_PIPE_LOGIC, buf -> {
                            buf.writeVarInt(networkID);
                            buf.writeUtf(e.getSerializedName());
                            buf.writeBoolean(r);
                            buf.writeBoolean(f);
                            if (!r) {
                                e.encode(buf, f);
                            }
                        }));
                this.listeners.add(listener);
                node.getData().addListener(listener);
                // Manually resync the data, as it's loaded & the listeners are queried for the first time *before*
                // we call `addListener` on the line above.
                for (var entry : node.getData().getEntries()) {
                    node.getData().markLogicEntryAsUpdated(entry, true);
                }
                if (firstNode) {
                    firstNode = false;
                    this.temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
                }
            }
            this.netLogicDatas.trim();
            this.listeners.trim();
            this.capabilities.trim();
            this.netCapabilities.trim();
            updateActiveStatus(null, false);
        }
    }

    /*
    @Override
    public void writeInitialSyncData(@NotNull FriendlyByteBuf buf) {
        buf.writeVarInt(netLogicDatas.size());
        for (var entry : netLogicDatas.int2ObjectEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            entry.getValue().encode(buf);
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull FriendlyByteBuf buf) {
        if (level.isClientSide) {
            netLogicDatas.clear();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int networkID = buf.readVarInt();
                NetLogicData data = new NetLogicData();
                data.decode(buf);
                netLogicDatas.put(networkID, data);
            }
        }
        scheduleRenderUpdate();
    }
    */

    @Override
    public void receiveCustomData(int discriminator, @NotNull FriendlyByteBuf buf) {
        if (discriminator == UPDATE_PIPE_LOGIC) {
            // extra check just to make sure we don't affect actual net data with our writes
            if (level.isClientSide) {
                int networkID = buf.readVarInt();
                String identifier = buf.readUtf(255);
                boolean removed = buf.readBoolean();
                boolean fullChange = buf.readBoolean();
                if (removed) {
                    this.netLogicDatas.computeIfPresent(networkID, (k, v) -> v.removeLogicEntry(identifier));
                } else {
                    if (fullChange) {
                        NetLogicEntry<?, ?> logic = NetLogicRegistry.getSupplierErroring(identifier).get();
                        logic.decode(buf, true);
                        this.netLogicDatas.compute(networkID, (k, v) -> {
                            if (v == null) v = new NetLogicData();
                            v.setLogicEntry(logic);
                            return v;
                        });
                    } else {
                        NetLogicData data = this.netLogicDatas.get(networkID);
                        if (data != null) {
                            NetLogicEntry<?, ?> entry = data.getLogicEntryNullable(identifier);
                            if (entry != null) entry.decode(buf, false);
                            data.markLogicEntryAsUpdated(entry, false);
                        } else return;
                    }
                    if (identifier.equals(TemperatureLogic.INSTANCE.getSerializedName())) {
                        TemperatureLogic tempLogic = this.netLogicDatas.get(networkID)
                                .getLogicEntryNullable(TemperatureLogic.INSTANCE);
                        if (tempLogic != null) updateTemperatureLogic(tempLogic);
                    }
                }
            }
        }
    }

    // particle //

    public void updateTemperatureLogic(@NotNull TemperatureLogic logic) {
        if (overheatParticle == null || !overheatParticle.isAlive()) {
            long tick = Platform.getMinecraftServer().getTickCount();
            int temp = logic.getTemperature(tick);
            if (temp > GTOverheatParticle.TEMPERATURE_CUTOFF) {
                IPipeStructure structure = this.getStructure();
                overheatParticle = new GTOverheatParticle(this, logic, structure.getPipeBoxes(this),
                        structure instanceof IInsulatable i && i.isInsulated());
                GTParticleManager.INSTANCE.addEffect(overheatParticle);
            }
        } else {
            overheatParticle.setTemperatureLogic(logic);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void killOverheatParticle() {
        if (overheatParticle != null) {
            overheatParticle.setExpired();
            overheatParticle = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isOverheatParticleAlive() {
        return overheatParticle != null && overheatParticle.isAlive();
    }

    public void spawnParticles(Direction direction, ParticleOptions particleType, int particleCount) {
        if (getLevel() instanceof ServerLevel server) {
            server.sendParticles(particleType,
                    getBlockPos().getX() + 0.5,
                    getBlockPos().getY() + 0.5,
                    getBlockPos().getZ() + 0.5,
                    particleCount,
                    direction.getStepX() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getStepY() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getStepZ() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    0.1);
        }
    }

    // misc overrides //

    public void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getBlockPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    @Override
    @SuppressWarnings("ConstantConditions") // yes this CAN actually be null
    public void markAsDirty() {
        if (hasLevel()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_KNOWN_SHAPE);
        }
        // this most notably gets called when the covers of a pipe get updated, aka the edge predicates need syncing.
        for (var node : this.netCapabilities.keySet()) {
            node.getNet().updatePredication(node, this);
        }
        this.setChanged();
    }

    public static @Nullable PipeBlockEntity getTileNoLoading(BlockPos pos, ResourceKey<Level> dimension) {
        Level world = Platform.getMinecraftServer().getLevel(dimension);
        if (world == null || !world.isLoaded(pos)) return null;

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof PipeBlockEntity pipe) return pipe;
        else return null;
    }

    @Override
    public @NotNull ModelData getModelData() {
        byte frameMask = 0;
        for (Direction facing : GTUtil.DIRECTIONS) {
            CoverBehavior cover = getCoverHolder().getCoverAtSide(facing);
            if (cover != null) {
                frameMask |= (byte) (1 << facing.ordinal());
                if (cover.forcePipeRenderConnection()) this.connectionMask |= (byte) (1 << facing.ordinal());
            }
        }
        return ModelData.builder()
                .with(AbstractPipeModel.THICKNESS_PROPERTY, this.getStructure().getRenderThickness())
                .with(AbstractPipeModel.CONNECTED_MASK_PROPERTY, connectionMask)
                .with(AbstractPipeModel.CLOSED_MASK_PROPERTY, renderMask)
                .with(AbstractPipeModel.BLOCKED_MASK_PROPERTY, blockedMask)
                .with(AbstractPipeModel.COLOR_PROPERTY, getPaintingColor())
                .with(AbstractPipeModel.FRAME_MATERIAL_PROPERTY, frameMaterial)
                .with(AbstractPipeModel.FRAME_MASK_PROPERTY, frameMask)
                .with(CoverRendererPackage.PROPERTY, getCoverHolder().createPackage())
                .build();
    }

    public void getCoverBoxes(Consumer<VoxelShape> consumer) {
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (getCoverHolder().hasCover(facing)) {
                consumer.accept(Shapes.create(CoverRendererBuilder.PLATE_AABBS.get(facing)));
            }
        }
    }

    @Override
    public void dealAreaDamage(int size, Consumer<LivingEntity> damageFunction) {
        long timer = getOffsetTimer();
        if (timer >= this.nextDamageTime) {
            List<LivingEntity> entities = getLevel().getEntitiesOfClass(LivingEntity.class,
                    new AABB(getBlockPos()).inflate(size));
            entities.forEach(damageFunction);
            this.nextDamageTime = timer + 20;
        }
    }

    public void playLossSound() {
        long timer = getOffsetTimer();
        if (timer >= this.nextSoundTime) {
            getLevel().playSound(null, getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.nextSoundTime = timer + 20;
        }
    }

    public void visuallyExplode() {
        getLevel().explode(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5,
                1.0f + GTValues.RNG.nextFloat(), Level.ExplosionInteraction.BLOCK);
    }

    public void setNeighborsToFire() {
        for (Direction side : GTUtil.DIRECTIONS) {
            if (!GTValues.RNG.nextBoolean()) continue;
            BlockPos blockPos = getBlockPos().relative(side);
            BlockState blockState = getLevel().getBlockState(blockPos);
            if (level.isEmptyBlock(blockPos) ||
                    blockState.isFlammable(getLevel(), blockPos, side.getOpposite())) {
                getLevel().setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
            }
        }
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        if (toolTypes.contains(getBlockType().getToolClass()) || toolTypes.contains(GTToolType.SCREWDRIVER))
            return true;
        for (CoverBehavior cover : coverHolder.getCovers()) {
            if (cover.shouldRenderGrid(player, pos, state, held, toolTypes)) return true;
        }
        return false;
    }

    public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                    Direction side) {
        if (toolTypes.contains(getBlockType().getToolClass())) {
            if (player.isShiftKeyDown() && this.getBlockType().allowsBlocking()) {
                return getStructure().getPipeTexture(isBlocked(side));
            } else {
                return getStructure().getPipeTexture(isConnected(side));
            }
        }
        var cover = coverHolder.getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, pos, state, toolTypes, side);
        }
        return null;
    }

    @Override
    public Pair<@Nullable GTToolType, InteractionResult> onToolClick(@NotNull Set<GTToolType> toolTypes,
                                                                     ItemStack itemStack, UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return Pair.of(null, InteractionResult.PASS);

        var hand = context.getHand();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSide == null ? null : coverHolder.getCoverAtSide(gridSide);
        if (gridSide == null) gridSide = hitResult.getDirection();

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SCREWDRIVER, coverBehavior.onScrewdriverClick(playerIn, hand, hitResult));
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SOFT_MALLET, coverBehavior.onSoftMalletClick(playerIn, hand, hitResult));
            }
        } else if (toolTypes.contains(this.getBlockType().getToolClass())) {
            if (playerIn.isShiftKeyDown() && this.getBlockType().allowsBlocking()) {
                boolean isBlocked = this.isBlocked(gridSide);
                if (isBlocked) {
                    PipeBlock.unblockTile(this, this.getPipeNeighbor(gridSide, true), gridSide);
                } else {
                    PipeBlock.blockTile(this, this.getPipeNeighbor(gridSide, true), gridSide);
                }
            } else {
                boolean isOpen = this.isConnected(gridSide);
                if (isOpen) {
                    PipeBlock.disconnectTile(this, this.getPipeNeighbor(gridSide, true), gridSide);
                } else {
                    PipeBlock.connectTile(this, this.getPipeNeighbor(gridSide, true), gridSide);
                }
            }
            playerIn.swing(hand);
            return Pair.of(this.getBlockType().getToolClass(), InteractionResult.CONSUME);
        } else if (toolTypes.contains(GTToolType.CROWBAR)) {
            if (coverBehavior != null) {
                if (isServerSide()) {
                    coverHolder.removeCover(gridSide, playerIn);
                    playerIn.swing(hand);
                    return Pair.of(GTToolType.CROWBAR, InteractionResult.CONSUME);
                }
            } else {
                if (frameMaterial != null) {
                    Block.popResource(getLevel(), getBlockPos(),
                            GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, frameMaterial).asStack());
                    frameMaterial = null;
                    playerIn.swing(hand);
                    return Pair.of(GTToolType.CROWBAR, InteractionResult.CONSUME);
                }
            }
        }

        return Pair.of(null, InteractionResult.PASS);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        this.markAsDirty();
    }

    @Override
    public IManagedStorage getRootStorage() {
        return syncStorage;
    }
}
