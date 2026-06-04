package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.IPipeVariant;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PipeBlock<NodeDataType>
                               extends Block
                               implements EntityBlock, SimpleWaterloggedBlock {

    @Getter
    public final IPipeVariant<NodeDataType> pipeVariant;

    protected final Map<@Nullable Direction, VoxelShape> shapes = new IdentityHashMap<>();

    @Getter
    protected final PipeNetworkType networkType;

    public PipeBlock(Properties properties, IPipeVariant<NodeDataType> pipeVariant, PipeNetworkType networkType) {
        super(properties);
        this.pipeVariant = pipeVariant;
        this.networkType = networkType;
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));

        float min = (16 - pipeVariant.getThickness() * 16) / 2f;
        float max = min + pipeVariant.getThickness() * 16;
        shapes.put(null, Block.box(min, min, min, max, max, max));
        for (Direction dir : GTUtil.DIRECTIONS) {
            var coords = GTMath.getCoordinates(dir, min, max);
            Vector3f minCoord = coords.getLeft();
            Vector3f maxCoord = coords.getRight();
            shapes.put(dir, Block.box(minCoord.x, minCoord.y, minCoord.z, maxCoord.x, maxCoord.y, maxCoord.z));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) :
                super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public final @Nullable PipeBlockEntity<?, ?> newBlockEntity(BlockPos pos, BlockState state) {
        return networkType.blockEntityType().get().create(pos, state);
    }

    /**
     * Add data via placement.
     */
    public abstract NodeDataType createRawData();

    public NodeDataType createProperties() {
        return pipeVariant.modifyProperties(createRawData());
    }

    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeVariant.createPipeModel(this, provider);
    }

    public static @Nullable PipeBlockEntity<?, ?> getPipeBE(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipeBlockEntity) {
            return pipeBlockEntity;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        PipeBlockEntity<?, ?> pipeTile = getPipeBE(level, pos);

        if (pipeTile != null && placer instanceof Player player) {
            // Color pipes/cables on place if holding spray can in off-hand
            ItemStack offhand = placer.getOffhandItem();
            for (int i = 0; i < DyeColor.values().length; i++) {
                if (offhand.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                    ((IInteractionItem) GTItems.SPRAY_CAN_DYES[i].get().getComponents().get(0))
                            .useOn(new UseOnContext(player, InteractionHand.OFF_HAND,
                                    new BlockHitResult(Vec3.ZERO, player.getDirection(), pos, false)));
                    break;
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean isMoving) {
        if (level.isClientSide) return;
        var pipeBE = getPipeBE(level, pos);
        if (pipeBE != null) {
            pipeBE.onNeighborChanged(neighborBlock, neighborPos, isMoving);
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && !pState.is(pNewState.getBlock())) {
            pLevel.removeBlockEntity(pPos);
            var pipeBE = getPipeBE(pLevel, pPos);
            if (pipeBE != null) {
                pipeBE.getCoverContainer().dropAllCovers();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockEntity entity = level.getBlockEntity(pos);

        PipeBlockEntity<?, ?> pipeBlockEntity = null;
        if (entity instanceof PipeBlockEntity<?, ?> pbe) {
            pipeBlockEntity = pbe;
        }
        if (pipeBlockEntity == null) {
            return InteractionResult.FAIL;
        }

        if (pipeBlockEntity.getFrameMaterial().isNull() && pipeVariant.getThickness() < 1) {
            var frameBlock = MaterialBlock.getFrameboxFromItem(itemStack);
            if (frameBlock != null) {
                pipeBlockEntity.setFrameMaterial(frameBlock.material);
                if (!player.isCreative()) itemStack.shrink(1);
                SoundType type = VanillaRecipeHelper.isMaterialWood(frameBlock.material) ? SoundType.WOOD :
                        SoundType.METAL;
                level.playSound(player, pos,
                        type.getPlaceSound(), SoundSource.BLOCKS,
                        (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        if (itemStack.getItem() instanceof PipeBlockItem itemPipe) {
            BlockPos offsetPos = pos.offset(hit.getDirection().getNormal());
            BlockState stateAtSide = level.getBlockState(offsetPos);
            if (stateAtSide.getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                if (itemPipe.getBlock().pipeVariant == pipeVariant) {
                    boolean wasPlaced = matBlock.replaceWithFramedPipe(level, offsetPos, stateAtSide, player, itemStack,
                            hit);
                    if (wasPlaced) {
                        pipeBlockEntity.setConnection(hit.getDirection(), true, false);
                    }
                    return wasPlaced ? InteractionResult.CONSUME : InteractionResult.FAIL;
                }
            }
        }

        Set<GTToolType> types = ToolHelper.getToolTypes(itemStack);
        if ((!types.isEmpty() && ToolHelper.canUse(itemStack))) {
            var result = pipeBlockEntity.onToolClick(new ExtendedUseOnContext(player, hand, hit));
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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        var pipeNode = getPipeBE(level, pos);
        if (pipeNode == null) {
            GTCEu.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipeNode.getFrameMaterial().isNull()) {
            BlockState frameState = Objects.requireNonNull(GTMaterialBlocks.MATERIAL_BLOCKS
                    .get(TagPrefix.frameGt, pipeNode.getFrameMaterial()))
                    .getDefaultState();
            frameState.getBlock().entityInside(frameState, level, pos, entity);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        var pipeNode = getPipeBE(level, pos);
        return pipeNode != null && !pipeNode.getFrameMaterial().isNull();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var pipeNode = getPipeBE(level, pos);
        if (pipeNode != null && !pipeNode.getFrameMaterial().isNull()) {
            return MaterialBlock.FRAME_COLLISION_BOX;
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        var pipeNode = getPipeBE(pLevel, pPos);
        var connections = 0;

        if (pipeNode != null) {
            if (!pipeNode.getFrameMaterial().isNull()) {
                return Shapes.block();
            }
            connections = pipeNode.getVisualConnections();
            VoxelShape shape = getShapes(connections);
            shape = Shapes.or(shape, pipeNode.getCoverContainer().addCoverCollisionBoundingBox());

            if (context instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player) {
                var coverable = pipeNode.getCoverContainer();
                var held = player.getMainHandItem();
                Set<GTToolType> types = Set.of(getPipeTuneTool());

                PipeBlock<?> block;

                if (held.getItem() instanceof BlockItem blockItem) {
                    block = blockItem.getBlock() instanceof PipeBlock<?> pipeBlock ? pipeBlock : null;
                } else {
                    block = null;
                }

                if (player.isShiftKeyDown() && held.isEmpty() && coverable.hasAnyCover()) return Shapes.block();

                if (types.stream().anyMatch(type -> type.matchTags.stream().anyMatch(held::is))) return Shapes.block();

                if (CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover,
                        coverDef -> ICoverable.canPlaceCover(coverDef, coverable)))
                    return Shapes.block();

                if (block != null && block.getNetworkType() == getNetworkType()) return Shapes.block();
            }
            return shape;
        }
        return getShapes(connections);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (blockEntityType == networkType.blockEntityType().get()) {
            if (!level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof PipeBlockEntity<?, ?> pipeNode) {
                        pipeNode.serverTick();
                    }
                };
            }
        }
        return null;
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                    @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        var pipe = getPipeBE(level, pos);
        if (pipe != null) {
            var appearance = pipe.getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState,
                    sourcePos);
            if (appearance != null) return appearance;
        }
        return super.getAppearance(state, level, pos, side, sourceState, sourcePos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (blockEntity instanceof PipeBlockEntity<?, ?> pipeTile) {
            if (!pipeTile.getFrameMaterial().isNull()) {
                drops.addAll(Objects
                        .requireNonNull(
                                GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeTile.getFrameMaterial()))
                        .getDefaultState().getDrops(builder));
            }
            for (Direction direction : GTUtil.DIRECTIONS) {
                pipeTile.getCoverContainer().removeCover(direction, null);
            }
        }
        return drops;
    }

    public GTToolType getPipeTuneTool() {
        return GTToolType.WRENCH;
    }

    public VoxelShape getShapes(int connections) {
        return this.shapes.entrySet().stream()
                .filter(entry -> entry.getKey() == null || PipeBlockEntity.isConnected(connections, entry.getKey()))
                .map(Map.Entry::getValue)
                .reduce(Shapes.empty(), Shapes::or);
    }
}
