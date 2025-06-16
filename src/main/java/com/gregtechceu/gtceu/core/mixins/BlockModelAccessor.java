package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

    @Accessor("elements")
    List<BlockElement> gtceu$getRawElements();
}
