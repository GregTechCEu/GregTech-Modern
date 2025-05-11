package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

public enum ItemPipeType implements IMaterialPipeType<ItemPipeProperties> {

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

    public String getSizeForTexture() {
        if (!isRestrictive())
            return name;
        else
            return name.substring(0, name.length() - 12);
    }

    @Override
    public ItemPipeProperties modifyProperties(ItemPipeProperties baseProperties) {
        return new ItemPipeProperties((int) ((baseProperties.getPriority() * resistanceMultiplier) + 0.5),
                baseProperties.getTransferRate() * rateMultiplier);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel(Material material) {
        PipeModel model;
        if (material.hasProperty(PropertyKey.WOOD)) {
            model = new PipeModel(thickness, () -> GTCEu.id("block/pipe/pipe_side_wood"),
                    () -> GTCEu.id("block/pipe/pipe_%s_in_wood"
                            .formatted(this.isRestrictive() ? values()[this.ordinal() - 4].name : name)),
                    null, null);
        } else {
            model = new PipeModel(thickness, () -> GTCEu.id("block/pipe/pipe_side"),
                    () -> GTCEu.id("block/pipe/pipe_%s_in"
                            .formatted(this.isRestrictive() ? values()[this.ordinal() - 4].name : name)),
                    null, null/*
                               * () -> GTCEu.id("block/pipe/pipe_side_secondary"), () ->
                               * GTCEu.id("block/pipe/pipe_%s_in_secondary".formatted(this.isRestrictive() ?
                               * values()[this.ordinal() - 4].name : name)) TODO enable once the textures are added
                               */);
        }
        if (isRestrictive()) {
            model.setSideOverlayTexture(GTCEu.id("block/pipe/pipe_restrictive"));
        }
        return model;
    }
}
