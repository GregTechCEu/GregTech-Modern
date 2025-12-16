package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public enum Insulation implements IMaterialPipeType<WireProperties> {

    WIRE_SINGLE("single_wire", 0.1875f, 1, 2, wireGtSingle, -1, false),
    WIRE_DOUBLE("double_wire", 0.3125f, 2, 2, wireGtDouble, -1, false),
    WIRE_QUADRUPLE("quadruple_wire", 0.4375f, 4, 3, wireGtQuadruple, -1, false),
    WIRE_OCTAL("octal_wire", 0.5625f, 8, 3, wireGtOctal, -1, false),
    WIRE_HEX("hex_wire", 0.8125f, 16, 3, wireGtHex, -1, false),

    CABLE_SINGLE("single_cable", 0.25f, 1, 1, cableGtSingle, 0, true),
    CABLE_DOUBLE("double_cable", 0.375f, 2, 1, cableGtDouble, 1, true),
    CABLE_QUADRUPLE("quadruple_cable", 0.5f, 4, 1, cableGtQuadruple, 2, true),
    CABLE_OCTAL("octal_cable", 0.625f, 8, 1, cableGtOctal, 3, true),
    CABLE_HEX("hex_cable", 0.875f, 16, 1, cableGtHex, 4, true);

    public static final ResourceLocation TYPE_ID = GTCEu.id("insulation");

    public final String name;
    public final float thickness;
    public final int amperage;
    public final int lossMultiplier;
    @Getter
    public final TagPrefix tagPrefix;
    public final int insulationLevel;
    public final boolean isCable;

    Insulation(String name, float thickness, int amperage, int lossMultiplier, TagPrefix TagPrefix, int insulated,
               boolean isCable) {
        this.name = name;
        this.thickness = thickness;
        this.amperage = amperage;
        this.tagPrefix = TagPrefix;
        this.insulationLevel = insulated;
        this.lossMultiplier = lossMultiplier;
        this.isCable = isCable;
    }

    @Override
    public float getThickness() {
        return thickness;
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
        return ordinal() > 4;
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
        Supplier<ResourceLocation> wireSideTexturePath = () -> MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "side", true);
        Supplier<ResourceLocation> wireEndTexturePath = () -> MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "end", true);
        Supplier<@Nullable ResourceLocation> wireSideOverlayTexturePath = () -> MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "side_overlay", true);
        Supplier<@Nullable ResourceLocation> wireEndOverlayTexturePath = () -> MaterialIconType.wire
                .getBlockTexturePath(material.getMaterialIconSet(), "end_overlay", true);
        PipeModel model = new PipeModel(thickness,
                isCable ? () -> GTCEu.id("block/cable/insulation_5") : wireSideTexturePath, wireEndTexturePath,
                wireSideOverlayTexturePath, wireEndOverlayTexturePath);
        if (isCable) {
            model.setEndOverlayTexture(GTCEu.id("block/cable/insulation_%s".formatted(insulationLevel)));
        }
        return model;
    }
}
