package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "unchecked" })
@NoArgsConstructor
public class CraftingComponentsEventJS extends StartupEventJS {

    public void modify(CraftingComponent.Component component, int tier, Object value) {
        component.appendIngredients(Map.of(tier, value));
    }

    public void modify(CraftingComponent.Component component, Map<Number, Object> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        component.appendIngredients(newMap);
    }

    public void modifyItem(CraftingComponent.Component component, int tier, ItemStack item) {
        component.appendIngredients(Map.of(tier, item));
    }

    public void modifyItem(CraftingComponent.Component component, Map<Number, ItemStack> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        component.appendIngredients(newMap);
    }

    public void modifyTag(CraftingComponent.Component component, int tier, ResourceLocation tag) {
        component.appendIngredients(Map.of(tier, TagKey.create(Registries.ITEM, tag)));
    }

    public void modifyTag(CraftingComponent.Component component, Map<Number, ResourceLocation> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), TagKey.create(Registries.ITEM, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        component.appendIngredients(newMap);
    }

    public void modifyUnificationEntry(CraftingComponent.Component component, int tier, UnificationEntry item) {
        component.appendIngredients(Map.of(tier, item));
    }

    public void modifyUnificationEntry(CraftingComponent.Component component, Map<Number, UnificationEntry> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        component.appendIngredients(newMap);
    }

    public CraftingComponent.Component create(Map<Number, Object> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CraftingComponent.Component(newMap);
    }

    public CraftingComponent.Component createItem(Map<Number, ItemStack> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CraftingComponent.Component(newMap);
    }

    public CraftingComponent.Component createTag(Map<Number, ResourceLocation> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), TagKey.create(Registries.ITEM, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CraftingComponent.Component(newMap);
    }

    public CraftingComponent.Component createUnificationEntry(Map<Number, UnificationEntry> map) {
        Map<Integer, Object> newMap = map.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().intValue(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CraftingComponent.Component(newMap);
    }
}
