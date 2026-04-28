package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Triplet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

final class ItemMagnetBehaviorUI {

    private ItemMagnetBehaviorUI() {}

    static Object create(GTHeldItemUIHolder holder, Player entityPlayer) {
        final ItemStack held = holder.getHeld();
        ItemMagnetBehavior.MagnetComponent magnetData = held.getOrDefault(GTDataComponents.MAGNET,
                ItemMagnetBehavior.MagnetComponent.EMPTY);
        ItemMagnetBehavior.Filter selected = magnetData.filterType();

        HashSet<Triplet<ItemMagnetBehavior.Filter, Widget, Widget>> widgets = new HashSet<>();
        HashMap<ItemMagnetBehavior.Filter, ItemFilter> filters = new HashMap<>();
        ModularUI ui = new ModularUI(176, 157, holder, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new EnumSelectorWidget<>(146, 5, 20, 20,
                        FilterSelection.values(), FilterSelection.of(selected),
                        val -> updateSelection(held, val.filter, widgets)))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 75, true));
        for (ItemMagnetBehavior.Filter f : ItemMagnetBehavior.Filter.values()) {
            ItemStack stack = f.getFilter(held);
            ItemFilter filter = ItemFilter.loadFilter(stack);
            filters.put(f, filter);
            LabelWidget description = new LabelWidget(5, 5, stack.getItem().getDescriptionId());
            WidgetGroup config = (WidgetGroup) filter.openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15);
            boolean visible = f == selected;
            description.setVisible(visible);
            config.setVisible(visible);
            widgets.add(new Triplet<>(f, description, config));
            ui.widget(description);
            ui.widget(config);
        }
        ui.registerCloseListener(() -> {
            ItemMagnetBehavior.Filter selection = magnetData.filterType();
            selection.saveFilter(held, filters.get(selection));
        });
        return ui;
    }

    private static void updateSelection(ItemStack stack, ItemMagnetBehavior.Filter filter,
                                        Collection<Triplet<ItemMagnetBehavior.Filter, Widget, Widget>> widgets) {
        stack.update(GTDataComponents.MAGNET, ItemMagnetBehavior.MagnetComponent.EMPTY,
                c -> new ItemMagnetBehavior.MagnetComponent(c.active(), filter));
        widgets.forEach(tri -> {
            var visible = tri.getA() == filter;
            tri.getB().setVisible(visible);
            tri.getC().setVisible(visible);
        });
    }

    private enum FilterSelection implements EnumSelectorWidget.SelectableEnum {

        SIMPLE(ItemMagnetBehavior.Filter.SIMPLE),
        TAG(ItemMagnetBehavior.Filter.TAG);

        private final ItemMagnetBehavior.Filter filter;

        FilterSelection(ItemMagnetBehavior.Filter filter) {
            this.filter = filter;
        }

        static FilterSelection of(ItemMagnetBehavior.Filter filter) {
            return filter == ItemMagnetBehavior.Filter.TAG ? TAG : SIMPLE;
        }

        @Override
        public @NotNull String getTooltip() {
            return filter.getTooltip();
        }

        @Override
        public @NotNull Object getIcon() {
            return new ResourceTexture("gtceu:textures/item/" + filter.name + ".png");
        }
    }
}
