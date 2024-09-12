package com.gregtechceu.gtceu.common.pipelike.block.cable;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IInsulatable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRedirector;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRegistry;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CableStructure(String name, int material, int costFactor, TagPrefix prefix,
                             @Nullable CableStructure partialBurnStructure, @Nullable Integer partialBurnThreshold,
                             float renderThickness, PipeModelRedirector model)
        implements IPipeMaterialStructure, IInsulatable {

    public static final int INSULATION_BURN_TEMP = 1000;

    public static final CableStructure WIRE_SINGLE = new CableStructure("single_wire", 1, 2, TagPrefix.wireGtSingle,
            null, null, 0.125f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_DOUBLE = new CableStructure("double_wire", 2, 2, TagPrefix.wireGtDouble,
            null, null, 0.25f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_QUADRUPLE = new CableStructure("quadruple_wire", 4, 3,
            TagPrefix.wireGtQuadruple, null, null, 0.375f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_OCTAL = new CableStructure("octal_wire", 8, 3, TagPrefix.wireGtOctal, null,
            null, 0.5f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_HEX = new CableStructure("hex_wire", 16, 3, TagPrefix.wireGtHex, null, null,
            0.75f, PipeModelRegistry.getCableModel(0));

    public static final CableStructure CABLE_SINGLE = new CableStructure("single_cable", 1, 1, TagPrefix.cableGtSingle,
            WIRE_SINGLE, INSULATION_BURN_TEMP, 0.25f, PipeModelRegistry.getCableModel(1));
    public static final CableStructure CABLE_DOUBLE = new CableStructure("double_cable", 2, 1, TagPrefix.cableGtDouble,
            WIRE_DOUBLE, INSULATION_BURN_TEMP, 0.375f, PipeModelRegistry.getCableModel(2));
    public static final CableStructure CABLE_QUADRUPLE = new CableStructure("quadruple_cable", 4, 1,
            TagPrefix.cableGtQuadruple, WIRE_QUADRUPLE, INSULATION_BURN_TEMP, 0.5f, PipeModelRegistry.getCableModel(3));
    public static final CableStructure CABLE_OCTAL = new CableStructure("octal_cable", 8, 1, TagPrefix.cableGtOctal,
            WIRE_OCTAL, INSULATION_BURN_TEMP, 0.75f, PipeModelRegistry.getCableModel(4));
    public static final CableStructure CABLE_HEX = new CableStructure("hex_cable", 16, 1, TagPrefix.cableGtHex,
            WIRE_HEX, INSULATION_BURN_TEMP, 1f, PipeModelRegistry.getCableModel(5));

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public TagPrefix getPrefix() {
        return prefix;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock ? GuiTextures.TOOL_WIRE_CONNECT : GuiTextures.TOOL_WIRE_BLOCK;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public boolean isInsulated() {
        return partialBurnStructure != null;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(WIRE_SINGLE);
        event.register(WIRE_DOUBLE);
        event.register(WIRE_QUADRUPLE);
        event.register(WIRE_OCTAL);
        event.register(WIRE_HEX);
        event.register(CABLE_SINGLE);
        event.register(CABLE_DOUBLE);
        event.register(CABLE_QUADRUPLE);
        event.register(CABLE_OCTAL);
        event.register(CABLE_HEX);
    }
}
