package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/// Have to do the ModifyArg calls separately, thanks forge.
/// see [Connector#383](https://github.com/Sinytra/Connector/discussions/383) for an explanation.
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>,
        A extends HumanoidModel<T>> extends RenderLayer<T, M> {

    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @ModifyArg(method = "renderArmorPiece",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel" +
                                "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
                        remap = false),
               index = 6)
    private float gtceu$modifyArmorTintR(float oldR, @Local ArmorItem armorItem) {
        if (armorItem instanceof GTArmorItem gtArmorItem) {
            int argb = gtArmorItem.material.getMaterialARGB();
            float r = FastColor.ARGB32.red(argb) / 255.0F;

            if (oldR != 1.0f) {
                return (r + oldR) / 2.0f;
            } else {
                return r;
            }
        }
        return oldR;
    }

    @ModifyArg(method = "renderArmorPiece",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel" +
                                "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
                        remap = false),
               index = 7)
    private float gtceu$modifyArmorTintsG(float oldG, @Local ArmorItem armorItem) {
        if (armorItem instanceof GTArmorItem gtArmorItem) {
            int argb = gtArmorItem.material.getMaterialARGB();
            float g = FastColor.ARGB32.green(argb) / 255.0F;

            if (oldG != 1.0f) {
                return (g + oldG) / 2.0f;
            } else {
                return g;
            }
        }
        return oldG;
    }

    @ModifyArg(method = "renderArmorPiece",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel" +
                                "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
                        remap = false),
               index = 8)
    private float gtceu$modifyArmorTintsB(float oldB, @Local ArmorItem armorItem) {
        if (armorItem instanceof GTArmorItem gtArmorItem) {
            int argb = gtArmorItem.material.getMaterialARGB();
            float b = FastColor.ARGB32.blue(argb) / 255.0F;

            if (oldB != 1.0f) {
                return (b + oldB) / 2.0f;
            } else {
                return b;
            }
        }
        return oldB;
    }
}
