package com.gregtechceu.gtceu.api.block;


import com.gregtechceu.gtceu.api.item.VariantBlockItem;
import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote VariantBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariantBlock<T extends Enum<T> & VariantBlock.AppendableStringRepresentable> extends Block {
    @Getter
    protected EnumProperty<T> variantProperty;
    @Getter
    protected T[] variantValues;

    public VariantBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(variantProperty, variantValues[0]));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        Class<T> enumClass = GTUtil.getActualTypeParameter(getClass(), 0);
        this.variantProperty = EnumProperty.create("variant", enumClass);
        this.variantValues = enumClass.getEnumConstants();
        builder.add(variantProperty);
    }

    public BlockState changeVariant(BlockState state, T variant) {
        if (state.is(this)) {
            return state.setValue(variantProperty, variant);
        }
        return state;
    }

    public BlockState getState(T variant) {
        return changeVariant(defaultBlockState(), variant);
    }

    public ItemStack getItemVariant(T variant) {
        return getItemVariant(variant, 1);
    }

    public ItemStack getItemVariant(T variant, int count) {
        var itemStack = this.asItem().getDefaultInstance();
        if (this.asItem() instanceof VariantBlockItem variantBlockItem) {
            variantBlockItem.setVariant(itemStack, variant);
            itemStack.setCount(count);
        }
        return itemStack;
    }

    public T getVariant(BlockState blockState) {
        return blockState.getValue(variantProperty);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var item = context.getItemInHand();
        if (item.getItem() instanceof VariantBlockItem<?, ?> variantBlockItem) {
            T variant = (T) variantBlockItem.getVariant(item);
            if (variant != null) {
                return getState(variant);
            }
        }
        return super.getStateForPlacement(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        var item = super.getCloneItemStack(level, pos, state);
        if (item.getItem() instanceof VariantBlockItem variantBlockItem && state.is(this)) {
            variantBlockItem.setVariant(item, state.getValue(variantProperty));
        }
        return item;
    }

    public void appendHoverText(@Nullable T variant, ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return List.of(getItemVariant(getVariant(state)));
    }

    public interface AppendableStringRepresentable extends StringRepresentable{
         default boolean hasLangAppendage() { return true;}
    }
}
