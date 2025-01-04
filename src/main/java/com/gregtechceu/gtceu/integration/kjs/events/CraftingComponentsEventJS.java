package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unused", "unchecked" })
@NoArgsConstructor
public class CraftingComponentsEventJS extends StartupEventJS {

    public void modify(CraftingComponent craftingComponent, int tier, Object value) {
        craftingComponent.add(tier, value);
    }

    public void modify(CraftingComponent craftingComponent, Map<Number, Object> map) {
        for (var val : map.entrySet()) {
            craftingComponent.add(val.getKey().intValue(), val.getValue());
        }
    }

    public void modifyItem(CraftingComponent craftingComponent, int tier, ItemStack item) {
        craftingComponent.add(tier, item);
    }

    public void modifyItem(CraftingComponent craftingComponent, Map<Number, ItemStack> map) {
        for (var val : map.entrySet()) {
            craftingComponent.add(val.getKey().intValue(), val.getValue());
        }
    }

    public void modifyTag(CraftingComponent craftingComponent, int tier, ResourceLocation tag) {
        craftingComponent.add(tier, TagKey.create(Registries.ITEM, tag));
    }

    public void modifyTag(CraftingComponent craftingComponent, Map<Number, ResourceLocation> map) {
        for (var val : map.entrySet()) {
            craftingComponent.add(val.getKey().intValue(), TagKey.create(Registries.ITEM, val.getValue()));
        }
    }

    public void modifyUnificationEntry(CraftingComponent craftingComponent, int tier, UnificationEntry item) {
        craftingComponent.add(tier, item);
    }

    public void modifyUnificationEntry(CraftingComponent craftingComponent, Map<Number, UnificationEntry> map) {
        for (var val : map.entrySet()) {
            craftingComponent.add(val.getKey().intValue(), val.getValue());
        }
    }

    public void setFallbackItem(CraftingComponent craftingComponent, ItemStack stack) {
        craftingComponent.setFallback(stack);
    }

    public void setFallbackTag(CraftingComponent craftingComponent, ResourceLocation tag) {
        craftingComponent.setFallback(TagKey.create(Registries.ITEM, tag));
    }

    public void setFallbackUnificationEntry(CraftingComponent craftingComponent, UnificationEntry unificationEntry) {
        craftingComponent.setFallback(unificationEntry);
    }

    public void removeTier(CraftingComponent craftingComponent, int tier) {
        craftingComponent.remove(tier);
    }

    public void removeTiers(CraftingComponent craftingComponent, List<Number> tiers) {
        for (var tier : tiers) {
            craftingComponent.remove(tier.intValue());
        }
    }

    public CraftingComponent create(Object fallback, Map<Number, Object> map) {
        var m = new CraftingComponent(fallback);
        for (var val : map.entrySet()) {
            m.add(val.getKey().intValue(), val.getValue());
        }
        return m;
    }

    public CraftingComponent createItem(Object fallback, Map<Number, ItemStack> map) {
        var m = new CraftingComponent(fallback);
        for (var val : map.entrySet()) {
            m.add(val.getKey().intValue(), val.getValue());
        }
        return m;
    }

    public CraftingComponent createTag(Object fallback, Map<Number, ResourceLocation> map) {
        var m = new CraftingComponent(fallback);
        for (var val : map.entrySet()) {
            m.add(val.getKey().intValue(), TagKey.create(Registries.ITEM, val.getValue()));
        }
        return m;
    }

    public CraftingComponent createUnificationEntry(Object fallback, Map<Number, UnificationEntry> map) {
        var m = new CraftingComponent(fallback);
        for (var val : map.entrySet()) {
            m.add(val.getKey().intValue(), val.getValue());
        }
        return m;
    }
}
