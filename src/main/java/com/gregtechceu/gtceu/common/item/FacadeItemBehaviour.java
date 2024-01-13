package com.gregtechceu.gtceu.common.item;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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
    public String getItemStackDisplayName(ItemStack itemStack) {
        ItemStack facadeStack = getFacadeStack(itemStack);
        String name = facadeStack.getItem().getDescriptionId(facadeStack);
        return LocalizationUtils.format(itemStack.getItem().getDescriptionId()) + "-" +LocalizationUtils.format(name);
    }

    @Override
    public void fillItemCategory(ComponentItem item, CreativeModeTab category, NonNullList<ItemStack> items) {
        List<ItemStack> validFacades = ImmutableList.of(new ItemStack(Blocks.STONE), GTBlocks.COIL_CUPRONICKEL.asStack(), new ItemStack(Blocks.GLASS));
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
        if (!itemStack.hasTag()) {
            itemStack.setTag(new CompoundTag());
        }
        var tagCompound = Objects.requireNonNull(itemStack.getTag());
        tagCompound.put("Facade", facadeStack.save(new CompoundTag()));
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
        var tagCompound = itemStack.getTag();
        if (tagCompound == null || !tagCompound.contains("Facade", Tag.TAG_COMPOUND)) {
            return null;
        }
        ItemStack facadeStack = ItemStack.of(tagCompound.getCompound("Facade"));
        if (facadeStack.isEmpty() || !isValidFacade(facadeStack)) {
            return null;
        }
        return facadeStack;
    }

}
