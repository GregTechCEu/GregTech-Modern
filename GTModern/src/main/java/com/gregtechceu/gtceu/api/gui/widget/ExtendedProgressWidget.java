package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

@Accessors(chain = true)
public class ExtendedProgressWidget extends ProgressWidget {

    private List<Component> serverTooltips = new ArrayList<>();
    @Setter
    private Consumer<List<Component>> serverTooltipSupplier;

    public ExtendedProgressWidget() {
        super(JEIProgress, 0, 0, 40, 40, new ProgressTexture());
    }

    public ExtendedProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height,
                                  ResourceTexture fullImage) {
        super(progressSupplier, x, y, width, height, fullImage);
    }

    public ExtendedProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height,
                                  ProgressTexture progressBar) {
        super(progressSupplier, x, y, width, height, progressBar);
    }

    public ExtendedProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(progressSupplier, x, y, width, height);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (serverTooltipSupplier != null) {
            List<Component> textBuffer = new ArrayList<>();
            serverTooltipSupplier.accept(textBuffer);
            if (!serverTooltips.equals(textBuffer)) {
                this.serverTooltips = textBuffer;
                writeUpdateInfo(1, buffer -> {
                    buffer.writeVarInt(serverTooltips.size());
                    for (Component component : serverTooltips) {
                        buffer.writeComponent(component);
                    }
                });
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.serverTooltips.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                Component component = buffer.readComponent();
                this.serverTooltips.add(component);
            }
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if ((!tooltipTexts.isEmpty() || !serverTooltips.isEmpty()) && isMouseOverElement(mouseX, mouseY) &&
                getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
            var tips = new ArrayList<>(tooltipTexts);
            tips.addAll(serverTooltips);
            gui.getModularUIGui().setHoverTooltip(tips, ItemStack.EMPTY, null, null);
        }
    }
}
