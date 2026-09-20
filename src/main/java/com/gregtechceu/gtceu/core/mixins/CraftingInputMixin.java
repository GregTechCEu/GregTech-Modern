package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.CraftingGridOrigin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CraftingInput.class)
public class CraftingInputMixin implements CraftingGridOrigin.Access {
    @Unique
    private CraftingGridOrigin gtceu$gridOrigin;

    @Override
    public CraftingGridOrigin gtceu$getGridOrigin() {
        return gtceu$gridOrigin;
    }

    @Override
    public void gtceu$setGridOrigin(CraftingGridOrigin origin) {
        gtceu$gridOrigin = origin;
    }

    @Inject(method = "ofPositioned", at = @At("RETURN"))
    private static void gtceu$rememberOriginalGrid(int width, int height, List<ItemStack> items,
                                                  CallbackInfoReturnable<CraftingInput.Positioned> cir) {
        var positioned = cir.getReturnValue();
        // EMPTY is a shared singleton. Never attach per-grid state to it.
        if (!positioned.input().isEmpty()) {
            ((CraftingGridOrigin.Access) positioned.input()).gtceu$setGridOrigin(
                    new CraftingGridOrigin(width, height, positioned.left(), positioned.top()));
        }
    }
}
