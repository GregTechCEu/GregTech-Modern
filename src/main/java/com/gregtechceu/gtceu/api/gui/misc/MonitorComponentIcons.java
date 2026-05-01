package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.Identifier;

public final class MonitorComponentIcons {

    private MonitorComponentIcons() {}

    public static IGuiTexture buttonCheck() {
        return GuiTextures.BUTTON_CHECK;
    }

    public static IGuiTexture monitorCover() {
        return ResourceTexture.fromSpirit(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID,
                "item/computer_monitor_cover"));
    }

    public static IGuiTexture networkSwitch() {
        return ResourceTexture.fromSpirit(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID,
                "block/multiblock/network_switch/overlay_front_active"));
    }

    public static IGuiTexture dataModule() {
        return new ResourceTexture(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID,
                "textures/item/data_module.png")).getSubTexture(0, 0, 1, 1 / 13f);
    }

    public static IGuiTexture hpcaBridgeComponent() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }

    public static IGuiTexture hpcaComputationComponent(boolean advanced, boolean damaged) {
        if (damaged) {
            return advanced ? GuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }

    public static IGuiTexture hpcaCoolerComponent(boolean advanced) {
        return advanced ? GuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT :
                GuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }

    public static IGuiTexture hpcaEmptyComponent() {
        return GuiTextures.HPCA_ICON_EMPTY_COMPONENT;
    }
}
