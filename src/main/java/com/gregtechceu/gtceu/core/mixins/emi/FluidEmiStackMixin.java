package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidEmiStack.class, remap = false)
public class FluidEmiStackMixin {

    @Shadow
    @Final
    private Fluid fluid;
    @Shadow
    @Final
    private CompoundTag nbt;

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void gtceu$drawFreezingIndicator(GuiGraphics graphics, int x, int y, float delta, int flags,
                                             CallbackInfo ci) {
        if ((flags & EmiStack.RENDER_ICON) == 0) return;
        var material = ChemicalHelper.getMaterial(fluid);
        if (material == null || !material.requiresMetalFreezing() ||
                !fluid.isSame(material.getFluid(FluidStorageKeys.LIQUID)))
            return;
        graphics.pose().pushPose();
        graphics.pose().translate(x + 11, y, 200);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);
        graphics.blit(GTCEu.id("textures/gui/fluid_overlay/flame.png"), 0, 0, 0, 0, 8, 8, 8, 8);
        graphics.pose().popPose();
    }

    @Inject(method = "getTooltip",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiPort;getFluidRegistry()Lnet/minecraft/core/Registry;"),
            remap = false,
            require = 0)
    private void gtceu$addFluidTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> cir,
                                       @Local(ordinal = 0) List<ClientTooltipComponent> list) {
        TooltipsHandler.appendFluidTooltips(new FluidStack(this.fluid,
                Math.max(GTMath.saturatedCast(((EmiStack) (Object) this).getAmount()), 1),
                nbt),
                text -> list.add(EmiTooltipComponents.of(text)),
                TooltipFlag.NORMAL);
    }
}
