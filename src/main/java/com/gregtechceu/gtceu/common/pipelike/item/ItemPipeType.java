package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.api.pipenet.PipeSegmentPropertyHolder;
import com.gregtechceu.gtceu.api.pipenet.property.FloatSegmentProperty;
import com.gregtechceu.gtceu.api.pipenet.property.IntSegmentProperty;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.pipelike.SegmentPropertyTypes;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum ItemPipeType implements IMaterialPipeType {

    SMALL("small", 0.375f, TagPrefix.pipeSmallItem, 0.5f, 1.5f),
    NORMAL("normal", 0.5f, TagPrefix.pipeNormalItem, 1f, 1f),
    LARGE("large", 0.625f, TagPrefix.pipeLargeItem, 2f, 0.75f),
    HUGE("huge", 0.75f, TagPrefix.pipeHugeItem, 4f, 0.5f),

    RESTRICTIVE_SMALL("small_restrictive", 0.375f, TagPrefix.pipeSmallRestrictive, 0.5f, 150f),
    RESTRICTIVE_NORMAL("normal_restrictive", 0.5f, TagPrefix.pipeNormalRestrictive, 1f, 100f),
    RESTRICTIVE_LARGE("large_restrictive", 0.625f, TagPrefix.pipeLargeRestrictive, 2f, 75f),
    RESTRICTIVE_HUGE("huge_restrictive", 0.75f, TagPrefix.pipeHugeRestrictive, 4f, 50f);

    public static final ResourceLocation TYPE_ID = GTCEu.id("item");
    public static final ItemPipeType[] VALUES = values();

    @Getter
    public final String name;
    @Getter
    private final float thickness;
    @Getter
    private final float rateMultiplier;
    private final float resistanceMultiplier;
    @Getter
    private final TagPrefix tagPrefix;

    ItemPipeType(String name, float thickness, TagPrefix orePrefix, float rateMultiplier, float resistanceMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.tagPrefix = orePrefix;
        this.rateMultiplier = rateMultiplier;
        this.resistanceMultiplier = resistanceMultiplier;
    }

    public boolean isRestrictive() {
        return ordinal() > 3;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel(PipeBlock<?> block, Material material, GTBlockstateProvider provider) {
        ResourceLocation sideTexture = GTCEu.id("block/pipe/pipe_side");
        ResourceLocation endTexture = GTCEu.id("block/pipe/pipe_%s_in"
                .formatted(this.isRestrictive() ? values()[this.ordinal() - 4].name : name));
        if (material.hasProperty(PropertyKey.WOOD)) {
            sideTexture = sideTexture.withSuffix("_wood");
            endTexture = endTexture.withSuffix("_wood");
        }

        PipeModel model = new PipeModel(block, provider, thickness, sideTexture, endTexture);
        if (isRestrictive()) {
            model.setSideOverlay(GTCEu.id("block/pipe/pipe_restrictive"));
        }
        return model;
    }

    @Override
    public PipeSegmentPropertyHolder buildSegmentProperties(@Nullable Material material) {
        if (material == null || material.getProperty(PropertyKey.ITEM_PIPE) == null) throw new IllegalArgumentException(
                "Attempted to build item pipe properties for null material or material without PropertyKey.ITEM_PIPE");
        var segmentProperties = new PipeSegmentPropertyHolder();
        var materialProperties = material.getProperty(PropertyKey.ITEM_PIPE);

        segmentProperties.setProperty(SegmentPropertyTypes.PRIORITY,
                new IntSegmentProperty((int) ((materialProperties.getPriority() * resistanceMultiplier) + 0.5)));
        segmentProperties.setProperty(SegmentPropertyTypes.TRANSFER_RATE,
                new FloatSegmentProperty(materialProperties.getTransferRate() * rateMultiplier));

        return segmentProperties;
    }
}
