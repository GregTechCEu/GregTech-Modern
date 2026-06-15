package com.gregtechceu.gtceu.integration.forestry.items;

import com.gregtechceu.gtceu.GTCEu;
import forestry.modules.features.FeatureItemGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.resources.ResourceLocation;

@FeatureProvider
public class GTApicultureItems {
    public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get( new ResourceLocation(GTCEu.MOD_ID, "core"));
    public static final FeatureItemGroup<GTCombItem, GTCombType> BEE_COMBS = REGISTRY.itemGroup(GTCombItem::new, "bee_comb", GTCombType.VALUES);
}
