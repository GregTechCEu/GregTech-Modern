package com.gregtechceu.gtceu.utils.lang;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Accessors(fluent = true)
public class GTCustomTooltip {

    protected final Map<DisplayType, List<TooltipEntry>> tooltips = new Object2ObjectOpenHashMap<>();
    protected final Map<DisplayType, CustomTooltipBuilder> customTooltipBuilders = new Object2ObjectOpenHashMap<>();

    /**
     * The default component style, applied to all components added by this tooltip if they don't have a style already.
     */
    @Getter
    @Setter
    private @Nullable Style defaultComponentStyle = Style.EMPTY.withColor(ChatFormatting.GRAY);

    /**
     * The prompt displayed when holding shift displays extra tooltips.
      */
    @Getter
    @Setter
    private @Nullable Component shiftInfoPrompt = GTCommonComponents.HOLD_SHIFT_FOR_INFO;

    /**
     * The prompt displayed when holding ctrl displays extra tooltips.
     */
    @Getter
    @Setter
    private @Nullable Component ctrlInfoPrompt = GTCommonComponents.HOLD_CTRL_FOR_INFO;

    /**
     * The prompt displayed when holding alt displays extra tooltips.
     */
    @Getter
    @Setter
    private @Nullable Component altInfoPrompt = null;

    /**
     * Adds a custom tooltip function which can add/modify all tooltips.
     * @param type The display type when this custom tooltip function should be called.
     * @param customTooltipBuilder The custom tooltip builder.
     */
    public GTCustomTooltip customTooltipBuilder(DisplayType type, CustomTooltipBuilder customTooltipBuilder) {
        customTooltipBuilders.put(type, customTooltipBuilder);
        return this;
    }

    /**
     * Adds a custom tooltip function which can add/modify all tooltips.
     * @param customTooltipBuilder The custom tooltip builder.
     */
    public GTCustomTooltip customTooltipBuilder(CustomTooltipBuilder customTooltipBuilder) {
        customTooltipBuilders.put(DisplayType.NORMAL, customTooltipBuilder);
        return this;
    }

    /**
     * Adds a tooltip entry
     * @param type The display type of this tooltip line
     * @param tooltip The tooltip entry
     */
    public GTCustomTooltip tooltip(DisplayType type, TooltipEntry tooltip) {
        tooltips.computeIfAbsent(type, v -> new ObjectArrayList<>()).add(tooltip);
        return this;
    }

    /**
     * Adds a tooltip
     * @param type The display type of this tooltip line
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip tooltip(DisplayType type, Component tooltip) {
        return tooltip(type, new TooltipEntry(tooltip));
    }

    /**
     * Adds a dynamic tooltip
     * @param type The display type of this tooltip line
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip tooltip(DisplayType type, Function<ItemStack, Component> tooltipFunc) {
        return tooltip(type, new TooltipEntry(tooltipFunc));
    }

    /**
     * Adds a tooltip with normal display type.
     * @param tooltip The tooltip entry
     */
    public GTCustomTooltip tooltip(TooltipEntry tooltip) {
        return tooltip(DisplayType.NORMAL, tooltip);
    }

    /**
     * Adds a tooltip with normal display type.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip tooltip(Component tooltip) {
        return tooltip(new TooltipEntry(tooltip));
    }

    /**
     * Adds a dynamic tooltip with normal display type.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip tooltip(Function<ItemStack, Component> tooltipFunc) {
        return tooltip(new TooltipEntry(tooltipFunc));
    }

    /**
     * Adds a conditional tooltip with normal display type.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip conditionalTooltip(Predicate<ItemStack> predicate, Component tooltip) {
        return tooltip(new TooltipEntry(tooltip, predicate));
    }

    /**
     * Adds a conditional dynamic tooltip with normal display type.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltipFunc The tooltip function.
     */
    public GTCustomTooltip conditionalTooltip(Predicate<ItemStack> predicate, Function<ItemStack, Component> tooltipFunc) {
        return tooltip(new TooltipEntry(tooltipFunc, predicate));
    }

