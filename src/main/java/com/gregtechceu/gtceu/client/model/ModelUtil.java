package com.gregtechceu.gtceu.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote ModelUtil
 */
@OnlyIn(Dist.CLIENT)
public class ModelUtil {

    @ExpectPlatform
    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        throw new AssertionError();
    }
}
