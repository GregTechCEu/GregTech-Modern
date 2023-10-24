package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(FluidEmiStack.class)
public class FluidEmiStackMixin {

    @Shadow @Final private Fluid fluid;

    @Inject(method = "getTooltip", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void gtceu$addFluidTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> cir, List<ClientTooltipComponent> list, String namespace, String var3) {
        List<Component> tooltips = new ArrayList<>();
        TooltipsHandler.appendFluidTooltips(this.fluid, tooltips, TooltipFlag.NORMAL);
        tooltips.stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).forEachOrdered(list::add);
    }
}
