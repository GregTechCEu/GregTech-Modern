package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.client.TooltipsHandler;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;

import com.google.common.collect.Lists;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = FluidEmiStack.class, remap = false)
public class FluidEmiStackMixin {

    @Shadow
    @Final
    private Fluid fluid;

    @Inject(method = "getTooltip", at = @At("TAIL"), remap = false)
    private void gtceu$addFluidTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        List<Component> tooltips = Lists.newArrayList(Component.empty(), Component.empty());
        TooltipsHandler.appendFluidTooltips(this.fluid, ((EmiStack) (Object) this).getAmount(), tooltips,
                TooltipFlag.NORMAL);

        List<ClientTooltipComponent> list = cir.getReturnValue();
        tooltips.stream()
                .filter(component -> component.getContents() != ComponentContents.EMPTY)
                .map(component -> Map.entry(tooltips.indexOf(component),
                        ClientTooltipComponent.create(component.getVisualOrderText())))
                .forEach(component -> list.add(component.getKey(), component.getValue()));
    }
}
