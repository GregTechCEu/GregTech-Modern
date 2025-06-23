package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.gregtechceu.gtceu.client.model.CTMBakedModel;

import com.lowdragmc.lowdraglib.client.forge.ClientProxyImpl;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(value = ClientProxyImpl.class, remap = false)
public class ClientProxyImplMixin {

    // This mixin exists to replace LDLib's CTM model implementation with something we can still modify.
    @WrapOperation(method = "modelBake",
                   at = @At(value = "INVOKE",
                            target = "Ljava/util/Map$Entry;setValue(Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object gtceu$swapCtmModel(Map.Entry<ResourceLocation, BakedModel> instance,
                                     Object value, Operation<Object> original) {
        if (value instanceof BakedModel bakedModel) {
            return new CTMBakedModel<>(bakedModel);
        }
        return original.call(instance, value);
    }
}
