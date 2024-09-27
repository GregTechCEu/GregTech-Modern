package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class OverlayEnergyIORenderer {

    public static final OverlayEnergyIORenderer ENERGY_IN_1A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_1a_tinted", "block/overlay/machine/overlay_energy_1a_in");
    public static final OverlayEnergyIORenderer ENERGY_IN_2A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_2a_tinted", "block/overlay/machine/overlay_energy_2a_in");
    public static final OverlayEnergyIORenderer ENERGY_IN_4A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_4a_tinted", "block/overlay/machine/overlay_energy_4a_in");
    public static final OverlayEnergyIORenderer ENERGY_IN_8A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_8a_tinted", "block/overlay/machine/overlay_energy_8a_in");
    public static final OverlayEnergyIORenderer ENERGY_IN_16A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_16a_tinted", "block/overlay/machine/overlay_energy_16a_in");
    public static final OverlayEnergyIORenderer ENERGY_IN_64A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_64a_tinted", "block/overlay/machine/overlay_energy_64a_in");

    public static final OverlayEnergyIORenderer ENERGY_OUT_1A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_1a_tinted", "block/overlay/machine/overlay_energy_1a_out");
    public static final OverlayEnergyIORenderer ENERGY_OUT_2A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_2a_tinted", "block/overlay/machine/overlay_energy_2a_out");
    public static final OverlayEnergyIORenderer ENERGY_OUT_4A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_4a_tinted", "block/overlay/machine/overlay_energy_4a_out");
    public static final OverlayEnergyIORenderer ENERGY_OUT_8A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_8a_tinted", "block/overlay/machine/overlay_energy_8a_out");
    public static final OverlayEnergyIORenderer ENERGY_OUT_16A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_16a_tinted", "block/overlay/machine/overlay_energy_16a_out");
    public static final OverlayEnergyIORenderer ENERGY_OUT_64A = new OverlayEnergyIORenderer(
            "block/overlay/machine/overlay_energy_64a_tinted", "block/overlay/machine/overlay_energy_64a_out");

    private final ResourceLocation tintedPart;
    private final ResourceLocation ioPart;

    public OverlayEnergyIORenderer(String tintedPart, String ioPart) {
        this.tintedPart = GTCEu.id(tintedPart);
        this.ioPart = GTCEu.id(ioPart);
    }

    public void renderOverlay(List<BakedQuad> quads, Direction modelFacing, ModelState modelState, int tintIndex) {
        quads.add(StaticFaceBakery.bakeFace(
                StaticFaceBakery.SLIGHTLY_OVER_BLOCK, modelFacing,
                ModelFactory.getBlockSprite(tintedPart), modelState, tintIndex, 0, true, true));
        quads.add(StaticFaceBakery.bakeFace(
                StaticFaceBakery.SLIGHTLY_OVER_BLOCK, modelFacing,
                ModelFactory.getBlockSprite(ioPart), modelState, -1, 0, true, true));
    }
}
