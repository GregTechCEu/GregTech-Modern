package com.gregtechceu.gtceu.api.gui.widget.directional.handlers;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.gui.widget.directional.IDirectionalConfigHandler;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AutoOutputFluidConfigHandler implements IDirectionalConfigHandler {

    private static final IGuiTexture TEXTURE_OFF = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON.getSubTexture(0, 0, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_OUTPUT = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON.getSubTexture(0, 1 / 3f, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_AUTO = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON.getSubTexture(0, 2 / 3f, 1, 1 / 3f));

    private final AutoOutputTrait trait;
    private Direction side;
    private ButtonWidget ioModeButton;

    public AutoOutputFluidConfigHandler(AutoOutputTrait trait) {
        this.trait = trait;
    }

    @Override
    public Widget getSideSelectorWidget(SceneWidget scene, FancyMachineUIWidget machineUI) {
        WidgetGroup group = new WidgetGroup(0, 0, (18 * 2) + 1, 18);

        group.addWidget(ioModeButton = new ButtonWidget(0, 0, 18, 18, this::onIOModePressed) {

            @Override
            public void updateScreen() {
                super.updateScreen();
                if (side == null) {
                    setButtonTexture(TEXTURE_OFF);
                    setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.fluid_auto_output.unselected")
                            .toArray(Component[]::new));
                } else if (trait.getFluidOutputDirection() == side) {
                    if (trait.isAutoOutputFluids()) {
                        setButtonTexture(TEXTURE_AUTO);
                        setHoverTooltips("gtceu.gui.fluid_auto_output.enabled");
                    } else {
                        setButtonTexture(TEXTURE_OUTPUT);
                        setHoverTooltips("gtceu.gui.fluid_auto_output.disabled");
                    }
                } else {
                    setButtonTexture(TEXTURE_OFF);
                    setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.fluid_auto_output.other_direction")
                            .toArray(Component[]::new));
                }
            }
        });

        group.addWidget(new ToggleButtonWidget(
                19, 0, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT,
                trait::allowsFluidInputFromOutputSide, trait::setAllowFluidInputFromOutputSide)
                .setShouldUseBaseBackground().setTooltipText("gtceu.gui.fluid_auto_output.allow_input"));

        return group;
    }

    private void onIOModePressed(ClickData cd) {
        if (this.side == null)
            return;

        if (trait.getFluidOutputDirection() == this.side) {
            trait.setAllowAutoOutputFluids(!trait.isAutoOutputFluids());
        } else {
            trait.setAllowAutoOutputFluids(false);
            trait.setFluidOutputDirection(this.side);
        }
    }

    @Override
    public void onSideSelected(BlockPos pos, Direction side) {
        this.side = side;
    }

    @Override
    public ScreenSide getScreenSide() {
        return ScreenSide.LEFT;
    }

    @Override
    public void handleClick(ClickData cd, Direction direction) {
        if (!canHandleClick(cd) || !trait.supportsAutoOutputFluids())
            return;

        if (trait.getFluidOutputDirection() != side) {
            trait.setFluidOutputDirection(side);
            trait.setAllowAutoOutputFluids(false);
        } else {
            trait.setAllowAutoOutputFluids(!trait.isAutoOutputFluids());
        }
    }

    @SuppressWarnings("RedundantIfStatement") // Cleaner code this way
    private boolean canHandleClick(ClickData cd) {
        if (cd.button == 1)
            return true;

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(SceneWidget sceneWidget, BlockPosFace blockPosFace) {
        if (trait.getFluidOutputDirection() != blockPosFace.facing)
            return;

        sceneWidget.drawFacingBorder(new PoseStack(), blockPosFace,
                trait.isAutoOutputFluids() ? 0xff00b4ff : 0x8f00b4ff, 2);
    }

    @Override
    public void addAdditionalUIElements(WidgetGroup parent) {
        LabelWidget text = new LabelWidget(4, 4, "gtceu.gui.auto_output.name") {

            @Override
            public boolean isVisible() {
                return trait.isAutoOutputFluids() && trait.getFluidOutputDirection() != null;
            }
        };

        text.setSelfPosition(new Position(parent.getSize().width - 4 - text.getSize().width, 4));

        text.setTextColor(0xff00b4ff).setDropShadow(false);
        parent.addWidget(text);
    }
}
