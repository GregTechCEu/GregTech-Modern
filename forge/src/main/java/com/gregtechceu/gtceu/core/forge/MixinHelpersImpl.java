package com.gregtechceu.gtceu.core.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        for (FluidStorageKey key : FluidStorageKey.allKeys()) {
            Fluid value = prop.getStorage().get(key);
            if (value != null) {
                IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value);
                if (extensions instanceof GTClientFluidTypeExtensions gtExtensions) {
                    gtExtensions.setFlowingTexture(prop.getFlowTexture());
                    gtExtensions.setStillTexture(prop.getStillTexture());
                    gtExtensions.setTintColor(material.getMaterialARGB());
                }
            }
        }
    }
}
