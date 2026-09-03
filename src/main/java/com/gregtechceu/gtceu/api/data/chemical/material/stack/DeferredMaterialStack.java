package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.core.Holder;

public record DeferredMaterialStack(Holder<Material> material, long amount) {

}
