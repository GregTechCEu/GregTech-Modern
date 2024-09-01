package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.EventJS;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "unchecked" })
@NoArgsConstructor
public class CraftingComponentEventJS extends EventJS {

    public void modify(CraftingComponent.Component component, int tier, Object value) {
        component.appendIngredients(Map.of(tier, value));
    }

    public void modify(CraftingComponent.Component component, Int2ObjectMap<Object> map) {
        component.appendIngredients(map);
    }

    public void modifyItem(CraftingComponent.Component component, int tier, ItemStack item) {
        component.appendIngredients(Map.of(tier, item));
    }

    public void modifyItem(CraftingComponent.Component component, Int2ObjectMap<ItemStack> map) {
        component.appendIngredients((Map<Integer, Object>) (Map<?, ?>) map);
    }

    public void modifyTag(CraftingComponent.Component component, int tier, ResourceLocation tag) {
        component.appendIngredients(Map.of(tier, TagKey.create(Registries.ITEM, tag)));
    }

    public void modifyTag(CraftingComponent.Component component, Int2ObjectMap<ResourceLocation> map) {
        Map<Integer, Object> newMap = map.int2ObjectEntrySet()
                .stream()
                .map(entry -> Map.entry(entry.getIntKey(), TagKey.create(Registries.ITEM, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        component.appendIngredients(newMap);
    }

    public void modifyUnificationEntry(CraftingComponent.Component component, int tier, UnificationEntry item) {
        component.appendIngredients(Map.of(tier, item));
    }

    public void modifyUnificationEntry(CraftingComponent.Component component, Int2ObjectMap<UnificationEntry> map) {
        component.appendIngredients((Map<Integer, Object>) (Map<?, ?>) map);
    }

    public CraftingComponent.Component create(Int2ObjectMap<Object> map) {
        return new CraftingComponent.Component(map);
    }

    public CraftingComponent.Component createItem(Int2ObjectMap<ItemStack> map) {
        return new CraftingComponent.Component((Map<Integer, Object>) (Map<?, ?>) map);
    }

    public CraftingComponent.Component createTag(Int2ObjectMap<ResourceLocation> map) {
        Map<Integer, Object> newMap = map.int2ObjectEntrySet()
                .stream()
                .map(entry -> Map.entry(entry.getIntKey(), TagKey.create(Registries.ITEM, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CraftingComponent.Component(newMap);
    }

    public CraftingComponent.Component createUnificationEntry(Int2ObjectMap<UnificationEntry> map) {
        return new CraftingComponent.Component((Map<Integer, Object>) (Map<?, ?>) map);
    }
}
