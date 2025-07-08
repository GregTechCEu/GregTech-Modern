package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void gtceu$renderResearchItemContent(@Nullable LivingEntity entity, @Nullable Level level,
                                                 ItemStack stack, int x, int y, int seed, int z,
                                                 Operation<Void> original) {
        if (!RenderUtil.renderResearchItemContent((GuiGraphics) (Object) this, original,
                entity, level, stack, x, y, z, seed)) {
            original.call(entity, level, stack, x, y, seed, z);
        }
    }
}
