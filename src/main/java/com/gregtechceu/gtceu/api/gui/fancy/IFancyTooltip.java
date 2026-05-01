package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface IFancyTooltip {

    IGuiTexture getFancyTooltipIcon();

    List<Component> getFancyTooltip();

    default boolean showFancyTooltip() {
        return true;
    }

    @Nullable
    default TooltipComponent getFancyComponent() {
        return null;
    }

    record Basic(Supplier<IGuiTexture> icon, Supplier<List<Component>> content, BooleanSupplier predicate,
                 Supplier<TooltipComponent> componentSupplier)
            implements IFancyTooltip {

        @Override
        public IGuiTexture getFancyTooltipIcon() {
            return icon.get();
        }

        @Override
        public List<Component> getFancyTooltip() {
            return content.get();
        }

        @Override
        public @Nullable TooltipComponent getFancyComponent() {
            return componentSupplier.get();
        }

        @Override
        public boolean showFancyTooltip() {
            return predicate.getAsBoolean();
        }
    }
}
