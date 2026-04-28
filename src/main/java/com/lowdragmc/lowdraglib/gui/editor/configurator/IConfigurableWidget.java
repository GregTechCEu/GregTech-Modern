package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IConfigurableWidget {

    default void buildConfigurator(ConfiguratorGroup father) {}

    static void deserializeNBT(WidgetGroup root, CompoundTag tag, Resources resources, boolean isProject,
                               HolderLookup.Provider provider) {}
}
