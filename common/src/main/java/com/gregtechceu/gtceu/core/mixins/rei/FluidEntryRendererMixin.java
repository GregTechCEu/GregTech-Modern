package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(FluidEntryDefinition.FluidEntryRenderer.class)
public class FluidEntryRendererMixin {

    @Inject(method = "getTooltip", at = @At(value = "TAIL", shift = At.Shift.BEFORE), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private void gtceu$addMaterialTooltip(EntryStack<FluidStack> entry, TooltipContext context, CallbackInfoReturnable<@Nullable Tooltip> cir,
                                          List<Component> tooltip, long amount) {
        TooltipsHandler.appendFluidTooltips(entry.getValue().getFluid(), tooltip, context.getFlag());
    }
}
