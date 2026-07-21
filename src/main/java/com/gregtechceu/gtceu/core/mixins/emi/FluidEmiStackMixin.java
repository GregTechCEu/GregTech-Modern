package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = FluidEmiStack.class, remap = false)
public abstract class FluidEmiStackMixin extends EmiStack {

    @Shadow
    @Final
    private Fluid fluid;

    @Shadow
    public abstract DataComponentPatch getComponentChanges();

    @Inject(method = "getTooltip",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiPort;getFluidRegistry()Lnet/minecraft/core/Registry;"),
            remap = false,
            require = 0)
    private void gtceu$addFluidTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> cir,
                                       @Local(name = "list") List<ClientTooltipComponent> list) {
        TooltipsHandler.appendFluidTooltips(new FluidStack(
                this.fluid.builtInRegistryHolder(),
                Math.max(GTMath.saturatedCast(this.getAmount()), 1),
                this.getComponentChanges()),
                text -> list.add(EmiTooltipComponents.of(text)),
                TooltipFlag.NORMAL,
                Item.TooltipContext.of(Minecraft.getInstance().level));
    }
}
