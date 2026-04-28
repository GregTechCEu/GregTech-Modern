package com.gregtechceu.gtceu.common.cover.data;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class CoverModeTextures {

    private CoverModeTextures() {}

    public static Object getBucketModeIcon(BucketMode mode) {
        return new ResourceTexture(mode.getTextureName() + ".png").scale(16F / 20F);
    }

    public static Object getDistributionModeIcon(DistributionMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/distribution_mode/" + mode.localeName + ".png");
    }

    public static Object getFilterModeIcon(FilterMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/filter_mode/" + mode.localeName + ".png");
    }

    public static Object getManualIOModeIcon(ManualIOMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/manual_io_mode/" + mode.localeName + ".png");
    }

    public static Object getTransferModeIcon(TransferMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + mode.getTextureName() + ".png");
    }

    public static Object getVoidingModeIcon(VoidingMode mode) {
        return new ResourceTexture("gtceu:textures/gui/icon/voiding_mode/" + mode.getTextureName() + ".png");
    }
}
