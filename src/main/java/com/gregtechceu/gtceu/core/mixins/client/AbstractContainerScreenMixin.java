package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.client.mui.screen.IClickableContainerScreen;

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
    private Slot gtceu$clickedSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "findSlot", at = @At("HEAD"), cancellable = true)
    public void getSlot(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
        if (this.gtceu$clickedSlot != null) {
            cir.setReturnValue(this.gtceu$clickedSlot);
        } else if (IMuiScreen.class.isAssignableFrom(this.getClass())) {
            cir.setReturnValue(this.hoveredSlot);
        }
    }

    @Override
    public void gtceu$setClickedSlot(Slot slot) {
        this.gtceu$clickedSlot = slot;
    }

    @Override
    public Slot gtceu$getClickedSlot() {
        return gtceu$clickedSlot;
    }
}
