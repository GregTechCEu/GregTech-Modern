package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeCoverHolder;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public abstract class PipeBlock extends Block implements EntityBlock, IBlockRendererProvider {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final EnumMap<Direction, BooleanProperty> FACINGS = buildFacings();

    private static @NotNull EnumMap<Direction, BooleanProperty> buildFacings() {
        EnumMap<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, NORTH);
        map.put(Direction.EAST, EAST);
        map.put(Direction.SOUTH, SOUTH);
        map.put(Direction.WEST, WEST);
        map.put(Direction.UP, UP);
        map.put(Direction.DOWN, DOWN);
        return map;
    }

    public static final BooleanProperty FRAMED = BooleanProperty.create("framed");

    // do not touch these two unless you know what you are doing
    protected final ThreadLocal<BlockPos> lastTilePos = ThreadLocal.withInitial(() -> new BlockPos(0, 0, 0));
    protected final ThreadLocal<WeakReference<PipeBlockEntity>> lastTile = ThreadLocal
            .withInitial(() -> new WeakReference<>(null));

    @Getter
    private final IPipeStructure structure;

    public PipeBlock(BlockBehaviour.Properties properties, IPipeStructure structure) {
        super(properties);
        this.structure = structure;
        /*
         * setTranslationKey(structure.getName());
         * setSoundType(SoundType.METAL);
         * setHardness(2.0f);
         * setHarvestLevel(getToolClass(), 1);
         * setResistance(3.0f);
         * setLightOpacity(1);
         * disableStats();
         */
    }

    // net logic //

    public void doPlacementLogic(PipeBlockEntity tile, Direction placedBlockSearchSide) {
        for (Direction facing : GTUtil.DIRECTIONS) {
            BlockEntity neighbor = tile.getNeighbor(facing);
            if (neighbor instanceof PipeBlockEntity other) {
                // first check -- does the other tile have a cover that would prevent connection
                CoverBehavior cover = other.getCoverHolder().getCoverAtSide(facing.getOpposite());
                if (cover != null && !cover.canPipePassThrough()) continue;
                // second check -- connect to matching mark pipes if side matches or config allows.
                if (tile.getPaintingColor() == other.getPaintingColor() && (facing == placedBlockSearchSide ||
                        !ConfigHolder.INSTANCE.machines.gt6StylePipesCables)) {
                    if (coverCheck(tile, other, facing)) connectTile(tile, other, facing);
                    continue;
                }
                // third check -- connect to pipes with an open connection, no matter the mark status.
                if (other.isConnected(facing.getOpposite())) {
                    if (coverCheck(tile, other, facing)) connectTile(tile, other, facing);
                }
            } else if (facing == placedBlockSearchSide) {
                // if the placed on tile supports one of our capabilities, connect to it.
                tile.updateActiveStatus(facing, true);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockEntity entity = level.getBlockEntity(pos);

        PipeBlockEntity pipeBlockEntity = null;
        if (entity instanceof PipeBlockEntity pbe) {
            pipeBlockEntity = pbe;
        }
        if (pipeBlockEntity == null) {
            return InteractionResult.FAIL;
        }

        if (pipeBlockEntity.getFrameMaterial() == null) {
            var frameBlock = MaterialBlock.getFrameboxFromItem(itemStack);
            if (frameBlock != null) {
                pipeBlockEntity.setFrameMaterial(frameBlock.material);
                if (!player.isCreative()) itemStack.shrink(1);
                SoundType type = VanillaRecipeHelper.isMaterialWood(frameBlock.material) ? SoundType.WOOD :
                        SoundType.METAL;
                level.playSound(player, pos,
                        type.getPlaceSound(), SoundSource.BLOCKS,
                        (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }

        if (itemStack.getItem() instanceof PipeBlockItem itemPipe) {
            BlockPos offsetPos = pos.offset(hit.getDirection().getNormal());
            BlockState stateAtSide = level.getBlockState(offsetPos);
            if (stateAtSide.getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                boolean wasPlaced = matBlock.replaceWithFramedPipe(level, offsetPos, stateAtSide, player, itemStack,
                        hit);
                if (wasPlaced) {
                    connectTile(pipeBlockEntity, pipeBlockEntity.getPipeNeighbor(hit.getDirection(), false),
                            hit.getDirection());
                }
                return wasPlaced ? InteractionResult.CONSUME : InteractionResult.FAIL;
            }
        }

        Set<GTToolType> types = ToolHelper.getToolTypes(itemStack);
        if (!types.isEmpty() && ToolHelper.canUse(itemStack)) {
            var result = pipeBlockEntity.onToolClick(types, itemStack, new UseOnContext(player, hand, hit));
            if (result.getSecond() == InteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(result.getFirst(), serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(itemStack, serverPlayer, 1);
                }
            }
            return result.getSecond();
        }
        return InteractionResult.PASS;
    }

    /*
     * @Override
     * public void onBlockClicked(@NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player playerIn) {
     * PipeBlockEntity tile = getBlockEntity(worldIn, pos);
     * if (tile != null) {
     * RayTraceAABB trace = collisionRayTrace(playerIn, worldIn, pos);
     * if (trace == null) {
     * super.onBlockClicked(worldIn, pos, playerIn);
     * return;
     * }
     * Direction facing = trace.sideHit;
     * Direction actualSide = CoverRayTracer.determineGridSideHit(trace);
     * if (actualSide != null) facing = actualSide;
     * Cover cover = tile.getCoverHolder().getCoverAtSide(facing);
     * if (cover != null) {
     * if (cover.onLeftClick(playerIn, trace)) return;
     * }
     * }
     * super.onBlockClicked(worldIn, pos, playerIn);
     * }
     */
    /**
     * Should be called to verify if a connection can be formed before
     * {@link #connectTile(PipeBlockEntity, PipeBlockEntity, Direction)} is called.
     * 
     * @return whether the connection is allowed.
     */
    public static boolean coverCheck(@NotNull PipeBlockEntity tile, @Nullable PipeBlockEntity tileAcross,
                                     Direction facing) {
        CoverBehavior tileCover = tile.getCoverHolder().getCoverAtSide(facing);
        CoverBehavior acrossCover = tileAcross != null ?
                tileAcross.getCoverHolder().getCoverAtSide(facing.getOpposite()) :
                null;
        return (tileCover == null || tileCover.canPipePassThrough()) &&
                (acrossCover == null || acrossCover.canPipePassThrough());
    }

    public static void connectTile(@NotNull PipeBlockEntity tile, @Nullable PipeBlockEntity tileAcross,
                                   Direction facing) {
        // abort connection if either tile refuses it.
        if (!tile.canConnectTo(facing) || tileAcross != null && !tileAcross.canConnectTo(facing.getOpposite())) return;

        // if one of the pipes is larger than the other, render it closed.
        tile.setConnected(facing, tileAcross != null &&
                tile.getStructure().getRenderThickness() > tileAcross.getStructure().getRenderThickness());
        if (tileAcross == null) return;
        tileAcross.setConnected(facing.getOpposite(),
                tileAcross.getStructure().getRenderThickness() > tile.getStructure().getRenderThickness());
        if (tile.getLevel().isClientSide) return;

        boolean blocked1 = tile.isBlocked(facing);
        boolean blocked2 = tileAcross.isBlocked(facing.getOpposite());

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            if (!blocked1 && !blocked2) {
                net.addEdge(node, other, true);
            } else if (net.getGraph().isDirected()) {
                if (!blocked1) net.addEdge(other, node, false);
                else if (!blocked2) net.addEdge(node, other, false);
            }
        }
    }

    public static void disconnectTile(@NotNull PipeBlockEntity tile, @Nullable PipeBlockEntity tileAcross,
                                      Direction facing) {
        tile.setDisconnected(facing);
        if (tileAcross == null) return;
        tileAcross.setDisconnected(facing.getOpposite());
        if (tile.getLevel().isClientSide) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(node, other, true);
        }
    }

    public static void blockTile(@NotNull PipeBlockEntity tile, @Nullable PipeBlockEntity tileAcross,
                                 Direction facing) {
        tile.setBlocked(facing);
        if (tileAcross == null || tile.getLevel().isClientSide) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.removeEdge(other, node, false);
        }
    }

    public static void unblockTile(@NotNull PipeBlockEntity tile, @Nullable PipeBlockEntity tileAcross,
                                   Direction facing) {
        tile.setUnblocked(facing);
        if (tileAcross == null || tile.getLevel().isClientSide) return;

        Map<WorldPipeNet, WorldPipeNetNode> tile2Nodes = new Object2ObjectOpenHashMap<>();
        for (WorldPipeNetNode node : getNodesForTile(tileAcross)) {
            tile2Nodes.put(node.getNet(), node);
        }

        for (WorldPipeNetNode node : getNodesForTile(tile)) {
            WorldPipeNet net = node.getNet();
            WorldPipeNetNode other = tile2Nodes.get(net);
            if (other == null) continue;
            net.addEdge(other, node, false);
        }
    }

    protected boolean allowsBlocking() {
        return true;
    }

    public static Collection<WorldPipeNetNode> getNodesForTile(PipeBlockEntity tile) {
        assert !tile.getLevel().isClientSide;
        return tile.getBlockType().getHandler(tile.getLevel(), tile.getBlockPos())
                .getOrCreateFromNets(tile.getLevel(), tile.getBlockPos(), tile.getStructure());
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (!level.isClientSide()) {
            getHandler(level, pos).removeFromNets(level, pos, getStructure());
        }
    }

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(BlockGetter world, BlockPos pos);

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(@NotNull ItemStack stack);

    // misc stuff //

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        if (getStructure() instanceof IPipeChanneledStructure channeledStructure) {
            if (channeledStructure.getChannelCount() > 1)
                tooltip.add(Component.translatable("gtceu.pipe.channels", channeledStructure.getChannelCount()));
        }
        getHandler(stack).addInformation(stack, level, tooltip, flag, getStructure());
        if (GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable(getConnectLangKey()));
            tooltip.add(Component.translatable("gtceu.tool_action.screwdriver.access_covers"));
            tooltip.add(Component.translatable("gtceu.tool_action.crowbar"));
        } else {
            tooltip.add(Component.translatable("gtceu.tool_action.show_tooltips"));
        }
    }

    protected String getConnectLangKey() {
        return "gregtech.tool_action.wrench.connect_and_block";
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        BlockEntity tileEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (tileEntity instanceof PipeBlockEntity pipeTile) {
            pipeTile.getDrops(drops, state);
        }
        return drops;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type,
                                EntityType<?> entityType) {
        return false;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null && tile.getFrameMaterial() == null && tile.getOffsetTimer() % 10 == 0) {
            TemperatureLogic logic = tile.getTemperatureLogic();
            if (logic != null) {
                long tick = Platform.getMinecraftServer().getTickCount();
                EntityDamageUtil.applyTemperatureDamage(living, logic.getTemperature(tick), 1f, 5);
            }
        }
    }

    /*
     * TODO fix
     * 
     * @Override
     * public boolean recolorBlock(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side,
     * 
     * @NotNull EnumDyeColor color) {
     * if (getStructure().isPaintable()) {
     * PipeBlockEntity tile = getBlockEntity(world, pos);
     * if (tile != null && tile.getPaintingColor() != color.colorValue) {
     * tile.setPaintingColor(color.colorValue, false);
     * return true;
     * }
     * }
     * return false;
     * }
     */

    // collision boxes //

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MutableObject<VoxelShape> shape = new MutableObject<>(Shapes.empty());
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null) {
            tile.getCoverBoxes(bb -> shape.setValue(Shapes.or(shape.getValue(), bb)));
            if (tile.getFrameMaterial() != null) {
                shape.setValue(Shapes.or(shape.getValue(), Shapes.block()));
            }
            shape.setValue(Shapes.or(shape.getValue(), getStructure().getPipeBoxes(tile)));
        } else {
            shape.setValue(Shapes.block());
        }
        return shape.getValue();
    }

    public boolean hasPipeCollisionChangingItem(BlockGetter world, BlockPos pos, Entity entity) {
        if (entity instanceof Player player) {
            return hasPipeCollisionChangingItem(world, pos, player.getMainHandItem()) ||
                    hasPipeCollisionChangingItem(world, pos, player.getOffhandItem()) ||
                    entity.isShiftKeyDown() && isHoldingPipe(player);
        }
        return false;
    }

    public boolean isHoldingPipe(Player player) {
        return isPipeItem(player.getMainHandItem()) || isPipeItem(player.getOffhandItem());
    }

    public boolean isPipeItem(ItemStack stack) {
        return stack.getItem() instanceof PipeBlockItem block && this.getClass().isInstance(block.getBlock());
    }

    @Nullable
    public static PipeBlock getBlockFromItem(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PipeBlockItem block) return block.getBlock();
        else return null;
    }

    public boolean hasPipeCollisionChangingItem(BlockGetter world, BlockPos pos, ItemStack stack) {
        if (isPipeTool(stack)) return true;

        PipeBlockEntity tile = getBlockEntity(world, pos);
        if (tile == null) return false;

        PipeCoverHolder coverable = tile.getCoverHolder();
        final boolean hasAnyCover = coverable.hasAnyCover();

        if (hasAnyCover && ToolHelper.isTool(stack, GTToolType.SCREWDRIVER)) return true;
        final boolean acceptsCovers = coverable.acceptsCovers();

        return CoverPlaceBehavior.isCoverBehaviorItem(stack, () -> hasAnyCover, coverDef -> acceptsCovers);
    }

    public boolean isPipeTool(@NotNull ItemStack stack) {
        return ToolHelper.isTool(stack, getToolClass());
    }

    public GTToolType getToolClass() {
        return GTToolType.WRENCH;
    }

    // blockstate //

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, FRAMED);
    }

    public static BlockState writeConnectionMask(@NotNull BlockState state, byte connectionMask) {
        for (Direction facing : GTUtil.DIRECTIONS) {
            state = state.setValue(FACINGS.get(facing), GTUtil.evalMask(facing, connectionMask));
        }
        return state;
    }

    public static byte readConnectionMask(@NotNull BlockState state) {
        byte mask = 0;
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (state.getValue(FACINGS.get(facing))) {
                mask |= (byte) (1 << facing.ordinal());
            }
        }
        return mask;
    }

    // tile entity //

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && state.getValue(BlockProperties.SERVER_TICK)) {
            return (pLevel, pPos, pState, pTile) -> {
                if (pTile instanceof IPipeNode<?, ?> pipeNode) {
                    pipeNode.serverTick();
                }
            };
        }
        return null;
    }

    @Nullable
    public PipeBlockEntity getBlockEntity(@NotNull BlockGetter world, @NotNull BlockPos pos) {
        if (lastTilePos.get().equals(pos)) {
            PipeBlockEntity tile = lastTile.get().get();
            if (tile != null && !tile.isRemoved()) return tile;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof PipeBlockEntity pipe) {
            lastTilePos.set(pos.immutable());
            lastTile.set(new WeakReference<>(pipe));
            return pipe;
        } else return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(GTBlockEntities.NEW_PIPE.get(), pos, state);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        Direction facing = GTUtil.getFacingToNeighbor(pos, neighbor);
        if (facing == null) return;
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null) tile.onNeighborChanged(facing);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null) {
            TemperatureLogic temperatureLogic = tile.getTemperatureLogic();
            int temp = temperatureLogic == null ? 0 : temperatureLogic
                    .getTemperature(Platform.getMinecraftServer().getTickCount());
            // max light at 5000 K
            // min light at 500 K
            if (temp >= 5000) {
                return 15;
            }
            if (temp > 500) {
                return (temp - 500) * 15 / (4500);
            }
        }
        return 0;
    }

    // cover compatibility //

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null) {
            Direction facing = GTUtil.getFacingToNeighbor(pos, neighborPos);
            if (facing != null) tile.onNeighborChanged(facing);
            // TODO redstone
            // tile.getCoverHolder().notifyBlockUpdate();
        }
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        PipeBlockEntity tile = getBlockEntity(level, pos);
        return tile != null ? tile.getCoverHolder().getOutputRedstoneSignal(direction) : 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        PipeBlockEntity tile = getBlockEntity(level, pos);
        return tile != null && tile.getCoverHolder().canConnectRedstone(direction);
    }
}
