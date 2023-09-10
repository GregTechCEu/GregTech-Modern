package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StatusWidget extends Widget {

    private final Supplier<Status> statusSupplier;

    public StatusWidget(int x, int y, Supplier<Status> statusSupplier) {
        super(x, y, 16, 16);

        this.statusSupplier = statusSupplier;
    }

    @Override
    public void updateScreen() {
        var status = statusSupplier.get();

        setBackground(status.type.icon);
        setHoverTooltips(status.type != StatusType.NONE ? status.message : List.of());

        super.updateScreen();
    }

    public record Status(StatusType type, List<Component> message) {
    }

    public enum StatusType {
        OK("ok"),
        WARNING("warning"),
        ERROR("error"),
        INFO("info"),
        UNKNOWN("unknown"),
        NONE(IGuiTexture.EMPTY);

        private final IGuiTexture icon;

        StatusType(@Nullable String textureName) {
            this(new ResourceTexture("gtceu:textures/gui/icon/status/" + textureName + ".png"));
        }

        StatusType(IGuiTexture icon) {
            this.icon = icon;
        }

        public Status toStatus() {
            return new Status(this, List.of());
        }

        public Status toStatus(List<Component> message) {
            return new Status(this, message);
        }
    }
}
