package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.client.renderer.block.LampRenderer;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LampBlock extends Block implements IBlockRendererProvider {

    public static final BooleanProperty BLOOM = GTBlockStateProperties.BLOOM;
    public static final BooleanProperty LIGHT = BlockStateProperties.LIT;
    public static final BooleanProperty INVERTED = GTBlockStateProperties.INVERTED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final String TAG_INVERTED = "inverted";
    public static final String TAG_BLOOM = "bloom";
    public static final String TAG_LIT = "lit";

    public static final int BLOOM_FLAG = 1;
    public static final int LIGHT_FLAG = 2;
    public static final int INVERTED_FLAG = 4;
    public static final int POWERED_FLAG = 8;

    public final DyeColor color;
    public final boolean bordered;
    private final Map<BlockState, LampRenderer> renderers = new IdentityHashMap<>();

    public LampBlock(Properties properties, DyeColor color, boolean bordered) {
        super(properties);
        this.color = color;
        this.bordered = bordered;
        registerDefaultState(defaultBlockState()
                .setValue(GTBlockStateProperties.BLOOM, true)
                .setValue(LIGHT, true)
                .setValue(INVERTED, false)
                .setValue(POWERED, false));
        for (BlockState state : getStateDefinition().getPossibleStates()) {
            renderers.put(state, new LampRenderer(this, state));
        }
    }

    public static boolean isLightActive(BlockState state) {
        return state.getValue(INVERTED) != state.getValue(POWERED);
    }

    public static boolean isInverted(BlockState state) {
        return state.getValue(INVERTED);
    }

    public static boolean isLightEnabled(BlockState state) {
        return state.getValue(LIGHT);
    }

    public static boolean isBloomEnabled(BlockState state) {
        return state.getValue(GTBlockStateProperties.BLOOM);
    }

    public static boolean isInverted(CompoundTag tag) {
        return tag.getBoolean(TAG_INVERTED);
    }

    public static boolean isLightEnabled(CompoundTag tag) {
        return tag.getBoolean(TAG_LIT);
    }

    public static boolean isBloomEnabled(CompoundTag tag) {
        return tag.getBoolean(TAG_BLOOM);
    }

    public CompoundTag getTagFromState(BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_BLOOM, state.getValue(GTBlockStateProperties.BLOOM));
        tag.putBoolean(TAG_LIT, state.getValue(LIGHT));
        tag.putBoolean(TAG_INVERTED, state.getValue(INVERTED));
        return tag;
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
        super.createBlockStateDefinition(builder.add(INVERTED, GTBlockStateProperties.BLOOM, LIGHT, POWERED));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIGHT) && isLightActive(state) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean powered = state.getValue(POWERED);
            if (powered != level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.setValue(POWERED, !powered), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), state.getValue(LIGHT) ? 2 | 8 : 2);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target,
                                       BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        stack.setTag(getTagFromState(state));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (isInverted(tag))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.inverted"));
            if (!isBloomEnabled(tag))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_bloom"));
            if (!isLightEnabled(tag))
                tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_light"));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> returnValue = super.getDrops(state, params);
        for (ItemStack stack : returnValue) {
            if (stack.is(this.asItem())) {
                stack.setTag(this.getTagFromState(state));
                break;
            }
        }
        return returnValue;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return renderers.get(state);
    }
}
