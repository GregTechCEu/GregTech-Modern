package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class LangKey extends BaseKey {

    @Getter
    private final @Nullable Supplier<@NotNull String> keySupplier;
    @Getter
    private final @Nullable Supplier<@NotNull Object @Nullable []> argsSupplier;
    private MutableComponent component;
    private long time = -1;

    public LangKey(@NotNull String key) {
        this(key, () -> null);
    }

    public LangKey(@NotNull Component component) {
        this.component = component.copy();

        this.keySupplier = null;
        this.argsSupplier = null;
    }

    public LangKey(@NotNull String key, @Nullable Object[] args) {
        this(() -> Objects.requireNonNull(key), () -> args == null || args.length == 0 ? null : args);
    }

    public LangKey(@NotNull String key, @NotNull Supplier<Object[]> argsSupplier) {
        this(() -> Objects.requireNonNull(key), argsSupplier);
    }

    public LangKey(@NotNull Supplier<String> keySupplier) {
        this(keySupplier, () -> null);
    }

    public LangKey(@Nullable Supplier<String> keySupplier, @Nullable Supplier<Object[]> argsSupplier) {
        this.keySupplier = Objects.requireNonNull(keySupplier);
        this.argsSupplier = Objects.requireNonNull(argsSupplier);
    }

    @Override
    public MutableComponent get() {
        if (keySupplier == null || argsSupplier == null) {
            return component;
        }
        if (this.time == ClientScreenHandler.getTicks()) {
            return this.component;
        }
        this.time = ClientScreenHandler.getTicks();

        String key = Objects.requireNonNull(this.keySupplier.get());
        Object[] args = this.argsSupplier.get();
        if (args != null) {
            this.component = Component.translatable(key, args);
        } else {
            this.component = Component.translatable(key);
        }
        return component;
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        if (keySupplier == null || argsSupplier == null) {
            return component;
        }

        Object[] args = this.argsSupplier.get();
        if (args == null || args.length == 0) return super.getFormatted(parentFormatting);
        String text = Objects.requireNonNull(this.keySupplier.get());
        Component formatted = FontRenderHelper.formatArgs(args,
                FormattingState.merge(parentFormatting, getFormatting()), text, true);
        return FontRenderHelper.format(getFormatting(), parentFormatting, formatted);
    }
}
