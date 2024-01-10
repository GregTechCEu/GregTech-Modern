package com.gregtechceu.gtceu.data.lang.forge;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class LangHandlerImpl {

    private LangHandlerImpl() {/**/}

    public static void replace(@NotNull RegistrateLangProvider provider, @NotNull String key, @NotNull String value) {
        try {
            // the regular lang mappings
            Field field = LanguageProvider.class.getDeclaredField("data");
            field.setAccessible(true);
            // noinspection unchecked
            Map<String, String> map = (Map<String, String>) field.get(provider);
            map.put(key, value);

            // upside-down lang mappings
            Field upsideDownField = RegistrateLangProvider.class.getDeclaredField("upsideDown");
            upsideDownField.setAccessible(true);
            // noinspection unchecked
            map = (Map<String, String>) field.get(upsideDownField.get(provider));

            Method toUpsideDown = RegistrateLangProvider.class.getDeclaredMethod("toUpsideDown", String.class);
            toUpsideDown.setAccessible(true);

            map.put(key, (String) toUpsideDown.invoke(provider, value));
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error replacing entry in datagen.", e);
        }
    }
}
