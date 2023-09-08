package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
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
                if (fluid.getStillTexture() == null) {
                    ResourceLocation foundTexture = MaterialIconType.fluid.getBlockTexturePath(iconSet, false);
                    fluid.setStillTexture(foundTexture);
                }
                if (fluid.getFlowTexture() == null) {
                    fluid.setFlowTexture(fluid.getStillTexture());
                }
                MixinHelpers.addFluidTexture(material, fluid);
            }
            if (material.hasProperty(PropertyKey.ALLOY_BLAST)) {
                AlloyBlastProperty property = material.getProperty(PropertyKey.ALLOY_BLAST);
                MixinHelpers.addFluidTexture(material, property);
            }
        }
        return Sets.newLinkedHashSet();
    }
}
