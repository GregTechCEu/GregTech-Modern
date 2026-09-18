package brachy.modularui.core.mixins.client;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.screen.IClickableContainerScreen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements IClickableContainerScreen {

    @Shadow
    protected Slot hoveredSlot;

    @Unique
    private Slot modularui$clickedSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "findSlot", at = @At("HEAD"), cancellable = true)
    public void modularui$getSlot(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
        if (this.modularui$clickedSlot != null) {
            cir.setReturnValue(this.modularui$clickedSlot);
        } else if (this instanceof IMuiScreen) {
            cir.setReturnValue(this.hoveredSlot);
        }
    }

    @Override
    public void modularui$setClickedSlot(Slot slot) {
        this.modularui$clickedSlot = slot;
    }

    @Override
    public Slot modularui$getClickedSlot() {
        return modularui$clickedSlot;
    }
}
