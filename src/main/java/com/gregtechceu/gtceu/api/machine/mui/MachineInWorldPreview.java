package com.gregtechceu.gtceu.api.machine.mui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.PosListSchema;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class MachineInWorldPreview extends ModularPanel<MachineInWorldPreview> {

    private final @Nullable SchemaRenderer schemaRenderer;
    private final @Nullable SchemaWidget schemaWidget;
    private final MetaMachine machine;


    public MachineInWorldPreview(MetaMachine machine, PanelSyncManager syncManager) {
        super(machine.getDefinition().getId().getPath() + "_eio");
        this.machine = machine;

        if (!machine.getLevel().isClientSide()) {
            schemaRenderer = null;
            schemaWidget = null;
            return;
        }

        var col = Flow.col().coverChildren();

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
                return machine.getBlockPos().below(1).east(1);
            }
        };

        this.schemaRenderer = schema.createRenderer();

        this.schemaWidget = new SchemaWidget(schemaRenderer).size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, MachineUIPanel.DEFAULT_CONTENT_HEIGHT)
                .background(GuiTextures.DISPLAY);

        col.child(schemaWidget);

        center();
        coverChildren();
        padding(4);
        child(col);
    }
}
