package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import lombok.NoArgsConstructor;

import java.util.Map;

@SuppressWarnings({ "unused" })
@NoArgsConstructor
public class CraftingComponentsEventJS extends StartupEventJS {

    private CraftingComponent context = null;

    // Context setting methods
    private CraftingComponentsEventJS create(String id, Object fallback) {
        context = CraftingComponent.of(id, fallback);
        return this;
    }

    public CraftingComponentsEventJS createItem(String id, ItemStack stack) {
        return create(id, stack);
    }

    public CraftingComponentsEventJS createTag(String id, ResourceLocation tag) {
        return create(id, TagKey.create(Registries.ITEM, tag));
    }

    public CraftingComponentsEventJS createMaterialEntry(String id, MaterialEntry entry) {
        return create(id, entry);
    }

    public CraftingComponentsEventJS set(CraftingComponent craftingComponent, int tier, Object value) {
        context = craftingComponent;
        context.add(tier, value);
        return this;
    }

    public CraftingComponentsEventJS setItem(CraftingComponent craftingComponent, int tier, ItemStack item) {
        return set(craftingComponent, tier, item);
    }

    public CraftingComponentsEventJS setTag(CraftingComponent craftingComponent, int tier, ResourceLocation tag) {
        return set(craftingComponent, tier, TagKey.create(Registries.ITEM, tag));
    }

    public CraftingComponentsEventJS setMaterialEntry(CraftingComponent craftingComponent, int tier,
                                                      MaterialEntry matEntry) {
        return set(craftingComponent, tier, matEntry);
    }

    // Context-chaining methods
    public CraftingComponentsEventJS set(int tier, Object value) {
        if (context == null) return this;
        context.add(tier, value);
        return this;
    }

    public CraftingComponentsEventJS setItem(int tier, ItemStack stack) {
        return set(tier, stack);
    }

    public CraftingComponentsEventJS setTag(int tier, ResourceLocation tag) {
        return set(tier, TagKey.create(Registries.ITEM, tag));
    }

    public CraftingComponentsEventJS setMaterialEntry(int tier, MaterialEntry entry) {
        return set(tier, entry);
    }

    // Other utility methods
    public void modify(CraftingComponent craftingComponent, Map<Integer, Object> map) {
        for (var val : map.entrySet()) {
            parseModify(craftingComponent, val.getKey(), val.getValue());
        }
    }

    private static void parseModify(CraftingComponent component, int tier, Object o) {
        Object obj = ItemStackJS.of(o);
        if (obj == null || ((ItemStack) obj).isEmpty()) {
            obj = UtilsJS.getMCID(null, o);
            if (obj != null) obj = TagKey.create(Registries.ITEM, (ResourceLocation) obj);
        }
        if (obj == null) obj = MaterialEntry.of(o);
        if (obj == null)
            throw new IllegalArgumentException("Object is not of type ItemStack, MaterialEntry or TagKey<Item>");
        component.add(tier, obj);
    }

    public void modifyItems(CraftingComponent craftingComponent, Map<Integer, Object> map) {
        for (var val : map.entrySet()) {
            ItemStack stack = ItemStackJS.of(val.getValue());
            if (stack.isEmpty()) throw new IllegalArgumentException("Invalid ItemStack passed to modifyItems");
            craftingComponent.add(val.getKey(), stack);
        }
    }

    public void modifyTags(CraftingComponent craftingComponent, Map<Integer, Object> map) {
        for (var val : map.entrySet()) {
            ResourceLocation rl = UtilsJS.getMCID(null, val.getValue());
            TagKey<Item> tagKey = null;
            if (rl != null) {
                tagKey = TagKey.create(Registries.ITEM, rl);
            }
            if (tagKey == null) throw new IllegalArgumentException("Invalid TagKey passed to modifyTags");
            craftingComponent.add(val.getKey(), tagKey);
        }
    }

    public void modifyMaterialEntries(CraftingComponent craftingComponent, Map<Integer, Object> map) {
        for (var val : map.entrySet()) {
            MaterialEntry entry = MaterialEntry.of(val.getValue());
            if (entry == null)
                throw new IllegalArgumentException("Invalid MaterialEntry passed to modifyMaterialEntries");
            craftingComponent.add(val.getKey(), entry);
        }
    }

    public void setFallbackItem(CraftingComponent craftingComponent, ItemStack stack) {
        craftingComponent.setFallback(stack);
    }

    public void setFallbackTag(CraftingComponent craftingComponent, ResourceLocation tag) {
        craftingComponent.setFallback(TagKey.create(Registries.ITEM, tag));
    }

    public void setFallbackMaterialEntry(CraftingComponent craftingComponent, MaterialEntry materialEntry) {
        craftingComponent.setFallback(materialEntry);
    }

    public void removeTier(CraftingComponent craftingComponent, int tier) {
        craftingComponent.remove(tier);
    }

    public void removeTiers(CraftingComponent craftingComponent, int[] tiers) {
        for (int t : tiers) {
            craftingComponent.remove(t);
        }
    }
}
