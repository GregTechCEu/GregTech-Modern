package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class SmartItemFilter implements ItemFilter {

    protected Consumer<ItemFilter> itemWriter = filter -> {};
    protected Consumer<ItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
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
    public boolean isBlank() {
        return filterMode.ordinal() == 0;
    }

    @Override
    public CompoundTag saveFilter() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putInt("filterMode", filterMode.ordinal());
        return tag;
    }

    private void setFilterMode(SmartFilteringMode filterMode) {
        this.filterMode = filterMode;
        onUpdated.accept(this);
    }

    @Override
    public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        EnumSyncValue<SmartFilteringMode> mode = new EnumSyncValue<>(SmartFilteringMode.class,
                this::getFilterMode, this::setFilterMode);

        syncManager.syncValue("mode", mode);

        return new Dialog<>("smart_item_filter")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .child(GTMuiWidgets.createTitleBar(GTItems.SMART_ITEM_FILTER.asStack(), 176, GTGuiTextures.BACKGROUND))
                .child(new GTMuiWidgets.EnumRowBuilder<>(SmartFilteringMode.class)
                        .value(mode)
                        .overlay(16, SmartFilteringMode.getTextures())
                        .lang(Text.dynamic(() -> Component.translatable(filterMode.localeName)))
                        .build().margin(7))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
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
            var stacks = ItemRecipeCapability.CAP.of(content.content()).getItems();
            for (var stack : stacks) {
                if (ItemStack.isSameItem(stack, itemStack)) return stack.getCount();
            }
        }
        return 0;
    }

    public void setModeFromMachine(String machineName) {
        for (SmartFilteringMode mode : SmartFilteringMode.VALUES) {
            if (machineName.contains(mode.localeName)) {
                setFilterMode(mode);
                return;
            }
        }
    }

    @MethodsReturnNonnullByDefault
    private enum SmartFilteringMode {

        ELECTROLYZER("electrolyzer", GTRecipeTypes.ELECTROLYZER_RECIPES),
        CENTRIFUGE("centrifuge", GTRecipeTypes.CENTRIFUGE_RECIPES),
        SIFTER("sifter", GTRecipeTypes.SIFTER_RECIPES);

        private static final SmartFilteringMode[] VALUES = values();
        private final String localeName;
        private final GTRecipeType recipeType;
        private final Object2IntOpenCustomHashMap<ItemStack> cache = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        SmartFilteringMode(String localeName, GTRecipeType type) {
            this.localeName = localeName;
            this.recipeType = type;
        }

        public String getTooltip() {
            return "cover.item_smart_filter.filtering_mode." + localeName;
        }

        public static UITexture[] getTextures() {
            return Arrays.stream(VALUES)
                    .map(v -> UITexture.fullImage(GTCEu.MOD_ID,
                            "textures/block/machines/" + v.localeName + "/overlay_front.png", ColorType.DEFAULT))
                    .toArray(UITexture[]::new);
        }
    }
}
