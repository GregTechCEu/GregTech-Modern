package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Predicate;

public class EmbedHandler {

    public static int getEmbedWidth(ModularScreen screen) {
        return screen.getMainPanel().getArea().width;
    }

    public static int getEmbedHeight(ModularScreen screen) {
        return screen.getMainPanel().getArea().height;
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphicsExtractor graphics, float partialTicks) {
        drawEmbed(screen, graphics, partialTicks, r -> true);
    }

    public static void drawEmbedNoVanillaElements(ModularScreen screen, GuiGraphicsExtractor graphics, float partialTicks) {
        drawEmbed(screen, graphics, partialTicks, r -> false);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphicsExtractor graphics, float partialTicks, Predicate<Renderable> vanillaElementFilter) {
        screen.getContext().reset();
        var pose = graphics.pose();
        var m = brachy.modularui.drawable.GuiTransforms.toWorld(pose);
        pose.pushMatrix();
        pose.identity(); // reset all current transformations and only reapply them for the main panel
        screen.getMainPanel().transform((p, stack) -> {
            stack.multiply(m);
        });

        var defContext = ClientScreenHandler.getDefaultContext();
        int mx = defContext.getAbsMouseX();
        int my = defContext.getAbsMouseY();
        screen.render(graphics, mx, my, partialTicks);

        if (vanillaElementFilter != null) {
            graphics.nextStratum();
            ClientScreenHandler.drawVanillaElements(graphics, screen.getScreenWrapper().wrappedScreen(), mx, my, partialTicks, vanillaElementFilter);
        }

        graphics.nextStratum();
        screen.drawForeground(graphics);
        pose.popMatrix();
    }

    public record EmbedWrapper(ModularScreen screen) implements IMuiScreen {

        @Override
        public Screen wrappedScreen() {
            return Minecraft.getInstance().gui.screen();
        }

        @Override
        public void updateGuiArea(Rectangle area) {}
    }
}
