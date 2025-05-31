package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

@LDLRegister(name = "mui", group = "editor.gtceu")
public class MachineUIProject extends UIProject {

    @Nullable
    @Getter
    protected MachineDefinition machineDefinition;

    private MachineUIProject() {
        this(null, null);
    }

    public MachineUIProject(Resources resources, WidgetGroup root) {
        super(resources, root);
    }

    public MachineUIProject(CompoundTag tag) {
        super(tag);
    }

    public void setMachine(@Nullable MachineDefinition machineDefinition) {
        this.machineDefinition = machineDefinition;
    }

    public MachineUIProject newEmptyProject() {
        return new MachineUIProject(Resources.defaultResource(), new WidgetGroup(30, 30, 200, 150));
    }

    @Override
    public UIProject loadProject(File file) {
        try {
            var tag = NbtIo.read(file);
            if (tag != null) {
                return new MachineUIProject(tag);
            }
        } catch (IOException ignored) {}
        return null;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        if (machineDefinition != null) {
            tag.putString("machine", machineDefinition.getId().toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("machine")) {
            machineDefinition = GTRegistries.MACHINES.get(new ResourceLocation(tag.getString("machine")));
        }
    }

    @Override
    public void onLoad(Editor editor) {
        editor.getResourcePanel().loadResource(getResources(), false);
        editor.getTabPages().addTab(new TabButton(50, 16, 60, 14).setTexture(
                new GuiTextureGroup(ColorPattern.T_GREEN.rectTexture().setBottomRadius(10).transform(0, 0.4f),
                        new TextTexture("Main")),
                new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setBottomRadius(10).transform(0, 0.4f),
                        new TextTexture("Main"))),
                new UIMainPanel(editor, root, machineDefinition == null ? null : machineDefinition.getDescriptionId()));

        for (WidgetToolBox.Default tab : WidgetToolBox.Default.TABS) {
            if (tab == WidgetToolBox.Default.CONTAINER) {
                continue;
            }
            editor.getToolPanel().addNewToolBox("ldlib.gui.editor.group." + tab.groupName, tab.icon,
                    tab.createToolBox());
        }
    }

    @Override
    public void attachMenu(Editor editor, String name, TreeBuilder.Menu menu) {
        if (name.equals("file")) {
            if (machineDefinition == null || machineDefinition.getEditableUI() == null) {
                menu.remove("ldlib.gui.editor.menu.save");
            } else {
                menu.remove("ldlib.gui.editor.menu.save");
                menu.leaf(Icons.SAVE, "ldlib.gui.editor.menu.save", () -> {
                    var editableUI = machineDefinition.getEditableUI();
                    var path = new File(LDLib.location,
                            "assets/%s/ui/machine".formatted(editableUI.getUiPath().getNamespace()));
                    path.mkdirs();
                    saveProject(new File(path, editableUI.getUiPath().getPath() + "." + this.getRegisterUI().name()));
                    editableUI.reloadCustomUI();
                });
            }
        } else if (name.equals("template_tab")) {
            Map<String, List<MachineDefinition>> categories = new LinkedHashMap<>();
            for (var definition : GTRegistries.MACHINES) {
                final var editableUI = definition.getEditableUI();
                if (editableUI != null) {
                    // has editable UI
                    categories.computeIfAbsent(editableUI.getGroupName(), group -> new ArrayList<>()).add(definition);
                }
            }
            categories.forEach((groupName, definitions) -> menu.branch(groupName, m -> {
                Set<EditableMachineUI> addedSet = new HashSet<>();
                for (var definition : definitions) {
                    var editableUI = definition.getEditableUI();
                    if (editableUI != null && addedSet.add(editableUI)) {
                        m.leaf(new ItemStackTexture(definition.asStack()), definition.getDescriptionId(), () -> {
                            root.clearAllWidgets();
                            if (editableUI.hasCustomUI()) {
                                deserializeNBT(editableUI.getCustomUI());
                            } else {
                                var template = editableUI.createDefault();
                                template.setSelfPosition(
                                        new Position(root.getSelfPosition().x, root.getSelfPosition().y));
                                this.root = template;
                            }
                            setMachine(definition);
                            editor.loadProject(this);
                        });
                    }
                }
            }));
        }
    }
}
