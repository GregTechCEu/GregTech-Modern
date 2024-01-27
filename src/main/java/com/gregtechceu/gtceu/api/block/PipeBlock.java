package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.*;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote PipeBlock
 */
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PipeBlock<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends AppearanceBlock implements EntityBlock, IBlockRendererProvider {
    public final PipeType pipeType;

    public PipeBlock(Properties properties, PipeType pipeType) {
        super(properties);
        this.pipeType = pipeType;
        registerDefaultState(defaultBlockState().setValue(BlockProperties.SERVER_TICK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BlockProperties.SERVER_TICK));
    }

    @Override
    public final PipeBlockEntity<PipeType, NodeDataType> newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    public abstract WorldPipeNetType getWorldPipeNet(ServerLevel level);

    public abstract BlockEntityType<? extends PipeBlockEntity<PipeType, NodeDataType>> getBlockEntityType();

    /**
     * Add data via placement.
     */
    public abstract NodeDataType createRawData(BlockState pState, @Nullable ItemStack pStack);

    public NodeDataType createProperties(BlockState state, @Nullable ItemStack stack) {
        return pipeType.modifyProperties(createRawData(state, stack));
    }

    public abstract NodeDataType createProperties(IPipeNode<PipeType, NodeDataType> pipeTile);

    /**
     * Sometimes some people
     */
    public abstract NodeDataType getFallbackType();

    @Nullable
    @Override
    public abstract PipeBlockRenderer getRenderer(BlockState state);

    protected abstract PipeModel getPipeModel();

    public void updateActiveNodeStatus(@NotNull Level worldIn, BlockPos pos,
                                       IPipeNode<PipeType, NodeDataType> pipeTile) {
        if (worldIn.isClientSide) return;

        PipeNet<NodeDataType> pipeNet = getWorldPipeNet((ServerLevel) worldIn).getNetFromPos(pos);
        if (pipeNet != null && pipeTile != null) {
            int activeConnections = pipeTile.getConnections(); // remove blocked connections
            boolean isActiveNodeNow = activeConnections != 0;
            boolean modeChanged = pipeNet.markNodeAsActive(pos, isActiveNodeNow);
            if (modeChanged) {
                onActiveModeChange(worldIn, pos, isActiveNodeNow, false);
            }
        }
    }

    /**
     * Get pipe nodes with the same pipe type.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public IPipeNode<PipeType, NodeDataType> getPipeTile(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IPipeNode<?,?> pipeTile && pipeTile.getPipeType().type().equals(pipeType.type())) {
            return (IPipeNode<PipeType, NodeDataType>) pipeTile;
        }
        return null;
    }

    /**
     * Can be used to update tile entity to tickable when node becomes active
     * usable for fluid pipes, as example
     */
    protected void onActiveModeChange(Level world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {}

    public boolean canConnect(IPipeNode<PipeType, NodeDataType> selfTile, Direction facing) {
        if (selfTile.getPipeLevel().getBlockState(selfTile.getPipePos().relative(facing)).getBlock() == Blocks.AIR)
            return false;
        CoverBehavior cover = selfTile.getCoverContainer().getCoverAtSide(facing);
        if (cover != null && !cover.canPipePassThrough()) {
            return false;
        }
        BlockEntity other = selfTile.getNeighbor(facing);
        if (other instanceof IPipeNode<?,?> node) {
            cover = node.getCoverContainer().getCoverAtSide(facing.getOpposite());
            if (cover != null && !cover.canPipePassThrough())
                return false;
            return canPipesConnect(selfTile, facing, (IPipeNode<PipeType, NodeDataType>) other);
        }
        return canPipeConnectToBlock(selfTile, facing, other);
    }

    public abstract boolean canPipesConnect(IPipeNode<PipeType, NodeDataType> selfTile, Direction side,
                                            IPipeNode<PipeType, NodeDataType> sideTile);

    public abstract boolean canPipeConnectToBlock(IPipeNode<PipeType, NodeDataType> selfTile, Direction side,
                                                  @Nullable BlockEntity tile);

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);


        IPipeNode<PipeType, NodeDataType> pipeTile = getPipeTile(level, pos);
        if (pipeTile != null) {
            // Color pipes/cables on place if holding spray can in off-hand
            if (placer instanceof Player player) {
                ItemStack offhand = placer.getOffhandItem();
                for (int i = 0; i < DyeColor.values().length; i++) {
                    if (offhand.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                        ((IInteractionItem)GTItems.SPRAY_CAN_DYES[i].get().getComponents().get(0)).useOn(new UseOnContext(player, InteractionHand.OFF_HAND, new BlockHitResult(Vec3.ZERO, player.getDirection(), pos, false)));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        IPipeNode<PipeType, NodeDataType> pipeTile = getPipeTile(level, pos);
        if (pipeTile != null) {
            Direction facing = GTUtil.getFacingToNeighbor(pos, fromPos);
            if (facing == null) return;
            if (!ConfigHolder.INSTANCE.machines.gt6StylePipesCables) {
                boolean open = pipeTile.isConnected(facing);
                boolean canConnect = pipeTile.getCoverContainer().getCoverAtSide(facing) != null ||
                    this.canConnect(pipeTile, facing);
                if (!open && canConnect && state.getBlock() != block)
                    pipeTile.setConnection(facing, true, false);
                if (open && !canConnect)
                    pipeTile.setConnection(facing, false, false);
                updateActiveNodeStatus(level, pos, pipeTile);
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && !pState.is(pNewState.getBlock())) {
            pLevel.removeBlockEntity(pPos);
            if (pLevel instanceof ServerLevel serverLevel) {
                getWorldPipeNet(serverLevel).removeNode(pPos);
            }
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        IPipeNode<PipeType, NodeDataType> pipeTile = getPipeTile(level, pos);
        if (pipeTile != null) {
            pipeTile.getCoverContainer().dropAllCovers();
        }
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            getWorldPipeNet(serverLevel).removeNode(pos);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        IPipeNode<PipeType, NodeDataType> pipeTile = getPipeTile(level, pos);
        if (pipeTile != null) {
            int activeConnections = pipeTile.getConnections();
            boolean isActiveNode = activeConnections != 0;
            getWorldPipeNet(level).addNode(pos, createRawData(state, null), 0, activeConnections, isActiveNode);
            onActiveModeChange(level, pos, isActiveNode, true);
        }
    }

    /*
    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (level instanceof ServerLevel serverLevel) {
            PipeNet<NodeDataType> net = getWorldPipeNet(serverLevel).getNetFromPos(pos);
            if (net != null) {
                net.onNeighbourUpdate(neighbor);
            }
        }
    }
    */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockEntity entity = level.getBlockEntity(pos);

        Set<GTToolType> types = ToolHelper.getToolTypes(itemStack);
        if (entity instanceof IToolable toolable && !types.isEmpty() && ToolHelper.canUse(itemStack)) {
            var result = toolable.onToolClick(types, itemStack, new UseOnContext(player, hand, hit));
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

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        var pipeNode = getPipeTile(pLevel, pPos);
        var connections = 0;
        if (pipeNode != null) {
            connections = pipeNode.getVisualConnections();
            VoxelShape shape = getPipeModel().getShapes(connections);
            shape = Shapes.or(shape, pipeNode.getCoverContainer().addCoverCollisionBoundingBox());

            if (context instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player) {
                var coverable = pipeNode.getCoverContainer();
                var held = player.getMainHandItem();
                Set<GTToolType> types = Set.of(GTToolType.WIRE_CUTTER, GTToolType.WRENCH);
                BlockEntity tile = pLevel.getBlockEntity(pPos);
                if (tile instanceof PipeBlockEntity<?,?> pipeTile) {
                    types = Set.of(pipeTile.getPipeTuneTool());
                }

                if (types.stream().anyMatch(type -> type.itemTags.stream().anyMatch(held::is)) ||
                        CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover, coverDef -> ICoverable.canPlaceCover(coverDef, coverable)) ||
                        (held.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PipeBlock<?,?,?> pipeBlock && pipeBlock.pipeType.type().equals(pipeType.type()))) {
                    return Shapes.block();
                }
            }
            return shape;
        }
        return getPipeModel().getShapes(connections);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getBlockEntityType()) {
            if (!level.isClientSide && state.getValue(BlockProperties.SERVER_TICK)) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IPipeNode<?, ?> pipeNode) {
                        pipeNode.serverTick();
                    }
                };
            }
        }
        return null;
    }


    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, BlockState sourceState, BlockPos sourcePos) {
        var pipe = getPipeTile(level, pos);
        if (pipe != null) {
            var appearance = pipe.getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
            if (appearance != null) return appearance;
        }
        return super.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        BlockEntity tileEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tileEntity instanceof IPipeNode<?,?> pipeTile) {
            for (Direction direction : GTUtil.DIRECTIONS) {
                pipeTile.getCoverContainer().removeCover(direction, null);
            }
        }
        return super.getDrops(state, builder);
    }
}
