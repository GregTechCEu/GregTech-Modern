package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipeHandler;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EmiRecipeFiller.class)
public class EmiRecipeHandlerMixin {
    @Inject(method = "getAllHandlers", at = @At("HEAD"), cancellable = true)
    private static <T extends AbstractContainerMenu> void AddGTEmiRecipeHandler(AbstractContainerScreen<T> screen, CallbackInfoReturnable<List<EmiRecipeHandler<T>>> cir){
        if (screen != null) {
            if (screen.getMenu() instanceof ModularUIContainer) {
                cir.setReturnValue((List<EmiRecipeHandler<T>>) (List<?>) List.of(new GTEmiRecipeHandler()));
            }
        }
    }
}
