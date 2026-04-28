package com.lowdragmc.lowdraglib.gui.editor.data;

import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.nio.file.Path;

public class UIProject {

    protected Resources resources;
    protected WidgetGroup root;

    public UIProject(Resources resources, WidgetGroup root) {
        this.resources = resources;
        this.root = root;
    }

    public UIProject(CompoundTag tag) {
        this(Resources.defaultResource(), new WidgetGroup());
        deserializeNBT(null, tag);
    }

    public UIProject newEmptyProject() {
        return new UIProject(Resources.defaultResource(), new WidgetGroup());
    }

    public UIProject loadProject(Path file) {
        return null;
    }

    public void saveProject(Path file) {}

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {}

    public void onLoad(Editor editor) {}

    public void attachMenu(Editor editor, String name, TreeBuilder.Menu menu) {}

    public Resources getResources() {
        return resources;
    }

    public WidgetGroup getRoot() {
        return root;
    }

    public LDLRegister getRegisterUI() {
        return getClass().getAnnotation(LDLRegister.class);
    }
}
