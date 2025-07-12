package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PredicatedButtonWidget extends ButtonWidget {

    private final BooleanSupplier predicate;

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                                  Consumer<ClickData> onPressed, BooleanSupplier predicate, boolean defaultVisibility) {
        super(xPosition, yPosition, width, height, buttonTexture, onPressed);
        this.predicate = predicate;
        setVisible(defaultVisibility);
    }

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                                  Consumer<ClickData> onPressed, BooleanSupplier predicate) {
        this(xPosition, yPosition, width, height, buttonTexture, onPressed, predicate, false);
    }

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, Consumer<ClickData> onPressed,
                                  BooleanSupplier predicate) {
        super(xPosition, yPosition, width, height, onPressed);
        this.predicate = predicate;
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
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            setVisible(buffer.readBoolean());
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
