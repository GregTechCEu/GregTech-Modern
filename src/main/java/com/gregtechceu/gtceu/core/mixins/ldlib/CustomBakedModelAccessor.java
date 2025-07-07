package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Mixin(value = CustomBakedModel.class, remap = false)
public interface CustomBakedModelAccessor {

    @Accessor("parent")
    BakedModel gtceu$getParent();

    @Accessor("sideCache")
    ConcurrentMap<Direction, ConcurrentMap<Connections, List<BakedQuad>>> gtceu$getSideCache();

    @Accessor("noSideCache")
    List<BakedQuad> gtceu$getNoSideCache();
}
