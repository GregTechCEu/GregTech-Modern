package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashSet;

@Mixin(value = ModelBakery.class, priority = 999)
public abstract class ModelBakeryMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newLinkedHashSet()Ljava/util/LinkedHashSet;", ordinal = 0, remap = false))
    private LinkedHashSet<Pair<String, String>> gtceu$injectModelBakery() { // Have to use a redirect here cuz it's to constructor and mixin doesn't like that much
        for (Material material : GTRegistries.MATERIALS.values()) {
            MaterialIconSet iconSet = material.getMaterialIconSet();
            if (material.hasProperty(PropertyKey.FLUID)) {
                FluidProperty fluid = material.getProperty(PropertyKey.FLUID);

                for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                    FluidStorage.FluidEntry fluidEntry = fluid.getStorage().getEntry(key);
                    if (fluidEntry != null) {
                        if (fluidEntry.getStillTexture() == null) {
                            ResourceLocation foundTexture = key.getIconType().getBlockTexturePath(iconSet, false);
                            fluidEntry.setStillTexture(foundTexture);
                        }
                        if (fluidEntry.getFlowTexture() == null) {
                            fluidEntry.setFlowTexture(fluidEntry.getStillTexture());
                        }
                        MixinHelpers.addFluidTexture(material, fluidEntry);
                    }
                }

            }
        }
        return Sets.newLinkedHashSet();
    }
}
