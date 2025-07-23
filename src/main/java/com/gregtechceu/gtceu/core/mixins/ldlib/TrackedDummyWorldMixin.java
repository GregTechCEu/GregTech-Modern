package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.extensions.IBlockGetterExtension;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

@Mixin(value = TrackedDummyWorld.class, remap = false)
public abstract class TrackedDummyWorldMixin extends DummyWorldMixin implements ILevelExtension, IBlockGetterExtension {

    @Shadow
    private Predicate<BlockPos> renderFilter;

    @Shadow
    @Final
    public WeakReference<Level> proxyWorld;

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos)) {
            return ModelData.EMPTY;
        }
        Level proxy = proxyWorld.get();
        return proxy != null ? proxy.getModelData(pos) : super.getModelData(pos);
    }
}
