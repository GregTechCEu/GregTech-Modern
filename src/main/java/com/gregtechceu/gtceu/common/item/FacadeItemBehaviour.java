package com.gregtechceu.gtceu.common.item;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.datacomponents.FacadeWrapper;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;

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
        ItemStack facadeStack = getFacadeStack(stack);
        return Component.translatable(stack.getDescriptionId(), facadeStack.getHoverName());
    }

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        List<ItemStack> validFacades = ImmutableList.of(new ItemStack(Blocks.STONE),
                GTBlocks.COIL_CUPRONICKEL.asStack(), new ItemStack(Blocks.GLASS));
        for (ItemStack facadeStack : validFacades) {
            ItemStack resultStack = item.getDefaultInstance();
            setFacadeStack(resultStack, facadeStack);
            items.add(resultStack);
        }
    }

    public static void setFacadeStack(ItemStack itemStack, ItemStack facadeStack) {
        facadeStack = facadeStack.copy();
        facadeStack.setCount(1);
        if (!isValidFacade(facadeStack)) {
            facadeStack = new ItemStack(Blocks.STONE);
        }
        itemStack.set(GTDataComponents.FACADE, new FacadeWrapper(facadeStack));
    }

    public static boolean isValidFacade(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        var rawBlockState = blockItem.getBlock().defaultBlockState();
        return !rawBlockState.hasBlockEntity() && rawBlockState.getRenderShape() == RenderShape.MODEL;
    }

    public static ItemStack getFacadeStack(ItemStack itemStack) {
        ItemStack unsafeStack = getFacadeStackUnsafe(itemStack);
        if (unsafeStack == null) {
            return new ItemStack(Blocks.STONE);
        }
        return unsafeStack;
    }

    @Nullable
    private static ItemStack getFacadeStackUnsafe(ItemStack itemStack) {
        var facade = itemStack.get(GTDataComponents.FACADE);
        if (facade == null) {
            return null;
        }
        ItemStack facadeStack = facade.stack();
        if (facadeStack.isEmpty() || !isValidFacade(facadeStack)) {
            return null;
        }
        return facadeStack;
    }
}
