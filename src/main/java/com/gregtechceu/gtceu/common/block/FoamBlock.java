package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FoamBlock extends Block {

    private final boolean isReinforced;
    private final DyeColor color;

    public FoamBlock(Properties properties, DyeColor color, boolean isReinforced) {
        super(properties);
        this.color = color;
        this.isReinforced = isReinforced;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (!stackInHand.isEmpty() && stackInHand.is(ItemTags.SAND)) {
            level.setBlockAndUpdate(pos, getPetrifiedBlock(state));
            level.playSound(player, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative())
                stackInHand.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        int lightLevel = (level.canSeeSky(pos) && level.isDay()) ? 16 : level.getRawBrightness(pos, 0);
        if (random.nextInt(20 - lightLevel) == 0) {
            level.setBlockAndUpdate(pos, getPetrifiedBlock(state));
        }
    }

    private BlockState getPetrifiedBlock(BlockState state) {
        var block = this.isReinforced ? GTBlocks.REINFORCED_STONES.get(this.color) :
                GTBlocks.PETRIFIED_FOAMS.get(this.color);
        return block.getDefaultState();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            float reinforced = this.isReinforced ? 0.85f : 0.9f;
            entity.makeStuckInBlock(state, new Vec3(reinforced, reinforced, reinforced));
        }

        if (!level.isClientSide()) {
            entity.setSharedFlagOnFire(false);
            entity.clearFire();
        }
    }
}
