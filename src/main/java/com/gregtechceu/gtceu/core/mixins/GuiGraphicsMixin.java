package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    // Prevents recursion in the hook below
    @Unique
    private static final ThreadLocal<ItemStack> GTCEU$OVERRIDING_FOR = new ThreadLocal<>();

    @Shadow
    private void renderItem(@Nullable LivingEntity entity, @Nullable Level level, ItemStack stack, int x, int y,
                            int seed, int guiOffset) {
        throw new AssertionError();
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    protected void gtceu$renderItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack,
                                    int x, int y, int seed, int z, CallbackInfo ci) {
        if (GTCEU$OVERRIDING_FOR.get() != null || !Screen.hasShiftDown()) {
            return;
        }

        ResearchManager.ResearchItem researchData = ResearchManager.readResearchId(stack);
        if (researchData != null) {
            Collection<GTRecipe> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
            if (recipes != null && !recipes.isEmpty()) {
                for (var recipe : recipes) {
                    ItemStack output = ItemRecipeCapability.CAP
                            .of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];

                    if (!output.isEmpty() && !ItemStack.isSameItemSameTags(output, stack)) {
                        gtceu$renderInstead(livingEntity, level, output, x, y, seed, z);
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }

    @Unique
    private void gtceu$renderInstead(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack,
                                     int x, int y, int seed, int z) {
        GTCEU$OVERRIDING_FOR.set(stack);
        try {
            this.renderItem(livingEntity, level, stack, x, y, seed, z);
        } finally {
            GTCEU$OVERRIDING_FOR.remove();
        }
    }
}
