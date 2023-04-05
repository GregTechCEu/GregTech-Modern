package com.gregtechceu.gtceu.data.lang.fabric;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public class LangHandlerImpl {

    private LangHandlerImpl() {/**/}

    public static void replace(@NotNull RegistrateLangProvider provider, @NotNull String key, @NotNull String value) {
        provider.add(key, value);
    }
}
