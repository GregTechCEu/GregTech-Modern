package com.gregtechceu.gtceu.utils.serialization.network;

import com.gregtechceu.gtceu.utils.EqualityTest;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public interface IByteBufAdapter<T> extends IByteBufSerializer<T>, IByteBufDeserializer<T>, EqualityTest<T> {

    @Override
    T deserialize(FriendlyByteBuf buffer);

    @Override
    void serialize(FriendlyByteBuf buffer, T u);

    @Override
    boolean areEqual(@NotNull T t1, @NotNull T t2);
}
