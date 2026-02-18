package com.gregtechceu.gtceu.integration.recipeviewer.util;

import com.gregtechceu.gtceu.api.mui.utils.Stencil;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.ApiStatus;

public class RecipeScreenRenderingUtil {

    public static final IItemHandlerModifiable EMPTY_ITEM_HANDLER = new EmptyHandler();

    @ApiStatus.Internal
    public static void drawScreenBackground(GuiGraphics guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        screen.getContext().graphicsPose().pushPose();

        // copied from ClientScreenHandler#drawScreenInternal to
        // let us draw foreground elements separately after everything else.
        Stencil.reset();
        screen.getContext().getStencil().push(screen.getScreenArea());

        screen.render(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.disableDepthTest();

        ClientScreenHandler.drawVanillaElements(guiGraphics, screen.getScreenWrapper().getWrappedScreen(),
                mouseX, mouseY, partialTick);

        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        screen.getContext().getStencil().pop();
        screen.getContext().graphicsPose().popPose();
    }

    @ApiStatus.Internal
    public static void drawScreenForeground(GuiGraphics guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        screen.getContext().graphicsPose().pushPose();

        // copied from ClientScreenHandler#drawScreenInternal to
        // let us draw foreground elements separately after everything else.
        screen.getContext().getStencil().push(screen.getScreenArea());
        RenderSystem.disableDepthTest();
        Lighting.setupForFlatItems();

        screen.drawForeground(guiGraphics, partialTick);

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        screen.getContext().getStencil().pop();
        screen.getContext().graphicsPose().popPose();
    }
}
