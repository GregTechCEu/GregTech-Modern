package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import com.gregtechceu.gtceu.api.item.datacomponents.FacadeWrapper;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote FacadeItem
 */
public class FacadeItemBehaviour implements ISubItemHandler, ICustomDescriptionId, ICustomRenderer {

    @NotNull
    @Override
    public IRenderer getRenderer() {
        return FacadeCoverRenderer.INSTANCE;
    }

    @Override
    public @Nullable Component getItemName(ItemStack stack) {
        BlockState facadeState = getFacadeState(stack);
        return Component.translatable(stack.getDescriptionId(), facadeState.getBlock().getName());
    }

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        List<BlockState> validFacades = ImmutableList.of(Blocks.STONE.defaultBlockState(),
                GTBlocks.COIL_CUPRONICKEL.getDefaultState(), Blocks.GLASS.defaultBlockState());
        for (BlockState facadeStack : validFacades) {
            ItemStack resultStack = item.getDefaultInstance();
            setFacadeStack(resultStack, facadeStack);
            items.add(resultStack);
        }
    }

    public static void setFacadeStack(ItemStack itemStack, ItemStack facadeStack) {
        BlockState state;
        if (!isValidFacade(facadeStack)) {
            state = Blocks.STONE.defaultBlockState();
        } else {
            state = ((BlockItem) facadeStack.getItem()).getBlock().defaultBlockState();
        }
        itemStack.set(GTDataComponents.FACADE, new FacadeWrapper(state));
    }

    public static void setFacadeStack(ItemStack itemStack, BlockState state) {
        if (!isValidFacade(state)) {
            state = Blocks.STONE.defaultBlockState();
        }
        itemStack.set(GTDataComponents.FACADE, new FacadeWrapper(state));
    }

    public static boolean isValidFacade(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        var rawBlockState = blockItem.getBlock().defaultBlockState();
        return !rawBlockState.hasBlockEntity() && rawBlockState.getRenderShape() == RenderShape.MODEL;
    }

    public static boolean isValidFacade(BlockState state) {
        return !state.hasBlockEntity() && state.getRenderShape() == RenderShape.MODEL;
    }

    public static BlockState getFacadeState(ItemStack itemStack) {
        BlockState unsafeState = getFacadeStackUnsafe(itemStack);
        if (unsafeState == null) {
            return Blocks.STONE.defaultBlockState();
        }
        return unsafeState;
    }

    @Nullable
    private static BlockState getFacadeStackUnsafe(ItemStack itemStack) {
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
