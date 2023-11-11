package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote PipeBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PipeBlock <PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends IAttachData, WorldPipeNetType extends LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends AppearanceBlock implements EntityBlock, IBlockRendererProvider {
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

    /**
     * Sometimes some people
     */
    public abstract NodeDataType getFallbackType();

    @Nullable
    @Override
    public abstract PipeBlockRenderer getRenderer(BlockState state);

    protected abstract PipeModel getPipeModel();

    /**
     * Get pipe nodes with the same pipe type.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public IPipeNode<PipeType, NodeDataType> getPileTile(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IPipeNode<?,?> pipeTile && pipeTile.getPipeType().type().equals(pipeType.type())) {
            return (IPipeNode<PipeType, NodeDataType>) pipeTile;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        var pipeNode = getPileTile(pLevel, pPos);
        if (pipeNode != null && pLevel instanceof ServerLevel serverLevel) {
            var net = getWorldPipeNet(serverLevel);
            if (net.getNetFromPos(pPos) == null) {
                net.addNode(pPos, pipeType.modifyProperties(createRawData(pState, pStack)), Node.DEFAULT_MARK, Node.ALL_CLOSED, true);
            } else {
                net.updateData(pPos, pipeType.modifyProperties(createRawData(pState, pStack)));
            }

            // if player is placing a pipe next to an existing pipe
            if (PipeBlockItem.LAST_CONTEXT != null && !PipeBlockItem.LAST_CONTEXT.replacingClickedOnBlock()) {
                var attachPos = PipeBlockItem.LAST_CONTEXT.getClickedPos().relative(PipeBlockItem.LAST_CONTEXT.getClickedFace().getOpposite());
                var attachSide = PipeBlockItem.LAST_CONTEXT.getClickedFace();
                if (attachPos.relative(attachSide).equals(pPos)) {

                    var attachNode = getPileTile(pLevel, attachPos);
                    if (attachNode != null) { // if is a pipe node
                        if (attachNode.isBlocked(attachSide)) {
                            attachNode.setBlocked(attachSide, false);
                        }
                        if (pipeNode.isBlocked(attachSide.getOpposite())) {
                            pipeNode.setBlocked(attachSide.getOpposite(), false);
                        }
                    } else if (pipeNode.isBlocked(attachSide.getOpposite()) && pipeNode.canAttachTo(attachSide.getOpposite())) { // if it can attach to
                        pipeNode.setBlocked(attachSide.getOpposite(), false);
                    }
                }
            }

            //If you place a pipe next to a pipe with an already open connection, it will connect automatically
            for (var side : Direction.values()) {
                if (pipeNode.isBlocked(side)) {
                    var attachPos = pPos.relative(side);
                    var attachNode = getPileTile(pLevel, attachPos);
                    if (attachNode != null && !attachNode.isBlocked(side.getOpposite())) {
                        pipeNode.setBlocked(side, false);
                    }
                }
            }

            pipeNode.updateConnections();
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(state.getBlock()) && level instanceof ServerLevel serverLevel) {
            var net = getWorldPipeNet(serverLevel);
            if (net.getNetFromPos(pos) == null) {
                net.addNode(pos, pipeType.modifyProperties(createRawData(state, null)), Node.DEFAULT_MARK, Node.ALL_CLOSED, true);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        var pipeNode = getPileTile(pLevel, pPos);
        if (pipeNode != null) {
            pipeNode.onNeighborChanged(pBlock, pFromPos, pIsMoving);
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && !pState.is(pNewState.getBlock())) {
            pLevel.updateNeighbourForOutputSignal(pPos, this);
            pLevel.removeBlockEntity(pPos);
            if (pLevel instanceof ServerLevel serverLevel) {
                getWorldPipeNet(serverLevel).removeNode(pPos);
            }
        }
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        var pipeNode = getPileTile(pLevel, pPos);
        var connections = 0;
        if (pipeNode != null) {
            connections = pipeNode.getVisualConnections();
            VoxelShape shape = getPipeModel().getShapes(connections);
            shape = Shapes.or(shape, pipeNode.getCoverContainer().addCoverCollisionBoundingBox());

            if (context instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player) {
                var coverable = pipeNode.getCoverContainer();
                var held = player.getMainHandItem();
                if (held.is(GTToolType.WIRE_CUTTER.itemTag) || held.is(GTToolType.WRENCH.itemTag) ||
                        CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover, coverDef -> ICoverable.canPlaceCover(coverDef, coverable)) ||
                        (held.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PipeBlock<?,?,?> pipeBlock && pipeBlock.pipeType.type().equals(pipeType.type()))) {
                    return Shapes.or(Shapes.block(), shape);
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
                    if (pTile instanceof IPipeNode pipeNode) {
                        pipeNode.serverTick();
                    }
                };
            }
        }
        return null;
    }


    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, BlockState sourceState, BlockPos sourcePos) {
        var pipe = getPileTile(level, pos);
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
            for (Direction direction : Direction.values()) {
                pipeTile.getCoverContainer().removeCover(direction, null);
            }
        }
        return super.getDrops(state, builder);
    }
}
