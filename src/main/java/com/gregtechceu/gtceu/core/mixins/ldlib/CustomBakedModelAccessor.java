package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;

import net.minecraft.client.resources.model.BakedModel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CustomBakedModel.class, remap = false)
public interface CustomBakedModelAccessor {

    @Accessor("parent")
    BakedModel gtceu$getParent();
}
