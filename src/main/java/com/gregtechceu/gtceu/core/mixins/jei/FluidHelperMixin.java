package com.gregtechceu.gtceu.core.mixins.jei;

import com.gregtechceu.gtceu.client.TooltipsHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mezz.jei.neoforge.platform.FluidHelper;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = FluidHelper.class, remap = false)
public class FluidHelperMixin {

    @WrapMethod(method = "getTooltip(Ljava/util/List;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V",
                remap = false,
                require = 0)
    private void gtceu$injectFluidTooltips(List<Component> tooltip, FluidStack ingredient, TooltipFlag tooltipFlag,
                                           Operation<Void> original) {
        original.call(tooltip, ingredient, tooltipFlag);
        TooltipsHandler.appendFluidTooltips(ingredient, tooltip::add, tooltipFlag, Item.TooltipContext.of(Minecraft.getInstance().level));
    }
}
