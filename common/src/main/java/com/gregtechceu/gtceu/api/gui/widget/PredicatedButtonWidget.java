package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote DisplayButtonWidget
 */
@Accessors(chain = true)
public class PredicatedButtonWidget extends ButtonWidget {
    @Setter
    private BooleanSupplier predicate;

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height, buttonTexture, onPressed);
    }

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height, onPressed);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        var result = predicate == null || predicate.getAsBoolean();
        setVisible(result);
        buffer.writeBoolean(result);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        setVisible(buffer.readBoolean());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (predicate != null) {
            if (isVisible() != predicate.getAsBoolean()) {
                setVisible(!isVisible());
                writeUpdateInfo(1, buf -> buf.writeBoolean(isVisible()));
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            setVisible(buffer.readBoolean());
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

}
