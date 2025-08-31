package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class CompoundKey extends BaseKey {

    private static final IKey[] EMPTY = new IKey[0];

    @Getter
    private final IKey[] keys;

    public CompoundKey(IKey... keys) {
        this.keys = keys == null || keys.length == 0 ? EMPTY : keys;
    }

    @Override
    public MutableComponent get() {
        return toComponent(false, null);
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        // formatting is prepended to each key
        return toComponent(true, parentFormatting);
    }

    private MutableComponent toComponent(boolean formatted, @Nullable FormattingState parentFormatting) {
        MutableComponent builder = Component.empty();
        for (IKey key : this.keys) {
            if (formatted) {
                // merge parent formatting and this formatting to not lose info
                builder.append(key.getFormatted(FormattingState.merge(parentFormatting, getFormatting())));
            } else {
                builder.append(key.get());
            }
        }
        return builder;
    }
}
