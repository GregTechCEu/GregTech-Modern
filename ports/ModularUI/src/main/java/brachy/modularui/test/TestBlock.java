package brachy.modularui.test;

import brachy.modularui.factory.UIFactories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class TestBlock extends BaseEntityBlock {

    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityCreator;

    public TestBlock(BiFunction<BlockPos, BlockState, BlockEntity> blockEntityCreator) {
        super(Properties.of());
        this.blockEntityCreator = blockEntityCreator;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return this.blockEntityCreator.apply(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            UIFactories.blockEntity().open(player, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> ((AbstractBlockEntity) blockEntity).update();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        // this shouldn't be done. Thankfully this method is unused in practice, so it's fine!
        return null;
    }
}
