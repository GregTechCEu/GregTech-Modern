package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
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

    public static void setFacadeState(ItemStack itemStack, BlockState facadeState) {
        if (!isValidFacade(facadeState)) {
            facadeState = Blocks.STONE.defaultBlockState();
        }
        var tagCompound = itemStack.getOrCreateTag();
        Tag stateTag = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, facadeState)
                .result().orElse(new CompoundTag());
        tagCompound.put("Facade", stateTag);
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

        BlockState unsafeState = getFacadeStateUnsafe(itemStack);
        if (unsafeState == null) {
            // backwards support
            ItemStack unsafeStack = getFacadeStackUnsafe(itemStack);
            if (unsafeStack.getItem() instanceof BlockItem blockItem) {
                return blockItem.getBlock().defaultBlockState();
            }

            return null;
        }
        return unsafeState;
    }

    @Nullable
    private static BlockState getFacadeStateUnsafe(ItemStack itemStack) {
        var tagCompound = itemStack.getTag();
        if (tagCompound == null || !tagCompound.contains("Facade", Tag.TAG_COMPOUND)) {
            return null;
        }
        return BlockState.CODEC.parse(NbtOps.INSTANCE, tagCompound.getCompound("Facade"))
                .result().orElse(null);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @NotNull
    private static ItemStack getFacadeStackUnsafe(ItemStack itemStack) {
        var tagCompound = itemStack.getTag();
        if (tagCompound == null || !tagCompound.contains("Facade", Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        ItemStack facadeStack = ItemStack.of(tagCompound.getCompound("Facade"));
        if (facadeStack.isEmpty() || !isValidFacade(facadeStack)) {
            return ItemStack.EMPTY;
        }
        return facadeStack;
    }
}
