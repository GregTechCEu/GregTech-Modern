package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public abstract class BaseKey implements IKey {

    @Getter
    private @Nullable FormattingState formatting;

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        return FontRenderHelper.format(this.formatting, parentFormatting, get());
    }

    @Override
    public BaseKey style(@Nullable ChatFormatting formatting) {
        if (this.formatting == null) {
            this.formatting = new FormattingState();
        }
        if (formatting == null) this.formatting.forceDefaultColor();
        else this.formatting.add(formatting, false);
        return this;
    }

    @Override
    public IKey removeStyle() {
        if (this.formatting != null) {
            this.formatting.reset();
        }
        return this;
    }

    @Override
    public String toString() {
        return getFormatted().getString();
    }

    @Override
    public int hashCode() {
        throw new NotImplementedException("Implement hashCode() in subclasses");
    }
}
