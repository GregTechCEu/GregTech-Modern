package com.gregtechceu.gtceu.integration.forestry.items;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

import forestry.modules.features.*;

@FeatureProvider
public class GTApicultureItems {

    public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(new ResourceLocation(GTCEu.MOD_ID, "core"));
    public static final FeatureItemGroup<GTCombItem, GTCombType> BEE_COMBS = REGISTRY.itemGroup(GTCombItem::new,
            "bee_comb", GTCombType.VALUES);



    //todo Make the items' IDs match their language keys, e.g. this should be accelerated_frame,
    // FRAME_MUTAGENIC's id should be mutagenic_frame, etc.
    public static final FeatureItem<GTHiveFrameItem> FRAME_ACCELERATED = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(175)
                    .setMutationMult(1.2f)
                    .setSpeedMult(1.8f)
                    .setAgeMult(0.9f)
                    .build(), "frame_accelerated");

    public static final FeatureItem<GTHiveFrameItem> FRAME_MUTAGENIC = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(3)
                    .setMutationMult(5.0f)
                    .setSpeedMult(10.0f)
                    .setAgeMult(0.0001f)
                    .build(), "frame_mutagenic");

    public static final FeatureItem<GTHiveFrameItem> FRAME_WORKING = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(2000)
                    .setMutationMult(0.0f)
                    .setSpeedMult(4.0f)
                    .setAgeMult(3.0f)
                    .build(), "frame_working");

    public static final FeatureItem<GTHiveFrameItem> FRAME_DECAYING = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(240)
                    .setDecayMult(10.0f)
                    .build(), "frame_decaying");

    public static final FeatureItem<GTHiveFrameItem> FRAME_SLOWING = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(175)
                    .setMutationMult(0.5f)
                    .setSpeedMult(0.5f)
                    .setAgeMult(2.0f)
                    .build(), "frame_slowing");

    public static final FeatureItem<GTHiveFrameItem> FRAME_STABILIZING = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(60)
                    .setMutationMult(0.1f)
                    .setSpeedMult(0.1f)
                    .setDecayMult(0.5f)
                    .build(), "frame_stabilizing");

    public static final FeatureItem<GTHiveFrameItem> FRAME_ARBORIST = REGISTRY
            .item(() -> new GTHiveFrameItem.GTItemHiveFrameBuilder(240)
                    .setMutationMult(0.0f)
                    .setSpeedMult(0.0f)
                    .setAgeMult(3.0f)
                    .build(), "frame_arborist");
}
