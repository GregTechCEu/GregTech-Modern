package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.SurfaceRockBlockItem;
import com.gregtechceu.gtceu.client.renderer.block.SurfaceRockRenderer;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceRockBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape AABB_NORTH = Block.box(2, 2, 0, 14, 14, 3);
    private static final VoxelShape AABB_SOUTH = Block.box(2, 2, 13, 14, 14, 16);
    private static final VoxelShape AABB_WEST = Block.box(0, 2, 2, 3, 14, 14);
    private static final VoxelShape AABB_EAST = Block.box(13, 2, 2, 16, 14, 14);
    private static final VoxelShape AABB_UP = Block.box(2, 13, 2, 14, 16, 14);
    private static final VoxelShape AABB_DOWN = Block.box(2, 0, 2, 14, 3, 14);

    @Getter
    private final Material material;

    public SurfaceRockBlock(Properties properties, Material material) {
        super(properties);
        this.material = material;

        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));

        if (GTCEu.isClientSide()) {
            SurfaceRockRenderer.create(this);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
                                       FluidState fluid) {
        if (!level.isClientSide) {
            ServerCache.instance.prospectSurfaceRockMaterial(
                    level.dimension(),
                    this.material,
                    pos,
                    (ServerPlayer) player);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!level.isClientSide) {
            ServerCache.instance.prospectSurfaceRockMaterial(
                    level.dimension(),
                    this.material,
                    pos,
                    (ServerPlayer) player);
        }
        if (level.destroyBlock(pos, true, player)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> AABB_DOWN;
            case UP -> AABB_UP;
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var facing = state.getValue(FACING);
        var attachedBlock = pos.relative(facing);

        return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, facing.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!canSurvive(state, level, pos)) {
            Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateForDirection(context.getNearestLookingVerticalDirection());
    }

    public BlockState getStateForDirection(Direction direction) {
        return defaultBlockState().setValue(FACING, direction);
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedBlockColor() {
        return (state, reader, pos, tintIndex) -> {
            if (state.getBlock() instanceof SurfaceRockBlock block) {
                return block.material.getMaterialRGB();
            }
            return -1;
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintedItemColor() {
        return (stack, tintIndex) -> {
            if (stack.getItem() instanceof SurfaceRockBlockItem surfaceRock) {
                return surfaceRock.getMat().getMaterialRGB();
            }
            return -1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public String getDescriptionId() {
        return "block.surface_rock";
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("block.surface_rock", material.getLocalizedName());
    }
}
