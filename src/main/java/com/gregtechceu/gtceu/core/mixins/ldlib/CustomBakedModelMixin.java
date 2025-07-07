package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.model.forge.CustomBakedModelImpl;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.POS;
import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.WORLD;

@Mixin(value = CustomBakedModelImpl.class, remap = false)
public abstract class CustomBakedModelMixin extends CustomBakedModel {

    private CustomBakedModelMixin(BakedModel parent) {
        super(parent);
    }

    /**
     * @author screret
     * @reason overwrite to include the parent model's model data instead of skipping it entirely
     */
    @Overwrite
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        BakedModel parentModel = ((CustomBakedModelAccessor) this).gtceu$getParent();
        modelData = parentModel.getModelData(level, pos, state, modelData);
        return modelData.derive()
                .with(WORLD, level)
                .with(POS, pos)
                .build();
    }

    /**
     * @author screret
     * @reason return the parent model's quad list as is instead of an empty one if the model properties aren't set
     */
    @Overwrite
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand,
                                             @NotNull ModelData modelData, RenderType renderType) {
        BlockAndTintGetter level = modelData.get(WORLD);
        BlockPos pos = modelData.get(POS);
        if (level != null && pos != null && state != null) {
            return gtceu$getCustomQuads(level, pos, state, side, rand, modelData, renderType);
        } else {
            BakedModel parent = ((CustomBakedModelAccessor) this).gtceu$getParent();
            return parent.getQuads(state, side, rand, modelData, renderType);
        }
    }

    /**
     * @author screret
     * @reason use forge's function with the proper arguments for getting the parent model's quads
     */
    @Unique
    public @NotNull List<BakedQuad> gtceu$getCustomQuads(@NotNull BlockAndTintGetter level,
                                                         @NotNull BlockPos pos, @NotNull BlockState state,
                                                         Direction side, @NotNull RandomSource rand,
                                                         @NotNull ModelData modelData, RenderType renderType) {
        BakedModel parent = ((CustomBakedModelAccessor) this).gtceu$getParent();
        var connections = Connections.checkConnections(level, pos, state, side);
        if (side == null) {
            var noSideCache = ((CustomBakedModelAccessor) this).gtceu$getNoSideCache();
            if (noSideCache.isEmpty()) {
                synchronized (noSideCache) {
                    if (noSideCache.isEmpty()) {
                        List<BakedQuad> parentQuads = parent.getQuads(state, null, rand, modelData, renderType);
                        noSideCache.addAll(buildCustomQuads(connections, parentQuads, 0.0f));
                    }
                }
            }
            return noSideCache;
        }
        var sideCache = ((CustomBakedModelAccessor) this).gtceu$getSideCache();
        return sideCache
                .computeIfAbsent(side, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(connections, c -> {
                    List<BakedQuad> parentQuads = parent.getQuads(state, side, rand, modelData, renderType);
                    return buildCustomQuads(c, parentQuads, 0.0f);
                });
    }
}
