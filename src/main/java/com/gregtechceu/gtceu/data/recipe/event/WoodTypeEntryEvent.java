package com.gregtechceu.gtceu.data.recipe.event;

import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import net.minecraftforge.eventbus.api.Event;

import lombok.Getter;

import java.util.List;

@Getter
public class WoodTypeEntryEvent extends Event {

    private final List<WoodTypeEntry> entries;

    public WoodTypeEntryEvent(List<WoodTypeEntry> entries) {
        this.entries = entries;
    }
}