    /**
     * Adds multiple components as tooltips with normal display type.
     * @param tooltips The components to add.
     */
    public GTCustomTooltip tooltips(Component... tooltips) {
        for (var tooltip: tooltips) {
            tooltip(tooltip);
        }
        return this;
    }

    /**
     * Adds a tooltip displayed when shift is held.
     * @param tooltip The tooltip entry
     */
    public GTCustomTooltip shiftTooltip(TooltipEntry tooltip) {
        return tooltip(DisplayType.SHIFT, tooltip);
    }

    /**
     * Adds a tooltip displayed when shift is held.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip shiftTooltip(Component tooltip) {
        return shiftTooltip(new TooltipEntry(tooltip));
    }

    /**
     * Adds a dynamic tooltip displayed when shift is held.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip shiftTooltip(Function<ItemStack, Component> tooltipFunc) {
        return shiftTooltip(new TooltipEntry(tooltipFunc));
    }

    /**
     * Adds a conditional tooltip displayed when shift is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip conditionalShiftTooltip(Predicate<ItemStack> predicate, Component tooltip) {
        return shiftTooltip(new TooltipEntry(tooltip, predicate));
    }

    /**
     * Adds a conditional dynamic tooltip displayed when shift is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip conditionalShiftTooltip(Predicate<ItemStack> predicate, Function<ItemStack, Component> tooltipFunc) {
        return shiftTooltip(new TooltipEntry(tooltipFunc, predicate));
    }

    /**
     * Adds multiple components displayed when shift is held
     * @param tooltips The components to add.
     */
    public GTCustomTooltip shiftTooltips(Component... tooltips) {
        for (var tooltip: tooltips) {
            shiftTooltip(tooltip);
        }
        return this;
    }

    /**
     * Adds a tooltip displayed when ctrl is held.
     * @param tooltip The tooltip entry
     */
    public GTCustomTooltip ctrlTooltip(TooltipEntry tooltip) {
        return tooltip(DisplayType.CTRL, tooltip);
    }

    /**
     * Adds a tooltip displayed when ctrl is held.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip ctrlTooltip(Component tooltip) {
        return ctrlTooltip(new TooltipEntry(tooltip));
    }

    /**
     * Adds a dynamic tooltip displayed when ctrl is held.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip ctrlTooltip(Function<ItemStack, Component> tooltipFunc) {
        return ctrlTooltip(new TooltipEntry(tooltipFunc));
    }

    /**
     * Adds a conditional tooltip displayed when ctrl is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip conditionalCtrlTooltip(Predicate<ItemStack> predicate, Component tooltip) {
        return ctrlTooltip(new TooltipEntry(tooltip, predicate));
    }

    /**
     * Adds a conditional dynamic tooltip displayed when ctrl is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip conditionalCtrlTooltip(Predicate<ItemStack> predicate, Function<ItemStack, Component> tooltipFunc) {
        return ctrlTooltip(new TooltipEntry(tooltipFunc, predicate));
    }

    /**
     * Adds multiple components displayed when ctrl is held
     * @param tooltips The components to add.
     */
    public GTCustomTooltip ctrlTooltips(Component... tooltips) {
        for (var tooltip: tooltips) {
            ctrlTooltip(tooltip);
        }
        return this;
    }


    /**
     * Adds a tooltip displayed when alt is held.
     * @param tooltip The tooltip entry
     */
    public GTCustomTooltip altTooltip(TooltipEntry tooltip) {
        return tooltip(DisplayType.ALT, tooltip);
    }

    /**
     * Adds a tooltip displayed when alt is held.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip altTooltip(Component tooltip) {
        return altTooltip(new TooltipEntry(tooltip));
    }

    /**
     * Adds a dynamic tooltip displayed when alt is held.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip altTooltip(Function<ItemStack, Component> tooltipFunc) {
        return altTooltip(new TooltipEntry(tooltipFunc));
    }

    /**
     * Adds a conditional tooltip displayed when alt is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltip The tooltip component
     */
    public GTCustomTooltip conditionalAltTooltip(Predicate<ItemStack> predicate, Component tooltip) {
        return altTooltip(new TooltipEntry(tooltip, predicate));
    }

