package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SmartItemFilter implements ItemFilter {

    protected Consumer<ItemFilter> itemWriter = filter -> {};
    protected Consumer<ItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Setter
    private SmartFilteringMode filterMode = SmartFilteringMode.ELECTROLYZER;

    protected SmartItemFilter() {}

    public static SmartItemFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static SmartItemFilter loadFilter(CompoundTag tag, Consumer<ItemFilter> itemWriter) {
        var handler = new SmartItemFilter();
        handler.itemWriter = itemWriter;
        handler.filterMode = SmartFilteringMode.VALUES[tag.getInt("filterMode")];
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public CompoundTag saveFilter() {
        var tag = new CompoundTag();
        tag.putInt("filterMode", filterMode.ordinal());
        return tag;
    }

    @Override
    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18*3+25, 18*3);
        group.addWidget(new EnumSelectorWidget<>(0, 0, 20, 20, SmartFilteringMode.VALUES, filterMode, this::setFilterMode));
        return group;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testItemCount(itemStack) > 0;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        int recipeCount = filterMode.cache.getOrDefault(itemStack, -1);
        if (recipeCount == -1) {
            ItemStack copy = itemStack.copy();
            copy.setCount(Integer.MAX_VALUE);
            var ingredients = ItemRecipeCapability.CAP.convertToMapIngredient(copy);
            var recipe = filterMode.lookup.recurseIngredientTreeFindRecipe(List.of(ingredients), filterMode.lookup.getLookup(), r -> true, 0, 0, 1);
            if(recipe == null) {
                filterMode.cache.put(itemStack, 0);
                return 0;
            }
            var stack = ItemRecipeCapability.CAP.of(recipe.getInputContents(ItemRecipeCapability.CAP).get(0).getContent()).getItems()[0];
            filterMode.cache.put(itemStack, stack.getCount());
            return stack.getCount();
        }
        return recipeCount;
    }

    public void setModeFromMachine(String machineName) {
        for(SmartFilteringMode mode : SmartFilteringMode.VALUES) {
            if(machineName.contains(mode.localeName)) {
                this.filterMode = mode;
                return;
            }
        }
    }
    public static String[] modes() { return Arrays.stream(SmartFilteringMode.VALUES).map(Enum::name).toArray(String[]::new);}

    private enum SmartFilteringMode implements EnumSelectorWidget.SelectableEnum {
        ELECTROLYZER("electrolyzer", GTRecipeTypes.ELECTROLYZER_RECIPES),
        CENTRIFUGE("centrifuge", GTRecipeTypes.CENTRIFUGE_RECIPES),
        SIFTER("sifter", GTRecipeTypes.SIFTER_RECIPES);

        private static final SmartFilteringMode[] VALUES = values();
        private final GTRecipeLookup lookup;
        private final Object2IntOpenCustomHashMap<ItemStack> cache =
                new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        private String localeName;

        SmartFilteringMode(String localeName, GTRecipeType type) {
            lookup = type.getLookup();
            this.localeName = localeName;
        }

        @Override
        public String getTooltip() {
            return localeName;
        }

        @Override
        public IGuiTexture getIcon() {
            return new ResourceTexture("gtceu:textures/block/machines/" + localeName + "/overlay_front.png");
        }
    }

}
