package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.CompassNode;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CompassManager.class, remap = false)
public abstract class CompassManagerMixin {

    @Shadow
    public abstract List<CompassNode> getNodesByItem(Item item);

    @Inject(method = "hasCompass", at = @At("HEAD"), cancellable = true)
    private void gtceu$disableCompass(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigHolder.INSTANCE.gameplay.enableCompass && getNodesByItem(item).stream()
                .anyMatch(node -> node.getNodeName().getNamespace().equals(GTCEu.MOD_ID))) {
            cir.setReturnValue(false);
        }
    }
}
