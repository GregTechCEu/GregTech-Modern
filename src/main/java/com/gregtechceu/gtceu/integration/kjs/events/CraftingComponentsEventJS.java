package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
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

    private CraftingComponentsEventJS set(CraftingComponent craftingComponent, int tier, Object value) {
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
    private CraftingComponentsEventJS andSet(int tier, Object value) {
        if (context == null) return this;
        context.add(tier, value);
        return this;
    }

    public CraftingComponentsEventJS andSetItem(int tier, ItemStack stack) {
        return andSet(tier, stack);
    }

    public CraftingComponentsEventJS andSetTag(int tier, ResourceLocation tag) {
        return andSet(tier, TagKey.create(Registries.ITEM, tag));
    }

    public CraftingComponentsEventJS andSetMaterialEntry(int tier, MaterialEntry entry) {
        return andSet(tier, entry);
    }

    // Other utility methods
    public void set(CraftingComponent craftingComponent, Map<Object, Object> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            Object obj = parseObject(val.getValue());
            if (obj == null) return;
            craftingComponent.add(tier, obj);
        }
    }

    public void setItems(CraftingComponent craftingComponent, Map<Object, Object> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            ItemStack stack = parseItemStack(val.getValue());
            if (stack == null) {
                ConsoleJS.STARTUP.errorf("Invalid ItemStack %s passed to modifyItems!", val.getValue());
                return;
            }
            craftingComponent.add(tier, stack);
        }
    }

    public void setTags(CraftingComponent craftingComponent, Map<Object, Object> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            TagKey<Item> tagKey = parseTag(val.getValue());
            if (tagKey == null) {
                ConsoleJS.STARTUP.error("Invalid TagKey passed to modifyTags");
                return;
            }
            craftingComponent.add(tier, tagKey);
        }
    }

    public void setMaterialEntries(CraftingComponent craftingComponent, Map<Object, Object> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            MaterialEntry entry = MaterialEntry.of(val.getValue());
            if (entry == null) {
                ConsoleJS.STARTUP.error("Invalid MaterialEntry passed to modifyMaterialEntries");
                return;
            }
            craftingComponent.add(tier, entry);
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

    public void removeTiers(CraftingComponent craftingComponent, int... tiers) {
        for (int t : tiers) {
            craftingComponent.remove(t);
        }
    }

    private static ItemStack parseItemStack(Object o) {
        ItemStack stack = ItemStackJS.of(o);
        if (stack == null || stack.isEmpty()) return null;
        return stack;
    }

    private static TagKey<Item> parseTag(Object o) {
        ResourceLocation rl = UtilsJS.getMCID(null, o);
        if (rl != null) return TagKey.create(Registries.ITEM, rl);
        return null;
    }

    private static Object parseObject(Object o) {
        Object obj = parseItemStack(o);
        if (obj == null) obj = parseTag(o);
        if (obj == null) obj = MaterialEntry.of(o);
        if (obj == null) {
            ConsoleJS.STARTUP.errorf("%s is not of type ItemStack, MaterialEntry or TagKey<Item>", o);
        }
        return obj;
    }

    private static int parseTier(Object o) {
        int ret = -1;
        if (o instanceof CharSequence cs) {
            String str = cs.toString();
            try {
                int tier = Integer.parseUnsignedInt(str);
                if (tier >= 0 && tier < GTValues.TIER_COUNT) ret = tier;
            } catch (NumberFormatException ignored) {
                ret = GTValues.RVN.getInt(str);
            }
        } else if (o instanceof Number number) {
            int tier = number.intValue();
            if (tier >= 0 && tier < GTValues.TIER_COUNT) ret = tier;
        }

        if (ret == -1) ConsoleJS.STARTUP.errorf("%s is not a valid tier!", o);
        return ret;
    }
}
