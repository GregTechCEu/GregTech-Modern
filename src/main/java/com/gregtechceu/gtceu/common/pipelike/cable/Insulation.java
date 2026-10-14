package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public enum Insulation implements IMaterialPipeType<WireProperties> {

    WIRE_SINGLE("single_wire", 0.1875f, 1, 2, wireGtSingle, -1),
    WIRE_DOUBLE("double_wire", 0.3125f, 2, 2, wireGtDouble, -1),
    WIRE_QUADRUPLE("quadruple_wire", 0.4375f, 4, 3, wireGtQuadruple, -1),
    WIRE_OCTAL("octal_wire", 0.5625f, 8, 3, wireGtOctal, -1),
    WIRE_HEX("hex_wire", 0.8125f, 16, 3, wireGtHex, -1),

    CABLE_SINGLE("single_cable", 0.25f, 1, 1, cableGtSingle, 0),
    CABLE_DOUBLE("double_cable", 0.375f, 2, 1, cableGtDouble, 1),
    CABLE_QUADRUPLE("quadruple_cable", 0.5f, 4, 1, cableGtQuadruple, 2),
    CABLE_OCTAL("octal_cable", 0.625f, 8, 1, cableGtOctal, 3),
    CABLE_HEX("hex_cable", 0.875f, 16, 1, cableGtHex, 4);

    public static final ResourceLocation TYPE_ID = GTCEu.id("insulation");

    public final String name;
    @Getter
    public final float thickness;
    public final int amperage;
    public final int lossMultiplier;
    @Getter
    public final TagPrefix tagPrefix;
    public final int insulationLevel;
    /// @deprecated Use {@link #isCable() Insulation.isCable()}
    @Deprecated(forRemoval = true, since = "8.0.0")
    public final boolean isCable;

    Insulation(String name, float thickness, int amperage, int lossMultiplier, TagPrefix TagPrefix,
               int insulationLevel) {
        this.name = name;
        this.thickness = thickness;
        this.amperage = amperage;
        this.tagPrefix = TagPrefix;
        this.insulationLevel = insulationLevel;
        this.lossMultiplier = lossMultiplier;

        this.isCable = insulationLevel >= 0;
    }

    @Override
    public WireProperties modifyProperties(WireProperties baseProperties) {
        int lossPerBlock;
        if (!baseProperties.isSuperconductor() && baseProperties.getLossPerBlock() == 0)
            lossPerBlock = (int) (0.75 * lossMultiplier);
        else lossPerBlock = baseProperties.getLossPerBlock() * lossMultiplier;

        return new WireProperties(baseProperties.getVoltage(), baseProperties.getAmperage() * amperage, lossPerBlock,
                baseProperties.isSuperconductor());
    }

    public boolean isCable() {
        return insulationLevel >= 0;
    }

    public Insulation getUninsulated() {
        if (isCable()) {
            return values()[insulationLevel];
        } else {
            return this;
        }
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel(PipeBlock<?, ?, ?> block, Material material, GTBlockstateProvider provider) {
        ResourceLocation side = MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "side", true);
        ResourceLocation end = MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "end", true);

        PipeModel model = new PipeModel(block, provider, thickness,
                isCable() ? GTCEu.id("block/cable/insulation_5") : side, end);

        ResourceLocation sideSecondary = MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "side_overlay", true);
        ResourceLocation endSecondary = MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "end_overlay", true);

        if (sideSecondary != null && !sideSecondary.equals(GTModels.BLANK_TEXTURE)) {
            model.setSideSecondary(sideSecondary);
        }
        if (endSecondary != null && !endSecondary.equals(GTModels.BLANK_TEXTURE)) {
            model.setEndSecondary(endSecondary);
        }
        if (isCable()) {
            model.setEndOverlay(GTCEu.id("block/cable/insulation_%s".formatted(insulationLevel)));
        }
        return model;
    }
}
