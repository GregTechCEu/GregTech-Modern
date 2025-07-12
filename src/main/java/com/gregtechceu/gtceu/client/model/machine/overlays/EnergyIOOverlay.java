package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;

public class EnergyIOOverlay {

    public static final EnergyIOOverlay ENERGY_IN_1A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_1a_tinted", "block/overlay/machine/overlay_energy_1a_in",
            "block/overlay/machine/overlay_energy_1a_in_emissive");
    public static final EnergyIOOverlay ENERGY_IN_2A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_2a_tinted", "block/overlay/machine/overlay_energy_2a_in",
            "block/overlay/machine/overlay_energy_2a_in_emissive");
    public static final EnergyIOOverlay ENERGY_IN_4A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_4a_tinted", "block/overlay/machine/overlay_energy_4a_in",
            "block/overlay/machine/overlay_energy_4a_in_emissive");
    public static final EnergyIOOverlay ENERGY_IN_8A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_8a_tinted", "block/overlay/machine/overlay_energy_8a_in",
            "block/overlay/machine/overlay_energy_8a_in_emissive");
    public static final EnergyIOOverlay ENERGY_IN_16A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_16a_tinted", "block/overlay/machine/overlay_energy_16a_in",
            "block/overlay/machine/overlay_energy_16a_in_emissive");
    public static final EnergyIOOverlay ENERGY_IN_64A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_64a_tinted", "block/overlay/machine/overlay_energy_64a_in",
            "block/overlay/machine/overlay_energy_64a_in_emissive");

    public static final EnergyIOOverlay ENERGY_OUT_1A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_1a_tinted", "block/overlay/machine/overlay_energy_1a_out",
            "block/overlay/machine/overlay_energy_1a_out_emissive");
    public static final EnergyIOOverlay ENERGY_OUT_2A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_2a_tinted", "block/overlay/machine/overlay_energy_2a_out",
            "block/overlay/machine/overlay_energy_2a_out_emissive");
    public static final EnergyIOOverlay ENERGY_OUT_4A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_4a_tinted", "block/overlay/machine/overlay_energy_4a_out",
            "block/overlay/machine/overlay_energy_4a_out_emissive");
    public static final EnergyIOOverlay ENERGY_OUT_8A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_8a_tinted", "block/overlay/machine/overlay_energy_8a_out",
            "block/overlay/machine/overlay_energy_8a_out_emissive");
    public static final EnergyIOOverlay ENERGY_OUT_16A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_16a_tinted", "block/overlay/machine/overlay_energy_16a_out",
            "block/overlay/machine/overlay_energy_16a_out_emissive");
    public static final EnergyIOOverlay ENERGY_OUT_64A = new EnergyIOOverlay(
            "block/overlay/machine/overlay_energy_64a_tinted", "block/overlay/machine/overlay_energy_64a_out",
            "block/overlay/machine/overlay_energy_64a_out_emissive");

    public static final Int2ObjectMap<EnergyIOOverlay> IN_OVERLAYS_FOR_AMP = Util.make(new Int2ObjectArrayMap<>(6),
            map -> {
                map.put(1, ENERGY_IN_1A);
                map.put(2, ENERGY_IN_2A);
                map.put(4, ENERGY_IN_4A);
                map.put(8, ENERGY_IN_8A);
                map.put(16, ENERGY_IN_16A);
                map.put(64, ENERGY_IN_64A);
                map.defaultReturnValue(ENERGY_IN_1A);
            });

    public static final Int2ObjectMap<EnergyIOOverlay> OUT_OVERLAYS_FOR_AMP = Util.make(new Int2ObjectArrayMap<>(6),
            map -> {
                map.put(1, ENERGY_OUT_1A);
                map.put(2, ENERGY_OUT_2A);
                map.put(4, ENERGY_OUT_4A);
                map.put(8, ENERGY_OUT_8A);
                map.put(16, ENERGY_OUT_16A);
                map.put(64, ENERGY_OUT_64A);
                map.defaultReturnValue(ENERGY_OUT_1A);
            });

    @Getter
    private final ResourceLocation tintedPart;
    @Getter
    private final ResourceLocation ioPart;
    @Getter
    private final ResourceLocation ioPartEmissive;

    public EnergyIOOverlay(String tintedPart, String ioPart, String ioPartEmissive) {
        this.tintedPart = GTCEu.id(tintedPart);
        this.ioPart = GTCEu.id(ioPart);
        this.ioPartEmissive = GTCEu.id(ioPartEmissive);
    }
}
