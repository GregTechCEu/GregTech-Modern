package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;

public class MultiblockPreviewWidget extends ParentWidget<GTRecipeViewerWidget> {

    private final MultiblockMachineDefinition multiblockDefinition;

    private SchemaWidget multiSchema;

    public MultiblockPreviewWidget(MultiblockMachineDefinition definition) {
        multiblockDefinition = definition;

        child(new ListWidget<>()
                .children(multiblockDefinition.getStructurePatterns().entrySet(), (e) -> {
                    return Text.str(e.getKey()).asWidget();
                }));
    }

    private void setupSchema() {
        // BoxSchema boxSchema = new BoxSchema();

        // multiSchema = new SchemaWidget()
    }
}
