package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor
    void setImageWidth(int v);

    @Accessor
    void setImageHeight(int v);

    @Accessor
    void setLeftPos(int v);

    @Accessor
    void setTopPos(int v);

    @Accessor
    void setHoveredSlot(Slot slot);

    @Accessor
    Slot getHoveredSlot();

    @Accessor
    Slot getClickedSlot();

    @Accessor
    ItemStack getDraggingItem();

    @Accessor
    boolean getIsSplittingStack();

    @Accessor
    boolean getIsQuickCrafting();

    @Accessor
    Set<Slot> getQuickCraftSlots();

    @Accessor
    int getQuickCraftingType();

    @Invoker
    void invokeRecalculateQuickCraftRemaining();

    @Accessor
    int getQuickCraftingRemainder();

    @Accessor
    ItemStack getSnapbackItem();

    @Accessor
    void setSnapbackItem(ItemStack stack);

    @Accessor
    Slot getSnapbackEnd();

    @Accessor
    int getSnapbackStartX();

    @Accessor
    int getSnapbackStartY();

    @Accessor
    long getSnapbackTime();

    @Invoker
    void invokeRenderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY);

    @Invoker
    void invokeRenderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY);
}
