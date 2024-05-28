package com.gregtechceu.gtceu.api.gui.widget.directional;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.scene.ISceneBlockRenderHook;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CombinedDirectionalConfigurator extends WidgetGroup {

    protected final static int MOUSE_CLICK_CLIENT_ACTION_ID = 0x0001_0001;
    protected final static int UPDATE_UI_ID = 0x0001_0002;

    protected final IDirectionalConfigHandler[] configHandlers;
    protected final int width, height;
    private final FancyMachineUIWidget machineUI;
    private final MetaMachine machine;

    protected SceneWidget sceneWidget;
    protected ImageWidget imageWidget;

    protected @Nullable BlockPos selectedPos;
    protected @Nullable Direction selectedSide;

    public CombinedDirectionalConfigurator(FancyMachineUIWidget machineUI, IDirectionalConfigHandler[] configHandlers,
                                           MetaMachine machine, int width, int height) {
        super(0, 0, width, height);
        this.width = width;
        this.height = height;

        this.machineUI = machineUI;
        this.configHandlers = configHandlers;
        this.machine = machine;
    }

    @Override
    public void initWidget() {
        super.initWidget();

        addWidget(imageWidget = new ImageWidget(0, 0, width, height, GuiTextures.BACKGROUND_INVERSE));
        addWidget(sceneWidget = createSceneWidget());

        for (IDirectionalConfigHandler configHandler : configHandlers) {
            configHandler.addAdditionalUIElements(this);
        }

        addConfigWidgets(sceneWidget);
    }

    private SceneWidget createSceneWidget() {
        var pos = this.machine.getPos();

        SceneWidget sceneWidget = new SceneWidget(4, 4, width - 8, height - 8, this.machine.getLevel())
                .setRenderedCore(List.of(pos), null)
                .setRenderSelect(false)
                .setOnSelected(this::onSideSelected);

        if (isRemote()) {
            sceneWidget.getRenderer().addRenderedBlocks(
                    List.of(pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()),
                    new ISceneBlockRenderHook() {

                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void apply(boolean isTESR, RenderType layer) {
                            RenderSystem.enableBlend();
                            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                        }
                    });

            sceneWidget.getRenderer().setAfterWorldRender(this::renderOverlays);

            var playerRotation = gui.entityPlayer.getRotationVector();
            sceneWidget.setCameraYawAndPitch(playerRotation.x, playerRotation.y - 90);
        }
        sceneWidget.setBackground(ColorPattern.BLACK.rectTexture());
        return sceneWidget;
    }

    private void renderOverlays(WorldSceneRenderer renderer) {
        sceneWidget.renderBlockOverLay(renderer);

        for (Direction face : GTUtil.DIRECTIONS) {
            for (IDirectionalConfigHandler configHandler : configHandlers) {
                configHandler.renderOverlay(sceneWidget, new BlockPosFace(machine.getPos(), face));
            }
        }
    }

    private void addConfigWidgets(SceneWidget sceneWidget) {
        int yOffsetLeft = 0, yOffsetRight = 0;

        for (IDirectionalConfigHandler configHandler : configHandlers) {
            Widget widget = configHandler.getSideSelectorWidget(sceneWidget, machineUI);

            if (widget == null)
                continue;

            final Size widgetSize = widget.getSize();
            switch (configHandler.getScreenSide()) {
                case LEFT -> {
                    widget.setSelfPosition(new Position(6, height - 6 - widgetSize.height - yOffsetLeft));
                    yOffsetLeft += widgetSize.height + 3;
                }
                case RIGHT -> {
                    widget.setSelfPosition(
                            new Position(width - widgetSize.width - 6, height - 6 - widgetSize.height - yOffsetRight));
                    yOffsetRight += widgetSize.height + 3;
                }
            }

            this.addWidget(widget);
        }
    }

    protected void onSideSelected(BlockPos pos, Direction side) {
        if (!pos.equals(machine.getPos()))
            return;

        if (this.selectedSide == side)
            return; // No need to do anything if the same side is already selected

        this.selectedSide = side;

        for (IDirectionalConfigHandler configWidget : this.configHandlers) {
            configWidget.onSideSelected(pos, side);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var lastSide = this.selectedSide;

        var result = super.mouseClicked(mouseX, mouseY, button);

        if (isMouseOverElement(mouseX, mouseY) && this.selectedSide == lastSide && this.selectedSide != null) {
            var hover = sceneWidget.getHoverPosFace();

            if (hover != null && hover.pos.equals(machine.getPos()) && hover.facing == this.selectedSide) {
                var cd = new ClickData();
                writeClientAction(MOUSE_CLICK_CLIENT_ACTION_ID, buf -> {
                    cd.writeToBuf(buf);
                    buf.writeByte(this.selectedSide.ordinal());
                });
            }
        }

        return result;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buf) {
        if (id != MOUSE_CLICK_CLIENT_ACTION_ID) {
            super.handleClientAction(id, buf);
            return;
        }

        var clickData = ClickData.readFromBuf(buf);
        var side = GTUtil.DIRECTIONS[buf.readByte()];

        for (IDirectionalConfigHandler configHandler : configHandlers) {
            configHandler.handleClick(clickData, side);
        }
    }
}
