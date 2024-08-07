package com.gregtechceu.gtceu.core.mixins.jei;

import com.gregtechceu.gtceu.client.TooltipsHandler;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.forge.platform.FluidHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FluidHelper.class)
public class FluidHelperMixin {

    @Inject(method = "getTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"), remap = false)
    private void gtceu$injectFluidTooltips(ITooltipBuilder tooltip, FluidStack ingredient, TooltipFlag tooltipFlag, CallbackInfo ci) {
        TooltipsHandler.appendFluidTooltips(ingredient.getFluid(), ingredient.getAmount(), tooltip::add, tooltipFlag);
    }
}
