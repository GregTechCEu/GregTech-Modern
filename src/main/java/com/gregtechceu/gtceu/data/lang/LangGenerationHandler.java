package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handles lang generation for GT registry content on 1.20
 */
public class LangGenerationHandler {

    private static final Map<String, LangGenerationHandler> namespaces = new Object2ObjectOpenHashMap<>();

    public static LangGenerationHandler forNamespace(String namespace) {
        return namespaces.computeIfAbsent(namespace, key -> {
            var handler = new LangGenerationHandler();
            GTRegistrate.createIgnoringListenerErrors(key).addDataGenerator(ProviderType.LANG, handler::generate);
            return handler;
        });
    }

    private final List<Consumer<GTLangProvider>> providers = new ArrayList<>();

    public void add(Consumer<GTLangProvider> provider) {
        providers.add(provider);
    }

    private void generate(RegistrateLangProvider provider) {
        var gtProvider = (GTLangProvider) provider;
        providers.forEach(v -> v.accept(gtProvider));
    }
}
