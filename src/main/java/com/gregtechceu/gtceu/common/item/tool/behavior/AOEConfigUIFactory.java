package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getAoEDefinition;

final class AOEConfigUIFactory {

    private AOEConfigUIFactory() {}

    static ModularUI create(Player player, GTHeldItemUIHolder holder) {
        ItemStack held = holder.getHeld();
        final AoESymmetrical.Mutable definition = getAoEDefinition(held).toMutable();
        return new ModularUI(120, 80, holder, player).background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(6, 10, "item.gtceu.tool.aoe.columns"))
                .widget(new LabelWidget(49, 10, "item.gtceu.tool.aoe.rows"))
                .widget(new LabelWidget(79, 10, "item.gtceu.tool.aoe.layers"))
                .widget(new ButtonWidget(15, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.increaseColumn().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(15, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.decreaseColumn().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.increaseRow().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.decreaseRow().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.increaseLayer().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, definition.decreaseLayer().toImmutable());
                    holder.markAsDirty();
                }))
                .widget(new LabelWidget(23, 65,
                        () -> Integer.toString(
                                1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).column())))
                .widget(new LabelWidget(58, 65,
                        () -> Integer.toString(
                                1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).row())))
                .widget(new LabelWidget(93, 65, () -> Integer
                        .toString(1 + held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).layer())));
    }
}
