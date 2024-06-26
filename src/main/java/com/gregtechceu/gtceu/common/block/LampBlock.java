package com.gregtechceu.gtceu.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LampBlock extends Block {

    public static final BooleanProperty BLOOM = BooleanProperty.create("bloom");
    public static final BooleanProperty LIGHT = BlockStateProperties.LIT;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final String TAG_INVERTED = "inverted";
    public static final String TAG_BLOOM = "bloom";
    public static final String TAG_LIT = "lit";

    public static final int BLOOM_FLAG = 1;
    public static final int LIGHT_FLAG = 2;
    public static final int INVERTED_FLAG = 4;

    public final DyeColor color;

    public LampBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
        registerDefaultState(defaultBlockState()
                .setValue(BLOOM, true)
                .setValue(LIGHT, true)
                .setValue(INVERTED, false)
                .setValue(POWERED, false));
    }

    public static boolean isLightActive(BlockState state) {
        return state.getValue(INVERTED) == state.getValue(POWERED);
    }

    public boolean isInverted(BlockState state) {
        return state.getValue(INVERTED);
    }

    public boolean isLightEnabled(BlockState state) {
        return state.getValue(LIGHT);
    }

    public boolean isBloomEnabled(BlockState state) {
        return state.getValue(BLOOM);
    }

    public ItemStack getStackFromIndex(int i) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LampBlock.TAG_INVERTED, (i & LampBlock.INVERTED_FLAG) == 0);
        tag.putBoolean(LampBlock.TAG_BLOOM, (i & LampBlock.BLOOM_FLAG) == 0);
        tag.putBoolean(LampBlock.TAG_LIT, (i & LampBlock.LIGHT_FLAG) == 0);
        ItemStack stack = new ItemStack(this);
        stack.setTag(tag);
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(INVERTED, BLOOM, LIGHT, POWERED));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIGHT) && isLightActive(state) ? 15 : 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean powered = state.getValue(POWERED);
            if (powered != level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.setValue(POWERED, !powered), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        if (!level.isClientSide) {
            if (state.getValue(POWERED)) {
                if (!level.hasNeighborSignal(pos)) {
                    level.updateNeighborsAt(pos, this);
                }
            } else if (level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.setValue(POWERED, true), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide && state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(POWERED, false), state.getValue(LIGHT) ? 2 | 8 : 2);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag()) {
            if (stack.getTag().getBoolean(TAG_INVERTED))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.inverted"));
            if (!stack.getTag().getBoolean(TAG_BLOOM))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_bloom"));
            if (!stack.getTag().getBoolean(TAG_LIT))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_light"));
        }
    }
}
