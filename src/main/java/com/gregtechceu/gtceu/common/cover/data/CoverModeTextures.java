package com.gregtechceu.gtceu.common.cover.data;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class CoverModeTextures {

    private CoverModeTextures() {}

    public static IGuiTexture getBucketModeIcon(BucketMode mode) {
        return new ResourceTexture(mode.getTextureName() + ".png").scale(16F / 20F);
    }

    public static IGuiTexture getDistributionModeIcon(DistributionMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/distribution_mode/" + mode.localeName + ".png");
    }

    public static IGuiTexture getFilterModeIcon(FilterMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/filter_mode/" + mode.localeName + ".png");
    }

    public static IGuiTexture getManualIOModeIcon(ManualIOMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/manual_io_mode/" + mode.localeName + ".png");
    }

    public static IGuiTexture getTransferModeIcon(TransferMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + mode.getTextureName() + ".png");
    }

    public static IGuiTexture getVoidingModeIcon(VoidingMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/voiding_mode/" + mode.getTextureName() + ".png");
    }
}
