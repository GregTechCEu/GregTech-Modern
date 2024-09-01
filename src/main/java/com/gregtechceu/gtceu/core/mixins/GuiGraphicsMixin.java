package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.client.renderer.item.LampItemOverlayRenderer;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    // Prevents recursion in the hook below
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                     value = "FIELD",
                     target = "Lnet/minecraft/client/gui/GuiGraphics;minecraft:Lnet/minecraft/client/Minecraft;",
                     shift = At.Shift.BEFORE,
                     ordinal = 0))
    private void gtceu$renderCustomItemDecorations(Font font, ItemStack stack, int x, int y, String text,
                                                   CallbackInfo ci) {
        if (stack.getItem() instanceof IGTTool toolItem) {
            ToolChargeBarRenderer.renderBarsTool((GuiGraphics) (Object) this, toolItem, stack, x, y);
        } else if (stack.getItem() instanceof IComponentItem componentItem) {
            ToolChargeBarRenderer.renderBarsItem((GuiGraphics) (Object) this, componentItem, stack, x, y);
        } else if (stack.getItem() instanceof LampBlockItem) {
            LampItemOverlayRenderer.renderOverlay((GuiGraphics) (Object) this, stack, x, y);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    protected void gtceu$renderItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack, int x,
                                    int y, int seed, int z, CallbackInfo ci) {
        var self = (GuiGraphics) (Object) this;
        var minecraft = Minecraft.getInstance();

        Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
        if (Screen.hasShiftDown() && researchData != null) {
            Collection<GTRecipe> recipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
            if (recipes != null && !recipes.isEmpty()) {
                for (var recipe : recipes) {
                    ItemStack output = ItemRecipeCapability.CAP
                            .of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];

                    if (!output.isEmpty() && output != stack) {
                        renderInstead(self, livingEntity, level, output, x, y, seed, z);
                        ci.cancel();
                    }
                }
            }
        }
    }

    private static void renderInstead(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity,
                                      @Nullable Level level, ItemStack stack, int x, int y, int seed, int z) {
        OVERRIDING_FOR.set(stack);
        try {
            guiGraphics.renderItem(livingEntity, level, stack, x, y, seed, z);
        } finally {
            OVERRIDING_FOR.remove();
        }
    }
}
