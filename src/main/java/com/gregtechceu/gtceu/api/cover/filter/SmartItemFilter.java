package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.util.Collections;
import java.util.function.Consumer;

public class SmartItemFilter implements ItemFilter {

    protected Consumer<SmartItemFilter> itemWriter = filter -> {};
    protected Consumer<SmartItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    private SmartFilteringMode filterMode;

    private SmartItemFilter(SmartFilteringMode filterMode) {
        this.filterMode = filterMode;
    }

    public static SmartItemFilter loadFilter(ItemStack itemStack) {
        SmartFilteringMode mode = itemStack.getOrDefault(GTDataComponents.SMART_ITEM_FILTER,
                SmartFilteringMode.ELECTROLYZER);
        SmartItemFilter handler = new SmartItemFilter(mode);
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.SMART_ITEM_FILTER, filter.filterMode);
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    private void setFilterMode(SmartFilteringMode filterMode) {
        this.filterMode = filterMode;
        onUpdated.accept(this);
    }

    @Override
    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3);
        group.addWidget(new EnumSelectorWidget<>(16, 8, 32, 32,
                SmartFilteringMode.VALUES, filterMode, this::setFilterMode));
        return group;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testItemCount(itemStack) > 0;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        return filterMode.cache.computeIfAbsent(itemStack, this::lookup);
    }

    private int lookup(ItemStack itemStack) {
        ItemStack copy = itemStack.copyWithCount(Integer.MAX_VALUE);
        var recipe = filterMode.recipeType.db()
                .find(Collections.singletonMap(ItemRecipeCapability.CAP, Collections.singletonList(copy)), r -> true);
        if (recipe == null) {
            return 0;
        }
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            var stacks = ItemRecipeCapability.CAP.of(content.getContent()).getItems();
            for (var stack : stacks) {
                if (ItemStack.isSameItem(stack, itemStack)) return stack.getCount();
            }
        }
        return 0;
    }

    public void setModeFromMachine(String machineName) {
        for (SmartFilteringMode mode : SmartFilteringMode.VALUES) {
            if (machineName.contains(mode.name)) {
                setFilterMode(mode);
                return;
            }
        }
    }

    public enum SmartFilteringMode implements EnumSelectorWidget.SelectableEnum, StringRepresentable {

        ELECTROLYZER("electrolyzer", GTRecipeTypes.ELECTROLYZER_RECIPES),
        CENTRIFUGE("centrifuge", GTRecipeTypes.CENTRIFUGE_RECIPES),
        SIFTER("sifter", GTRecipeTypes.SIFTER_RECIPES);

        public static final Codec<SmartFilteringMode> CODEC = StringRepresentable.fromEnum(SmartFilteringMode::values);
        private static final SmartFilteringMode[] VALUES = values();
        private final String name;
        private final GTRecipeType recipeType;
        private final Object2IntOpenCustomHashMap<ItemStack> cache = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        SmartFilteringMode(String name, GTRecipeType type) {
            this.name = name;
            this.recipeType = type;
        }

        @Override
        public String getTooltip() {
            return "cover.smart_item_filter.filtering_mode." + name;
        }

        @Override
        public IGuiTexture getIcon() {
            return new ResourceTexture("gtceu:textures/block/machines/" + name + "/overlay_front.png");
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
