package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

/**
 * @author KilaBash
 * @date 2023/7/20
 * @implNote GTModels
 */
public class GTModels {
    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;
            MaterialIconSet iconSet = material.getMaterialIconSet();

            for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                FluidStorage storage = fluidProperty.getStorage();
                // fluid block models.
                FluidStorage.FluidEntry fluidEntry = storage.getEntry(key);
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

                // bucket models.
                Fluid fluid = storage.get(key);
                if (fluid instanceof GTFluid gtFluid) {
                    FluidStack testFor = FluidStack.create(gtFluid, FluidHelper.getBucket());
                    GTDynamicResourcePack.addItemModel(
                            Registry.ITEM.getKey(gtFluid.getBucket()),
                            new DelegatedModel(GTCEu.id("item/bucket/" + (FluidHelper.isLighterThanAir(testFor) ? "bucket_gas" : "bucket")))
                    );
                }
            }
        }
    }
}
