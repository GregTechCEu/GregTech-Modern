package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote PredicatedImageWidget
 */
@Accessors(chain = true)
public class PredicatedImageWidget extends ImageWidget {
    @Setter
    private BooleanSupplier predicate;
    private boolean isVisible = true;

    public PredicatedImageWidget(int xPosition, int yPosition, int width, int height, IGuiTexture area) {
        super(xPosition, yPosition, width, height, area);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        isVisible = predicate == null || predicate.getAsBoolean();
        buffer.writeBoolean(isVisible);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        isVisible = buffer.readBoolean();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (predicate != null) {
            if (isVisible != predicate.getAsBoolean()) {
                isVisible = !isVisible;
                writeUpdateInfo(1, buf -> buf.writeBoolean(isVisible));
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            isVisible = buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (isVisible) {
            super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
