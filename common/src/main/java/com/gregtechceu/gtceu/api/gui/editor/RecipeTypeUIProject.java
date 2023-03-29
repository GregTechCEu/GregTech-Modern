package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.annotation.RegisterUI;
import com.lowdragmc.lowdraglib.gui.editor.data.Project;
import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.editor.ui.MainPanel;
import com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeTypeUIProject
 */
@RegisterUI(name = "rtui", group = "project")
public class RecipeTypeUIProject extends UIProject {

    @Nullable @Getter
    protected GTRecipeType recipeType;
    @Nullable @Getter
    private MainPanel currentPanel;

    private RecipeTypeUIProject() {
        this(null, null);
    }

    public RecipeTypeUIProject(RecipeTypeResources resources, WidgetGroup root) {
        super(resources, root);
    }

    public RecipeTypeUIProject(CompoundTag tag) {
        super(tag);
    }

    public RecipeTypeUIProject newEmptyProject() {
        return new RecipeTypeUIProject(RecipeTypeResources.defaultResource(),
                (WidgetGroup) new WidgetGroup(30, 30, 176, 83).setBackground(ResourceBorderTexture.BORDERED_BACKGROUND));
    }

    public void setRecipeType(@Nullable GTRecipeType recipeType) {
        this.recipeType = recipeType;
        if (this.currentPanel != null) {
            if (recipeType != null) {
                this.currentPanel.setBackground(new TextTexture(recipeType.registryName.toLanguageKey()).scale(2.0f));
            } else {
                this.currentPanel.setBackground(IGuiTexture.EMPTY);
            }
        }
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

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        if (recipeType != null) {
            tag.putString("recipe_type", recipeType.registryName.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("recipe_type")) {
            recipeType = GTRegistries.RECIPE_TYPES.get(new ResourceLocation(tag.getString("recipe_type")));
        }
    }

    @Override
    public RecipeTypeResources loadResources(CompoundTag tag) {
        return RecipeTypeResources.fromNBT(tag);
    }

    @Override
    public void onLoad(Editor editor) {
        editor.getResourcePanel().loadResource(getResources(), false);
        editor.getTabPages().addTab(new TabButton(50, 16, 60, 14).setTexture(
                new GuiTextureGroup(ColorPattern.T_GREEN.rectTexture().setBottomRadius(10).transform(0, 0.4f), new TextTexture("Main")),
                new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setBottomRadius(10).transform(0, 0.4f), new TextTexture("Main"))
        ), currentPanel = new MainPanel(editor, root));
        if (recipeType != null) {
            this.currentPanel.setBackground(new TextTexture(recipeType.registryName.toLanguageKey()).scale(2.0f));
        } else {
            this.currentPanel.setBackground(IGuiTexture.EMPTY);
        }
        for (WidgetToolBox.Default tab : WidgetToolBox.Default.TABS) {
            editor.getToolPanel().addNewToolBox("ldlib.gui.editor.group." + tab.groupName, tab.icon, tab.createToolBox());
        }
    }

    @Override
    public void attachMenu(Editor editor, String name, TreeBuilder.Menu menu) {
        if (name.equals("file")) {
            if (recipeType == null) {
                menu.remove("ldlib.gui.editor.menu.save");
            } else {
                menu.remove("ldlib.gui.editor.menu.save");
                menu.leaf(Icons.SAVE, "ldlib.gui.editor.menu.save", () -> {
                    var path = new File(LDLib.location, "assets/%s/ui/recipe_type".formatted(recipeType.registryName.getNamespace()));
                    path.mkdirs();
                    saveProject(new File(path, recipeType.registryName.getPath() + "." + this.getRegisterUI().name()));
                    recipeType.reloadCustomUI();
                });
            }
        }
    }
}
