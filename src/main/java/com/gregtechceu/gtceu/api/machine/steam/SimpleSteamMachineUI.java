package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.google.common.collect.Tables;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;

final class SimpleSteamMachineUI {

    private SimpleSteamMachineUI() {}

    static ModularUI create(SimpleSteamMachine machine, Player entityPlayer) {
        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        storages.put(IO.IN, ItemRecipeCapability.CAP, machine.importItems.storage);
        storages.put(IO.OUT, ItemRecipeCapability.CAP, machine.exportItems.storage);

        var group = machine.getRecipeType().getRecipeUI().createUITemplate(machine.recipeLogic::getProgressPercent,
                storages,
                new CompoundTag(),
                Collections.emptyList(),
                true,
                machine.isHighPressure);
        Position pos = new Position((Math.max(group.getSize().width + 4 + 8, 176) - 4 - group.getSize().width) / 2 + 4,
                32);
        group.setSelfPosition(pos);
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(machine.isHighPressure))
                .widget(group)
                .widget(new LabelWidget(5, 5, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(new PredicatedImageWidget(pos.x + group.getSize().width / 2 - 9,
                        pos.y + group.getSize().height / 2 - 9, 18, 18,
                        GuiTextures.INDICATOR_NO_STEAM.get(machine.isHighPressure))
                        .setPredicate(machine.recipeLogic::isWaiting))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(machine.isHighPressure), 7, 84, true));
    }
}
