package brachy.modularui.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LangUtil {

    private LangUtil() {}

    /**
     * Gets all translation components from a multi lang's sub-keys.<br>
     * E.g., given a multi lang:
     *
     * <pre>
     * <code>addMultiline(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code>
     * </pre>
     * <p>
     * The following code can be used to print out the translations:
     *
     * <pre>
     * <code>for (var component : getMultiline("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }</code>
     * </pre>
     * <p>
     * Result:
     *
     * <pre>
     * <code>radius size 1
     * radius size 2
     * radius size 3</code>
     * </pre>
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys
     */
    public static List<Component> getMultiline(String key) {
        ArrayList<String> outputKeys = new ArrayList<>();
        int i = 0;
        String next = subKey(key, i);
        while (Language.getInstance().has(next)) {
            outputKeys.add(next);
            next = subKey(key, ++i);
        }
        return outputKeys.stream().map(Component::translatable).collect(Collectors.toList());
    }

    /**
     * See {@link #getMultiline(String)}. If no multiline key is available, get
     * single instead.
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys.
     */
    public static List<Component> getSingleOrMultiline(String key) {
        List<Component> multiLang = getMultiline(key);
        if (!multiLang.isEmpty()) {
            return multiLang;
        }
        return List.of(Component.translatable(key));
    }

    /**
     * Gets a single translation from a multi lang.
     *
     * @param key   Base key of the multi lang. E.g. "modularui.gui.overclock.enabled".
     * @param index Index of the single translation. E.g. 3 would return
     *              "modularui.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiline(String key, int index) {
        return Component.translatable(subKey(key, index));
    }

    /**
     * Gets a single translation from a multi lang. Supports additional arguments
     * for the translation component.
     *
     * @param key   Base key of the multi lang. E.g. "modularui.gui.overclock.enabled".
     * @param index Index of the single translation. E.g. 3 would return
     *              "modularui.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiline(String key, int index, Object... args) {
        return Component.translatable(subKey(key, index), args);
    }

    /**
     * Returns the sub-key consisting of the given key plus the given index.<br>
     * E.g.,<br>
     *
     * <pre>
     * <code>subKey("terminal.fluid_prospector.tier", 0)</code>
     * </pre>
     * <p>
     * returns the <code>String</code>:
     *
     * <pre>
     * <code>
     * "terminal.fluid_prospector.tier.0"</code>
     * </pre>
     *
     * @param key   Base key of the sub-key.
     * @param index Index of the sub-key.
     * @return Sub-key consisting of key and index.
     */
    public static String subKey(String key, int index) {
        return key + "." + index;
    }

    public static Component getFluidModName(FluidStack fluidStack) {
        String namespace = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).getNamespace();
        return getFormattedModName(namespace);
    }

    public static Component getFormattedModName(String namespace) {
        return Component.literal(getModName(namespace)).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }

    public static String getModName(String namespace) {
        if (namespace.equals("c")) {
            return "Common";
        }

        Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
        if (container.isPresent()) {
            return container.get().getModInfo().getDisplayName();
        }
        container = ModList.get().getModContainerById(namespace.replace('_', '-'));
        if (container.isPresent()) {
            return container.get().getModInfo().getDisplayName();
        }
        return FormattingUtil.capitalizeFully(namespace.replace('_', ' '));
    }
}
