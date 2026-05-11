package com.gregtechceu.gtceu.core.mixins.jei;

import com.gregtechceu.gtceu.client.TooltipsHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.neoforge.platform.FluidHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = FluidHelper.class, remap = false)
public class FluidHelperMixin {

    @Inject(method = "getTooltip(Ljava/util/List;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0)
    private void gtceu$injectFluidTooltips(List<Component> tooltip, FluidStack ingredient, TooltipFlag tooltipFlag,
                                           CallbackInfo ci) {
        TooltipsHandler.appendFluidTooltips(ingredient, tooltip::add, tooltipFlag,
                Item.TooltipContext.of(Minecraft.getInstance().level));
    }
}
