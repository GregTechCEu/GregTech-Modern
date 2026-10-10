package com.gregtechceu.gtceu.api.machine.mui;

import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.ArraySchema;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.drawable.schema.PosListSchema;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class MachineInWorldPreview extends ModularPanel<MachineInWorldPreview> {

    private SchemaRenderer schemaRenderer;
    private SchemaWidget schemaWidget;
    private final MetaMachine machine;

    private final Flow mainCol = Flow.col().coverChildren();


    public MachineInWorldPreview(MetaMachine machine, PanelSyncManager syncManager) {
        super(machine.getDefinition().getId().getPath() + "_eio");
        this.machine = machine;

        if (machine.getLevel().isClientSide()) {
            createSchemaWidget();
        }

        center();
        coverChildren();
        padding(4);
        child(mainCol);
    }

    private void createSchemaWidget() {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(machine.getBlockPos());

        for (var dir: Direction.values()) {
            positions.add(machine.getBlockPos().relative(dir));
        }

        IGuiAction.MouseReleased selectSide = (ctx, m) -> {
            if (m == InputConstants.MOUSE_BUTTON_LEFT) {
                return true;
            }
            return false;
        };

        this.schemaRenderer = ArraySchema.of(machine.getLevel(), machine.getBlockPos(), 0)
                .createRenderer();

        schemaRenderer.highlightRenderer(new BlockHighlight(Color.RED.main, false, 0.05f));

        var schemaWidget = new SchemaWidget(schemaRenderer)
                .listenGuiAction(selectSide)
                .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, MachineUIPanel.DEFAULT_CONTENT_HEIGHT);

        this.schemaWidget = schemaWidget;

        mainCol.child(new ParentWidget<>().child(schemaWidget).coverChildren().padding(2).background(GuiTextures.DISPLAY));

    }
}
