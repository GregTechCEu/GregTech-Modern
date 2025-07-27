package com.gregtechceu.gtceu.core.mixins.forge;

import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = BlockStateProvider.ConfiguredModelList.class, remap = false)
public interface ConfiguredModelListAccessor {

    @Accessor("models")
    List<ConfiguredModel> gtceu$getModels();
}
