package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.sync.IStringSyncValue;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringSyncValue extends AbstractGenericSyncValue<String> implements IStringSyncValue<String> {

    public StringSyncValue(Supplier<String> getter, Consumer<String> setter) {
        super(String.class, getter, setter);
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
        super(String.class, clientGetter, clientSetter, serverGetter, serverSetter);
    }

    @Override
    protected String createDeepCopyOf(String value) {
        return value;
    }

    @Override
    protected boolean areEqual(String a, String b) {
        return Objects.equals(a, b);
    }

    @Override
    protected void serialize(FriendlyByteBuf buffer, String value) {
        NetworkUtils.writeStringSafe(buffer, value, Short.MAX_VALUE - 74);
    }

    @Override
    protected String deserialize(FriendlyByteBuf buffer) {
        return NetworkUtils.readStringSafe(buffer);
    }

    @Override
    public String getStringValue() {
        return getValue();
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setValue(value, setSource, sync);
    }
}
