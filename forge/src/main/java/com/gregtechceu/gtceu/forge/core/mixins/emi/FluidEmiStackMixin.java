package com.gregtechceu.gtceu.forge.core.mixins.emi;

import com.google.common.collect.Lists;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(FluidEmiStack.class)
public class FluidEmiStackMixin {

    @Shadow @Final private Fluid fluid;

    @Inject(method = "getTooltip", at = @At("TAIL"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private void gtceu$addFluidTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> cir, List<ClientTooltipComponent> list, String namespace) {
        List<Component> tooltips = Lists.newArrayList(Component.empty(), Component.empty());
        TooltipsHandler.appendFluidTooltips(this.fluid, tooltips, TooltipFlag.NORMAL);
        tooltips.stream()
                .filter(component -> component.getContents() != ComponentContents.EMPTY)
                .map(component -> Map.entry(tooltips.indexOf(component), ClientTooltipComponent.create(component.getVisualOrderText())))
                .forEach(component -> list.add(component.getKey(), component.getValue()));
    }
}
