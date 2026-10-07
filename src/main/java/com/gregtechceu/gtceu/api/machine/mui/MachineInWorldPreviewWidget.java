package com.gregtechceu.gtceu.api.machine.mui;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.PosListSchema;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SchemaWidget;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class MachineInWorldPreviewWidget extends ParentWidget<MachineInWorldPreviewWidget> {

    private final @Nullable SchemaRenderer schemaRenderer;
    private final @Nullable SchemaWidget schemaWidget;
    private final MetaMachine machine;


    public MachineInWorldPreviewWidget(MetaMachine machine, PanelSyncManager syncManager) {
        this.machine = machine;

        if (!machine.getLevel().isClientSide()) {
            schemaRenderer = null;
            schemaWidget = null;
            return;
        }

        List<BlockPos> positions = new ArrayList<>();
        positions.add(machine.getBlockPos());
        for (var dir: Direction.values()) {
            positions.add(machine.getBlockPos().relative(dir));
        }
        var schema = new PosListSchema(machine.getLevel(), positions) {
            @Override
            public Vector3fc getFocus() {
                return machine.getBlockPos().getCenter().toVector3f();
            }

            @Override
            public BlockPos getOrigin() {
                return machine.getBlockPos();
            }
        };

        this.schemaRenderer = new SchemaRenderer(schema);
        this.schemaWidget = schemaRenderer.asWidget().size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, MachineUIPanel.DEFAULT_CONTENT_HEIGHT);

        size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, MachineUIPanel.DEFAULT_CONTENT_HEIGHT);
        child(schemaWidget);
    }
}
