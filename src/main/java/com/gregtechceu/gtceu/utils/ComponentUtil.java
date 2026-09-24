package com.gregtechceu.gtceu.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.demonwav.mcdev.annotations.Translatable;

public class ComponentUtil {

    /**
     * Adds a given string to the beginning and end of a component.
     * 
     * @param component The component to append/preprend to.
     * @param str       The string to append/prepend.
     */
    public static MutableComponent wrap(Component component, String str) {
        return Component.literal(str).append(component).append(str);
    }

    /**
     * Adds a given string to the beginning and end of a component.
     * 
     * @param component The component to append/preprend to.
     * @param prefix    The string to preprend.
     * @param postfix   The string to append.
     */
    public static MutableComponent wrap(Component component, String prefix, String postfix) {
        return Component.literal(prefix).append(component).append(postfix);
    }

    /**
     * Preprends a value to the start of a component.<br>
     * e.g. {@code prepend(Component.translatable("common.gtceu.mb_per_tick"), 100)} returns {@code "100 mB/t"}
     */
    public static MutableComponent prepend(Component component, Object val) {
        return Component.literal(val.toString() + " ").append(component);
    }

    /**
     * Preprends a value to the start of a component.<br>
     * e.g. {@code prepend("common.gtceu.mb_per_tick", 100)} returns {@code "100 mB/t"}
     */
    public static MutableComponent prepend(@Translatable String langKey, Object val) {
        return Component.literal(val.toString() + " ").append(Component.translatable(langKey));
    }
}
