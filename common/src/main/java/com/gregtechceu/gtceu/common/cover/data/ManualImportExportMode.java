package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;

public enum ManualImportExportMode implements EnumSelectorWidget.SelectableEnum {
    DISABLED("cover.universal.manual_import_export.mode.disabled"),
    FILTERED("cover.universal.manual_import_export.mode.filtered"),
    UNFILTERED("cover.universal.manual_import_export.mode.unfiltered");

    public static final ManualImportExportMode[] VALUES = values();

    public final String localeName;

    ManualImportExportMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture(localeName);
    }
}