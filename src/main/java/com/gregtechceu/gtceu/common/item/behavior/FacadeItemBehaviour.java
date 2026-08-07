package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import com.gregtechceu.gtceu.api.item.datacomponents.FacadeWrapper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FacadeItemBehaviour implements ISubItemHandler, ICustomDescriptionId {

    @Override
    public @Nullable Component getItemName(ItemStack stack) {
        BlockState facadeState = getFacadeState(stack);
        return Component.translatable(stack.getDescriptionId(), facadeState.getBlock().getName());
    }

    public static final Supplier<List<BlockState>> DEFAULT_FACADES = GTMemoizer.memoize(() -> {
        List<BlockState> states = new ArrayList<>();
        states.add(Blocks.STONE.defaultBlockState());
        states.add(GTBlocks.COIL_CUPRONICKEL.getDefaultState());
        states.add(Blocks.GLASS.defaultBlockState());

        return states;
    });

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        for (BlockState facadeState : DEFAULT_FACADES.get()) {
            ItemStack resultStack = item.getDefaultInstance();
            setFacadeState(resultStack, facadeState);
            items.add(resultStack);
        }
    }

    public static void setFacadeState(ItemStack itemStack, BlockState state) {
        if (!isValidFacade(state)) {
            state = Blocks.STONE.defaultBlockState();
        }
        itemStack.set(GTDataComponents.FACADE, new FacadeWrapper(state));
    }

    public static boolean isValidFacade(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        return isValidFacade(blockItem.getBlock().defaultBlockState());
    }

    public static boolean isValidFacade(BlockState state) {
        return !state.hasBlockEntity() && state.getRenderShape() == RenderShape.MODEL;
    }

    public static BlockState getFacadeState(ItemStack itemStack) {
        BlockState nullableState = getFacadeStateNullable(itemStack);
        if (nullableState == null) {
            return Blocks.STONE.defaultBlockState();
        }
        return nullableState;
    }

    public static BlockState getFacadeStateNullable(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }

        return getFacadeStateUnsafe(itemStack);
    }

    @Nullable
    private static BlockState getFacadeStateUnsafe(ItemStack itemStack) {
        var facade = itemStack.get(GTDataComponents.FACADE);
        if (facade == null) {
            return null;
        }
        BlockState facadeStack = facade.state();
        if (facadeStack.isEmpty() || !isValidFacade(facadeStack)) {
            return null;
        }
        return facadeStack;
    }
}
