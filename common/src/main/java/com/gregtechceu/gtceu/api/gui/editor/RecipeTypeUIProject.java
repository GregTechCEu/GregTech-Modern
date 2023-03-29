package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.gui.editor.annotation.RegisterUI;
import com.lowdragmc.lowdraglib.gui.editor.data.Project;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeTypeUIProject
 */
@RegisterUI(name = "rtui", group = "project")
public class RecipeTypeUIProject extends UIProject {

    private RecipeTypeUIProject() {
        this(null, null);
    }

    public RecipeTypeUIProject(Resources resources, WidgetGroup root) {
        super(resources, root);
    }

    public RecipeTypeUIProject(CompoundTag tag) {
        super(tag);
    }

    public RecipeTypeUIProject newEmptyProject() {
        return new RecipeTypeUIProject(Resources.defaultResource(),
                (WidgetGroup) new WidgetGroup(30, 30, 176, 83).setBackground(ResourceBorderTexture.BORDERED_BACKGROUND));
    }

    @Override
    public Project loadProject(File file) {
        try {
            var tag = NbtIo.read(file);
            if (tag != null) {
                return new RecipeTypeUIProject(tag);
            }
        } catch (IOException ignored) {}
        return null;
    }

}
