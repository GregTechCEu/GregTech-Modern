package com.gregtechceu.gtceu.data.lang;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LangUtil {

    /**
     * Returns the sub-key consisting of the given key plus the given index.<br>
     * E.g., {@code getSubKey("terminal.fluid_prospector.tier", 0)} returns the string<br>
     * {@code "terminal.fluid_prospector.tier.0"}
     *
     * @param key   Base key of the sub-key.
     * @param index Index of the sub-key
     * @return Sub-key consisting of key and index
     */
    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }

    protected static Stream<String> getMultiLangKeys(String key) {
        var output = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (Language.getInstance().has(next)) {
            output.add(next);
            next = getSubKey(key, ++i);
        }
        return output.stream();
    }

    /**
     * Gets all translation strings from a multi lang's sub-keys.<br>
     * E.g., given a multi lang
     * {@code provider.addMultiLang("terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");},<br>
     * The following code can be used to print out the translations:
     * <pre> {@code
     * for (var component : getMultiLang("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }
     * } </pre>
     * And the output would be:
     * <pre> {@code
     * radius size 1
     * radius size 2
     * radius size 3
     * } </pre>
     *
     * @param key Base key of the multi lang. E.g. {@code "terminal.fluid_prospector.tier"}
     * @return Returns all translation components from a multi lang's sub-keys
     */
    public static Component[] getMultiLang(String key) {
        return getListMultiLang(key).toArray(Component[]::new);
    }

    /**
     * A variation of {@link #getMultiLang(String)} that returns a list instead of an array.
     *
     * @param key Base key of the multi lang. E.g. {@code "terminal.fluid_prospector.tier"}
     * @return Returns all translation components from a multi lang's sub-keys
     * @see #getMultiLang(String)
     */
    public static List<Component> getListMultiLang(String key) {
        return getMultiLangKeys(key).map(Component::translatable).collect(Collectors.toList());
    }

    /**
     * Gets all translation strings from a multi lang's sub-keys.<br>
     * E.g., given a multi lang
     * {@code provider.addMultiLang("terminal.fluid_prospector.info", "prospection radius %s", "Can prospect %s");},<br>
     * The following code can be used to print out the translations:
     * <pre> {@code
     * for (var component : getMultiLang("terminal.fluid_prospector.info", 5, "Ores")) {
     *     System.out.println(component.getString());
     * }
     * } </pre>
     * And the output would be:
     * <pre> {@code
     * prospection radius 5
     * Can prospect Ores
     * } </pre>
     *
     * @param key  Base key of the multi lang. E.g. {@code "terminal.fluid_prospector.info"}
     * @param args The arguments to pass to each {@link Component} of this multi lang
     * @return Returns all translation components from a multi lang's sub-keys
     */
    public static Component[] getMultiLang(String key, Object... args) {
        return getMultiLangKeys(key).map(k -> Component.translatable(k, args)).toArray(Component[]::new);
    }

    /**
     * A variation of {@link #getMultiLang(String, Object...)} )} that returns a list instead of an array.
     *
     * @param key Base key of the multi lang. E.g. {@code "terminal.fluid_prospector.info"}
     * @param args The arguments to pass to each {@link Component} of this multi lang
     * @return Returns all translation components from a multi lang's sub-keys
     * @see #getMultiLang(String, Object...)
     */
    public static List<Component> getListMultiLang(String key, Object... args) {
        return getMultiLangKeys(key).map(k -> Component.translatable(k, args)).collect(Collectors.toList());
    }

    /**
     * Returns a multi lang if one is available, and if it isn't, gets a singular value instead.
     *
     * @param key Base key of the multi lang. E.g. {@code "terminal.fluid_prospector.tier"}.
     * @return Returns all translation components from a multi lang's sub-keys
     * @see #getMultiLang(String)
     */
    public static Component[] getSingleOrMultiLang(String key) {
        Component[] multiLang = getMultiLang(key);

        if (multiLang.length > 0) {
            return multiLang;
        }

        return new Component[] { Component.translatable(key) };
    }

    /**
     * Gets a single translation from a multi lang.
     *
     * @param key   Base key of the multi lang. E.g. {@code "gtceu.gui.overclock.enabled"}
     * @param index Index of the single translation. E.g. 3 would return {@code "gtceu.gui.overclock.enabled.3"}
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index) {
        return Component.translatable(getSubKey(key, index));
    }

    /**
     * Gets a single translation from a multi lang. Supports additional arguments
     * for the translation component.
     *
     * @param key   Base key of the multi lang. E.g. {@code "gtceu.gui.overclock.enabled"}
     * @param index Index of the single translation. E.g. 3 would return {@code "gtceu.gui.overclock.enabled.3"}
     * @param args  The arguments to pass to the component
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index, Object... args) {
        return Component.translatable(getSubKey(key, index), args);
    }
}
