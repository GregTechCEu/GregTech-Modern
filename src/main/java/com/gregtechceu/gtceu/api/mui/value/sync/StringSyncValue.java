package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IStringSyncValue;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringSyncValue extends ValueSyncHandler<String> implements IStringSyncValue<String> {

    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private String cache;

    public StringSyncValue(@NotNull Supplier<String> getter, @Nullable Consumer<String> setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    public StringSyncValue(@NotNull Supplier<String> getter) {
        this(getter, (Consumer<String>) null);
    }

    @Contract("null, null -> fail")
    public StringSyncValue(@Nullable Supplier<String> clientGetter,
                           @Nullable Supplier<String> serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public StringSyncValue(@Nullable Supplier<String> clientGetter, @Nullable Consumer<String> clientSetter,
                           @Nullable Supplier<String> serverGetter, @Nullable Consumer<String> serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (GTCEu.isClientThread()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.get();
    }

    @Override
    public String getValue() {
        return this.cache;
    }

    @Override
    public String getStringValue() {
        return this.cache;
    }

    @Override
    public void setValue(String value, boolean setSource, boolean sync) {
        setStringValue(value, setSource, sync);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || !Objects.equals(this.getter.get(), this.cache)) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.getter.get(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        NetworkUtils.writeStringSafe(buffer, getValue(), Short.MAX_VALUE - 74);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(NetworkUtils.readStringSafe(buffer), true, false);
    }
}
