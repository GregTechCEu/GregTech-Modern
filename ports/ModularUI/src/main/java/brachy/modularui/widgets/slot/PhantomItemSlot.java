package brachy.modularui.widgets.slot;

import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhantomItemSlot extends ItemSlot implements GhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSyncHandler syncHandler;

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof PhantomItemSlotSyncHandler;
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(PhantomItemSlotSyncHandler.class);
    }

    @Override
    protected void drawOverlay(ModularGuiContext context) {
        RecipeViewerHandler handler = RecipeViewerHandler.getCurrent();
        if (handler.isHoveringOver(this)) {
            drawHighlight(context, getArea(), isHovering());
        } else {
            super.drawOverlay(context);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        MouseData mouseData = MouseData.create(button);
        this.syncHandler.syncToServer(PhantomItemSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseReleased(int button) {
        return true;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        MouseData mouseData = MouseData.create(-1);
        this.syncHandler.syncToServer(PhantomItemSlotSyncHandler.SYNC_SCROLL, buf -> {
            mouseData.writeToPacket(buf);
            buf.writeDouble(scrollX);
            buf.writeDouble(scrollY);
        });
        return true;
    }

    @Override
    public void onMouseDrag(int button, double dragX, double dragY) {
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
    public PhantomItemSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    @Override
    public PhantomItemSlot slot(ModularSlot slot) {
        return syncHandler(new PhantomItemSlotSyncHandler(slot));
    }

    @Override
    public PhantomItemSlot syncHandler(ItemSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
