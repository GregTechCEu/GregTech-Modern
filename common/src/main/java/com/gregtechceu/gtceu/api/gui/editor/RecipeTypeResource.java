package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.gui.editor.ui.ResourcePanel;
import com.lowdragmc.lowdraglib.gui.editor.ui.resource.ResourceContainer;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeTypeResource
 */
public class RecipeTypeResource extends Resource<Integer> {
    public final static String RESOURCE_NAME = "gtceu.gui.editor.group.recipe_type";
    @Override
    public String name() {
        return RESOURCE_NAME;
    }

    public void switchRecipeType(GTRecipeType recipeType) {
        data.clear();
        for (Object2IntMap.Entry<RecipeCapability<?>> entries : recipeType.maxInputs.object2IntEntrySet()) {
            var cap = entries.getKey();
            if (cap != null) {
                for (int i = 0; i < entries.getIntValue(); i++) {
                    addResource(cap.slotName(IO.IN, i), cap.color);
                }
            }
        }
        for (Object2IntMap.Entry<RecipeCapability<?>> entries : recipeType.maxOutputs.object2IntEntrySet()) {
            var cap = entries.getKey();
            if (cap != null) {
                for (int i = 0; i < entries.getIntValue(); i++) {
                    addResource(cap.slotName(IO.OUT, i), cap.color);
                }
            }
        }
        addResource("progress", 0xff377E3B);
    }

    @Override
    public ResourceContainer<Integer, ? extends Widget> createContainer(ResourcePanel panel) {
        return new RecipeResourceContainer(this, panel);
    }

    @Nullable
    @Override
    public Tag serialize(Integer value) {
        return IntTag.valueOf(value);
    }

    @Override
    public Integer deserialize(Tag nbt) {
        return nbt instanceof IntTag intTag ? intTag.getAsInt() : -1;
    }

    private static class RecipeResourceContainer extends ResourceContainer<Integer, ImageWidget>{
        RecipeTypeResource resource;
        public RecipeResourceContainer(RecipeTypeResource resource, ResourcePanel panel) {
            super(resource, panel);
            this.resource = resource;
            setDragging(key -> (IIdProvider) () -> key, key -> new TextTexture(key.get()));
        }

        @Override
        public void reBuild() {
            selected = null;
            container.clearAllWidgets();
            int width = (getSize().getWidth() - 16) / 2;
            int i = 0;
            for (var entry : resource.allResources()) {
                ImageWidget widget = new ImageWidget(width, 0, width, 15,
                        new GuiTextureGroup(ColorPattern.T_WHITE.rectTexture(),
                                new TextTexture("0").setSupplier(() -> {
                                    var project = panel.getEditor().getCurrentProject();
                                    if (project instanceof RecipeTypeUIProject recipeTypeUIProject) {
                                        return String.valueOf(recipeTypeUIProject.root.getWidgetsById(Pattern.compile("^%s$".formatted(entry.getKey()))).size());
                                    }
                                    return 0 + "";
                                })));
                widget.setHoverTooltips("gtceu.gui.editor.tips.citation");
                widgets.put(entry.getKey(), widget);
                Size size = widget.getSize();
                SelectableWidgetGroup selectableWidgetGroup = new SelectableWidgetGroup(3, 3 + i * 17, width * 2, 15);
                selectableWidgetGroup.setDraggingProvider(draggingMapping == null ? entry::getValue : () -> draggingMapping.apply(entry.getKey()), (c, p) -> draggingRenderer.apply(c));
                selectableWidgetGroup.addWidget(new ImageWidget(0, 0, width, 15, new GuiTextureGroup(
                        new ColorRectTexture(entry.getValue()),
                        new TextTexture(entry.getKey() + " ").setWidth(size.width).setType(TextTexture.TextType.ROLL))));
                selectableWidgetGroup.addWidget(widget);
                selectableWidgetGroup.setOnSelected(s -> selected = entry.getKey());
                selectableWidgetGroup.setOnUnSelected(s -> selected = null);
                selectableWidgetGroup.setSelectedTexture(ColorPattern.T_GRAY.rectTexture());
                container.addWidget(selectableWidgetGroup);
                i++;
            }
        }

        @Override
        protected TreeBuilder.Menu getMenu() {
            return null;
        }
    }
}
