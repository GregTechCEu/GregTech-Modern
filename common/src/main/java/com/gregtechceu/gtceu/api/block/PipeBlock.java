package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

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

    @Nullable
    @SuppressWarnings("unchecked")
    protected IPipeNode<PipeType, NodeDataType> getPileTile(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IPipeNode<?,?> pipeTile) {
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
                net.addNode(pPos, pipeType.modifyProperties(createRawData(pState, pStack)), Node.DEFAULT_MARK, Node.ALL_OPENED, true);
            } else {
                net.updateData(pPos, pipeType.modifyProperties(createRawData(pState, pStack)));
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
                net.addNode(pos, pipeType.modifyProperties(createRawData(state, null)), Node.DEFAULT_MARK, Node.ALL_OPENED, true);
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
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        var pipeNode = getPileTile(pLevel, pPos);
        var connections = 0;
        if (pipeNode != null) {
            connections = pipeNode.getVisualConnections();
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

}
