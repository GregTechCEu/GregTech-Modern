package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

@Setter
@Accessors(chain = true)
public class ColorBlockWidget extends Widget {
    private IntSupplier colorSupplier;
    @Getter
    private int currentColor;

    public ColorBlockWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.currentColor = 0xFFFFFFFF;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClientSideWidget && colorSupplier != null) {
            currentColor = colorSupplier.getAsInt();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x + 1;
        int y = getPosition().y + 1;
        int width = getSize().width - 2;
        int height = getSize().height - 2;

        if (colorSupplier != null) {
            currentColor = colorSupplier.getAsInt();
        }
        final int BORDER_COLOR = 0xFF000000;

        graphics.fill(x, y, x + width, y + height, currentColor);
        DrawerHelper.drawBorder(graphics, x, y, width, height, BORDER_COLOR, 1);
    }
}
