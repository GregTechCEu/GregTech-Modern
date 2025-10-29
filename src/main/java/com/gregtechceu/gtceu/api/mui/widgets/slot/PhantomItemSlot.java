package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.PhantomItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.xei.handlers.RecipeViewerHandler;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhantomItemSlot extends ItemSlot implements GhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSH syncHandler;

    @Override
    public void onInit() {
        super.onInit();
        getContext().getXeiSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, PhantomItemSlotSH.class);
        return this.syncHandler != null && super.isValidSyncHandler(syncHandler);
    }

    @Override
    protected void drawOverlay(ModularGuiContext context) {
        RecipeViewerHandler handler = RecipeViewerHandler.getCurrent();
        if (handler.isHoveringOver(this)) {
            RenderSystem.colorMask(true, true, true, false);
            drawHighlight(context, getArea(), isHovering());
            RenderSystem.colorMask(true, true, true, true);
        } else {
            super.drawOverlay(context);
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        MouseData mouseData = MouseData.create(button);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        MouseData mouseData = MouseData.create((int) delta);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_SCROLL, mouseData::writeToPacket);
        return true;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // TODO custom drag impl
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        this.syncHandler.updateFromClient(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return areAncestorsEnabled() &&
                this.syncHandler.isPhantom() &&
                ingredient instanceof ItemStack itemStack &&
                this.syncHandler.isItemValid(itemStack) ? itemStack : null;
    }

    @Override
    @NotNull
    public PhantomItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    @Override
    public PhantomItemSlot slot(ModularSlot slot) {
        ((Slot) slot).index = -1;
        this.syncHandler = new PhantomItemSlotSH(slot);
        super.isValidSyncHandler(this.syncHandler);
        setSyncHandler(this.syncHandler);
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
