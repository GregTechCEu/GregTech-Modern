package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;

import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DynamicKey extends BaseKey {

    private final Supplier<IKey> supplier;

    public DynamicKey(Supplier<IKey> supplier) {
        // Objects.requireNonNull(supplier.get(), "IKey returns a null string!");
        this.supplier = supplier;
    }

    @Override
    public MutableComponent get() {
        return toString(false, null);
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        // formatting is prepended to each key
        return toString(true, parentFormatting);
    }

    private MutableComponent toString(boolean formatted, @Nullable FormattingState parentFormatting) {
        IKey key = this.supplier.get();
        if (key == null) key = IKey.EMPTY;
        if (formatted) {
            // merge parent formatting and this formatting to no lose info
            return key.getFormatted(FormattingState.merge(parentFormatting, getFormatting()));
        } else {
            return key.get();
        }
    }
}
