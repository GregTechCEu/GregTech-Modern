package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.renderer.block.SurfaceRockRenderer;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceRockBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.VERTICAL_DIRECTION;

    @Getter
    private final Material material;

    private static final VoxelShape SHAPE_UP = Block.box(2, 13, 2, 15, 16, 14);
    private static final VoxelShape SHAPE_DOWN = Block.box(2, 0, 2, 15, 3, 14);

    public SurfaceRockBlock(Properties properties, Material material) {
        super(properties);
        this.material = material;

        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));

        if (Platform.isClient()) {
            SurfaceRockRenderer.create(this);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.destroyBlock(pos, true, player)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING) == Direction.UP ? SHAPE_UP : SHAPE_DOWN;
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingVerticalDirection());
    }

    @Environment(EnvType.CLIENT)
    public static BlockColor tintedColor() {
        return (state, reader, pos, tintIndex) -> {
            if (state.getBlock() instanceof SurfaceRockBlock block) {
                return block.material.getMaterialRGB();
            }
            return -1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
