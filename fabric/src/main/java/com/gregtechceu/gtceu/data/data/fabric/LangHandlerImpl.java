package com.gregtechceu.gtceu.data.data.fabric;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public final class LangHandlerImpl {

    private LangHandlerImpl() {/**/}

    public static void replace(@NotNull RegistrateLangProvider provider, @NotNull String key, @NotNull String value) {
        provider.add(key, value);
    }
}
