package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import net.neoforged.bus.api.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listen to PostMaterialEvent instead of adding the property to all materials manually because it's a lot cleaner this
 * way.
 */
public class AlloyBlastPropertyAddition {

    @SubscribeEvent
    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        for (Material material : GTRegistries.MATERIALS) {
            if (!material.hasFlag(MaterialFlags.DISABLE_ALLOY_PROPERTY)) {
                addAlloyBlastProperty(material);
            }
        }

        // Manual overrides
        GTMaterials.NiobiumNitride.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(1, 11, -1));

        GTMaterials.IndiumTinBariumTitaniumCuprate.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 16));
    }

    public static void addAlloyBlastProperty(@NotNull Material material) {
        final List<MaterialStack> components = material.getMaterialComponents();
        // ignore materials which are not alloys
        if (components.size() < 2) return;

        if (!material.hasProperty(PropertyKey.BLAST)) return;
        if (!material.hasProperty(PropertyKey.FLUID)) return;

        // if there are more than 2 fluid-only components in the material, do not generate a hot fluid
        if (components.stream().filter(AlloyBlastPropertyAddition::isMaterialStackFluidOnly).limit(3).count() > 2) {
            return;
        }

        material.setProperty(PropertyKey.ALLOY_BLAST, new AlloyBlastProperty());
        material.getProperty(PropertyKey.FLUID)
                .enqueueRegistration(FluidStorageKeys.MOLTEN, new FluidBuilder().state(FluidState.LIQUID));
    }

    private static boolean isMaterialStackFluidOnly(@NotNull MaterialStack ms) {
        return !ms.material().hasProperty(PropertyKey.DUST) && ms.material().hasProperty(PropertyKey.FLUID);
    }
}
