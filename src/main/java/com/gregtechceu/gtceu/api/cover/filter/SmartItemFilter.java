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
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import brachy.modularui.widgets.menu.Menu;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;

public class SmartItemFilter extends Filter<ItemStack> {

    @Getter
    private SmartFilteringMode filterMode = SmartFilteringMode.ELECTROLYZER;

    public SmartItemFilter(ItemStack stack) {
        super(stack);

        var tag = stack.getOrCreateTag();
        if (tag.isEmpty()) return;
        filterMode = SmartFilteringMode.VALUES[tag.getInt("filterMode")];
    }

    @Override
    public @Nullable CompoundTag saveFilter() {
        if (filterMode.ordinal() == 0) {
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
        return new Dialog<>("smart_item_filter")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .child(GTMuiWidgets.createTitleBar(() -> GTItems.SMART_ITEM_FILTER.asStack(), 176,
                        GTGuiTextures.BACKGROUND))
                .child(getFilterUI(data, syncManager, settings))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        EnumSyncValue<SmartFilteringMode> mode = new EnumSyncValue<>(SmartFilteringMode.class,
                this::getFilterMode, this::setFilterMode).allowC2S();

        syncManager.syncValue("mode", mode);

        return Flow.row()
                .child(new ContextMenuButton<>("smart_filter")
                        .size(18)
                        .requiresClick()
                        .tooltip(r -> r.add(Text.str("Set Machine Recipe Type")))
                        .openRightDown()
                        .overlay(new DynamicDrawable(() -> SmartFilteringMode.getTextures()[mode.getIntValue()]))
                        .menu(new Menu<>()
                                .width(20)
                                .coverChildrenHeight()
                                .padding(2)
                                .child(new ListWidget<>()
                                        .maxSize(SmartFilteringMode.VALUES.length * 20)
                                        .widthRel(1.f)
                                        .children(SmartFilteringMode.VALUES.length, w -> {
                                            IBoolValue<?> bsv = new BoolValue.Dynamic(() -> mode.getIntValue() == w,
                                                    bool -> mode.setIntValue(w));

                                            return new ToggleButton()
                                                    .overlay(SmartFilteringMode.getTextures()[w])
                                                    .background(GuiTextures.MC_BUTTON)
                                                    .selectedBackground(GuiTextures.MC_BUTTON)
                                                    .value(bsv)
                                                    .tooltip(r -> r.add(Text.comp(Component
                                                            .translatable(SmartFilteringMode.VALUES[w].getTooltip()))));
                                        }))))
                .child(Text.str("Recipe Type").asWidget().verticalCenter().rightRel(0.f));
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testAmount(itemStack) > 0;
    }

    @Override
    public int testAmount(ItemStack itemStack) {
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
