package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;

public class AEItemCompat {

    public static final IGridLinkableHandler TERMINAL_LINKABLE_HANDLER = new LinkableHandler();

    public static void init() {
        GridLinkables.register(GTItems.TERMINAL.get(), TERMINAL_LINKABLE_HANDLER);
    }

    private static class LinkableHandler implements IGridLinkableHandler {

        @Override
        public boolean canLink(ItemStack stack) {
            if (stack.getItem() instanceof ComponentItem compItem) {
                for (IItemComponent comp : compItem.getComponents()) {
                    if (comp instanceof TerminalBehavior) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
                    .result()
                    .ifPresent(tag -> itemStack.getOrCreateTag().put(TerminalBehavior.ACCESS_POINT_TAG, tag));
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.removeTagKey(TerminalBehavior.ACCESS_POINT_TAG);
        }
    }
}
