package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsMixin {

    @WrapMethod(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V")
    private void gtceu$renderResearchItemContent(@Nullable LivingEntity entity, @Nullable Level level,
                                                 ItemStack stack, int x, int y, int seed,
                                                 Operation<Void> original) {
        Operation<Void> oldRenderItemOperation = args -> original.call(args[0], args[1], args[2], args[3], args[4],
                args[5]);
        if (!RenderUtil.renderResearchItemContent((GuiGraphicsExtractor) (Object) this, oldRenderItemOperation,
                entity, level, stack, x, y, 0, seed)) {
            original.call(entity, level, stack, x, y, seed);
        }
    }
}
