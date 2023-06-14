package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

/**
 * @author Screret
 * @date 2023/6/9
 * @implNote IWorldGenLayer
 */
public interface IWorldGenLayer extends StringRepresentable {
    Codec<IWorldGenLayer> CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, WorldGeneratorUtils.WORLD_GEN_LAYERS::get);

    RuleTest getTarget();
}
