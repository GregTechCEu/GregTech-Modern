package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.lowdragmc.lowdraglib.client.scene.ISceneRenderHook;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.val;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/29
 * @implNote AutoOutputConfigurator
 */
public class AutoOutputConfigurator extends WidgetGroup {
    protected final MetaMachine machine;
    protected SceneWidget sceneWidget;
    // runtime
    @Nullable
    protected Direction side;

    public AutoOutputConfigurator(MetaMachine machine) {
        super(0, 0, 120, 80);
        setBackground(GuiTextures.BACKGROUND_INVERSE);
        this.machine = machine;
        if (!(machine instanceof IAutoOutputFluid || machine instanceof IAutoOutputItem)) {
            throw new IllegalArgumentException("machine %s is not auto-outputable".formatted(machine));
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        sceneWidget = new SceneWidget(4, 4, 120 - 8, 80 - 8, gui.entityPlayer.level())
                .setRenderedCore(List.of(machine.getPos()), null)
                .setRenderSelect(false)
                .setOnSelected(this::onSideSelected);
        if (isRemote()) {
            sceneWidget.getRenderer().addRenderedBlocks(
                    List.of(machine.getPos().above(),
                            machine.getPos().below(),
                            machine.getPos().north(),
                            machine.getPos().south(),
                            machine.getPos().east(),
                            machine.getPos().west()
                    ), new ISceneRenderHook() {
                        @Override
                        @Environment(EnvType.CLIENT)
                        public void apply(boolean isTESR, RenderType layer) {
                            RenderSystem.enableBlend();
                            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                        }
                    });
            sceneWidget.getRenderer().setAfterWorldRender(this::renderBlockOverLay);

            var playerRotation = gui.entityPlayer.getRotationVector();
            sceneWidget.setCameraYawAndPitch(playerRotation.x, playerRotation.y - 90);
        }
        addWidget(sceneWidget.setBackground(ColorPattern.BLACK.rectTexture()));
        var leftButtonStartX = 4;
        var topLabelStartY = 4;
        if (machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.hasAutoOutputItem()) {
            addWidget(new ToggleButtonWidget(leftButtonStartX, 80 - 20 - 2, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, autoOutputItem::isAllowInputFromOutputSideItems, autoOutputItem::setAllowInputFromOutputSideItems)
                    .setShouldUseBaseBackground()
                    .setTooltipText("gtceu.gui.item_auto_output.allow_input"));
            addWidget(new LabelWidget(4, topLabelStartY, () -> autoOutputItem.isAutoOutputItems() && autoOutputItem.getOutputFacingItems() != null ? "gtceu.gui.auto_output.name" : "").setTextColor(0xffff6e0f).setDropShadow(false));
            leftButtonStartX += 20;
            topLabelStartY += 10;
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.hasAutoOutputFluid()) {
            addWidget(new ToggleButtonWidget(leftButtonStartX, 80 - 20 - 2, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, autoOutputFluid::isAllowInputFromOutputSideFluids, autoOutputFluid::setAllowInputFromOutputSideFluids)
                    .setShouldUseBaseBackground()
                    .setTooltipText("gtceu.gui.fluid_auto_output.allow_input"));
            addWidget(new LabelWidget(4, topLabelStartY, () -> autoOutputFluid.isAutoOutputFluids() && autoOutputFluid.getOutputFacingFluids() != null ? "gtceu.gui.auto_output.name" : "").setTextColor(0xff00b4ff).setDropShadow(false));
            leftButtonStartX += 20;
            topLabelStartY += 10;
        }
    }

    private void onSideSelected(BlockPos blockPos, Direction direction) {
        if (blockPos.equals(machine.getPos())) {
            if (side != direction) {
                side = direction;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderBlockOverLay(WorldSceneRenderer renderer) {
        sceneWidget.renderBlockOverLay(renderer);
        for (Direction face : Direction.values()) {
            var blockFace = new BlockPosFace(machine.getPos(), face);
            if (machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.getOutputFacingItems() == face) {
                sceneWidget.drawFacingBorder(new PoseStack(), blockFace, autoOutputItem.isAutoOutputItems() ? 0xffff6e0f : 0x8fff6e0f, 1);
            }
            if (machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.getOutputFacingFluids() == face) {
                sceneWidget.drawFacingBorder(new PoseStack(), blockFace, autoOutputFluid.isAutoOutputFluids() ? 0xff00b4ff : 0x8f00b4ff, 2);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var lastSide = this.side;
        var result = super.mouseClicked(mouseX, mouseY, button);
        if (isMouseOverElement(mouseX, mouseY) && this.side == lastSide && this.side != null) {
            var hover = sceneWidget.getHoverPosFace();
            if (hover != null && hover.pos.equals(machine.getPos()) && hover.facing == this.side && machine.canSetIoOnSide(side)) {
                val cd = new ClickData();
                writeClientAction(0, buf -> {
                    cd.writeToBuf(buf);
                    buf.writeEnum(this.side);
                });
            }
        }
        return result;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            val clickData = ClickData.readFromBuf(buffer);
            val side = buffer.readEnum(Direction.class);

            boolean isItem = false;
            boolean isFluid = false;

            if (machine instanceof IAutoOutputFluid fluid && fluid.hasAutoOutputFluid() && machine instanceof IAutoOutputItem item && item.hasAutoOutputItem()) {
                if (clickData.button == 0) {
                    isItem = true;
                } else if (clickData.button == 1) {
                    isFluid = true;
                }
            } else if (machine instanceof IAutoOutputFluid fluid && fluid.hasAutoOutputFluid()) {
                isFluid = true;
            } else if (machine instanceof IAutoOutputItem item && item.hasAutoOutputItem()) {
                isItem = true;
            }

            if (isItem) {
                IAutoOutputItem item = (IAutoOutputItem) machine;
                if (item.hasAutoOutputItem()) {
                    if (item.getOutputFacingItems() != side) {
                        item.setOutputFacingItems(side);
                        item.setAutoOutputItems(false);
                    } else if (!item.isAutoOutputItems()) {
                        item.setAutoOutputItems(true);
                    } else {
                        item.setOutputFacingItems(null);
                    }
                }
            }

            if (isFluid) {
                IAutoOutputFluid fluid = (IAutoOutputFluid) machine;
                if (fluid.hasAutoOutputFluid()) {
                    if (fluid.getOutputFacingFluids() != side) {
                        fluid.setOutputFacingFluids(side);
                        fluid.setAutoOutputFluids(false);
                    } else if (!fluid.isAutoOutputFluids()) {
                        fluid.setAutoOutputFluids(true);
                    } else {
                        fluid.setOutputFacingFluids(null);
                    }
                }
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }
}
