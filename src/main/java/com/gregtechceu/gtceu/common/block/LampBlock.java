package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.client.renderer.block.LampRenderer;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

public class LampBlock extends Block implements IBlockRendererProvider {

    public static final BooleanProperty BLOOM = BooleanProperty.create("bloom");
    public static final BooleanProperty LIGHT = BlockStateProperties.LIT;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

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
                .setValue(BLOOM, true)
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

    public boolean isInverted(BlockState state) {
        return state.getValue(INVERTED);
    }

    public boolean isLightEnabled(BlockState state) {
        return state.getValue(LIGHT);
    }

    public boolean isBloomEnabled(BlockState state) {
        return state.getValue(BLOOM);
    }

    public LampBlockItem.LampData getDataFromState(BlockState state) {
        return new LampBlockItem.LampData(state.getValue(INVERTED), state.getValue(BLOOM), state.getValue(LIGHT));
    }

    public ItemStack getStackFromIndex(int i) {
        ItemStack stack = new ItemStack(this);
        LampBlockItem.LampData data = new LampBlockItem.LampData((i & LampBlock.INVERTED_FLAG) == 0,
                (i & LampBlock.BLOOM_FLAG) == 0,
                (i & LampBlock.LIGHT_FLAG) == 0);
        stack.set(GTDataComponents.LAMP_DATA, data);
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
            boolean powered = state.getValue(POWERED);
            if (powered != level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.setValue(POWERED, !powered), state.getValue(LIGHT) ? 2 | 8 : 2);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), state.getValue(LIGHT) ? 2 | 8 : 2);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
                                       Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        stack.set(GTDataComponents.LAMP_DATA, getDataFromState(state));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> tooltip,
                                TooltipFlag flag) {
        LampBlockItem.LampData data = stack.getOrDefault(GTDataComponents.LAMP_DATA, LampBlockItem.LampData.EMPTY);
        if (data.inverted())
            tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.inverted"));
        if (!data.bloom())
            tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_bloom"));
        if (!data.lit())
            tooltip.add(Component.translatable("block.gtceu.lamp.tooltip.no_light"));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> returnValue = super.getDrops(state, params);
        for (ItemStack stack : returnValue) {
            if (stack.is(this.asItem())) {
                stack.set(GTDataComponents.LAMP_DATA, this.getDataFromState(state));
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