    /**
     * Adds a conditional dynamic tooltip displayed when alt is held.
     * @param predicate A predicate determining if this tooltip should be displayed.
     * @param tooltipFunc The tooltip function
     */
    public GTCustomTooltip conditionalAltTooltip(Predicate<ItemStack> predicate, Function<ItemStack, Component> tooltipFunc) {
        return altTooltip(new TooltipEntry(tooltipFunc, predicate));
    }

    /**
     * Adds multiple components displayed when alt is held
     * @param tooltips The components to add.
     */
    public GTCustomTooltip altTooltips(Component... tooltips) {
        for (var tooltip: tooltips) {
            altTooltip(tooltip);
        }
        return this;
    }

    public Map<DisplayType, List<TooltipEntry>> getTooltipEntries() {
        return tooltips;
    }

    public void addTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltipList,
                       TooltipFlag flag) {
        addForType(DisplayType.NORMAL, stack, level, tooltipList);
        if (tooltips.containsKey(DisplayType.SHIFT) && !tooltips.get(DisplayType.SHIFT).isEmpty()) {
            if (GTUtil.isShiftDown()) {
                addForType(DisplayType.SHIFT, stack, level, tooltipList);
            } else if (shiftInfoPrompt != null) {
                tooltipList.add(shiftInfoPrompt);
            }
        }

        if (tooltips.containsKey(DisplayType.CTRL) && !tooltips.get(DisplayType.CTRL).isEmpty()) {
            if (GTUtil.isCtrlDown()) {
                addForType(DisplayType.CTRL, stack, level, tooltipList);
            } else if (ctrlInfoPrompt != null) {
                tooltipList.add(ctrlInfoPrompt);
            }
        }

        if (tooltips.containsKey(DisplayType.ALT) && !tooltips.get(DisplayType.ALT).isEmpty()) {
            if (GTUtil.isAltDown()) {
                addForType(DisplayType.ALT, stack, level, tooltipList);
            } else if (altInfoPrompt != null) {
                tooltipList.add(altInfoPrompt);
            }
        }

        if (flag.isAdvanced()) addForType(DisplayType.ADVANCED, stack, level, tooltipList);
    }

    protected void addForType(DisplayType type, ItemStack stack, @Nullable BlockGetter level, List<Component> tooltipList) {
        var tooltips = this.tooltips.computeIfAbsent(type, v -> new ObjectArrayList<>());

        for (var tooltip: tooltips) {
            var component = tooltip.get(stack);
            if (component == null) continue;
            if (component.getStyle().isEmpty() && defaultComponentStyle != null) {
                component = component.copy().withStyle(defaultComponentStyle);
            }
            tooltipList.add(component);
        }

        var customTooltipBuilder = customTooltipBuilders.get(type);
        if (customTooltipBuilder != null) {
            customTooltipBuilder.addTooltips(stack, level, tooltipList);
        }
    }

    public enum DisplayType {
        /**
         * Normal tooltip
         */
        NORMAL,
        /**
         * Tooltip displayed when shift is held
         */
        SHIFT,
        /**
         * Tooltip displayed when control is held
         */
        CTRL,
        /**
         * Tooltip displayed when alt is held.
         */
        ALT,
        /**
         * Tooltip displayed when advanced tooltips are enabled
         */
        ADVANCED
    }

    public record TooltipEntry(Either<Component, Function<ItemStack, Component>> component, @Nullable Predicate<ItemStack> condition) {

        public TooltipEntry(Component component, @Nullable Predicate<ItemStack> condition) {
            this(Either.left(component), condition);
        }

        public TooltipEntry(Component component) {
            this(Either.left(component), null);
        }

        public TooltipEntry(Function<ItemStack, Component> componentFunc, @Nullable Predicate<ItemStack> condition) {
            this(Either.right(componentFunc), condition);
        }

        public TooltipEntry(Function<ItemStack, Component> componentFunc) {
            this(Either.right(componentFunc), null);
        }

        public @Nullable Component get(ItemStack stack) {
            if (condition != null && !condition.test(stack)) return null;
            return component.map(UnaryOperator.identity(), f -> f.apply(stack));
        }
    }
}
