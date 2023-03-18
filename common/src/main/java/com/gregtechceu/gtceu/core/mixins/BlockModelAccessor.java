package com.gregtechceu.gtceu.core.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote BlockModelMixin
 */
@Mixin(BlockModel.class)
public interface BlockModelAccessor {
    @Accessor Map<String, Either<Material, String>> getTextureMap();
}
