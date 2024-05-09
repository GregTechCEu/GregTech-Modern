package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.material.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.material.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.fluid.FluidBuilder;
import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Listen to PostMaterialEvent instead of doing this directly because it's a lot cleaner this way.
 */
//@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AlloyBlastPropertyAddition {

    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (!material.hasFlag(MaterialFlags.DISABLE_ALLOY_PROPERTY)) {
                addAlloyBlastProperty(material);
            }
        }
        // Alloy Blast Overriding
        GTMaterials.NiobiumNitride.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(1, 11, -1));

        GTMaterials.IndiumTinBariumTitaniumCuprate.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 16));
    }

    public static void addAlloyBlastProperty(@NotNull Material material) {
        final List<MaterialStack> components = material.getMaterialComponents();
        // ignore materials which are not alloys
        if (components.size() < 2) return;

        BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);
        if (blastProperty == null) return;

        if (!material.hasProperty(PropertyKey.FLUID)) return;

        // if there are more than 2 fluid-only components in the material, do not generate a hot fluid
        if (components.stream().filter(AlloyBlastPropertyAddition::isMaterialStackFluidOnly).limit(3).count() > 2) {
            return;
        }

        material.setProperty(PropertyKey.ALLOY_BLAST, new AlloyBlastProperty(material.getBlastTemperature()));
        material.getProperty(PropertyKey.FLUID).getStorage().enqueueRegistration(FluidStorageKeys.MOLTEN, new FluidBuilder().state(FluidState.LIQUID));
    }

    private static boolean isMaterialStackFluidOnly(@NotNull MaterialStack ms) {
        return !ms.material().hasProperty(PropertyKey.DUST) && ms.material().hasProperty(PropertyKey.FLUID);
    }
}
