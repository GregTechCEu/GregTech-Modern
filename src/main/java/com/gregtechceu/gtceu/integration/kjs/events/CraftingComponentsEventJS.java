package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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

    private ComponentWrapper create(String id, Object fallback) {
        return new ComponentWrapper(id, fallback);
    }

    public ComponentWrapper createItem(String id, ItemStack stack) {
        return create(id, stack);
    }

    public ComponentWrapper createTag(String id, ResourceLocation tag) {
        return create(id, TagKey.create(Registries.ITEM, tag));
    }

    public ComponentWrapper createMaterialEntry(String id, MaterialEntry entry) {
        return create(id, entry);
    }

    // Set singular
    private void set(CraftingComponent craftingComponent, int tier, Object value) {
        craftingComponent.add(tier, value);
    }

    public void setItem(CraftingComponent craftingComponent, int tier, ItemStack item) {
        set(craftingComponent, tier, item);
    }

    public void setTag(CraftingComponent craftingComponent, int tier, ResourceLocation tag) {
        set(craftingComponent, tier, TagKey.create(Registries.ITEM, tag));
    }

    public void setMaterialEntry(CraftingComponent craftingComponent, int tier,
                                 MaterialEntry matEntry) {
        set(craftingComponent, tier, matEntry);
    }

    // Set from Map methods
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

    @SuppressWarnings("unchecked")
    private static TagKey<Item> parseTag(Object o) {
        if (o instanceof TagKey<?> key && key.isFor(Registries.ITEM)) return (TagKey<Item>) key;
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

    public static class ComponentWrapper {

        private final CraftingComponent component;

        private ComponentWrapper(String id, Object fallback) {
            component = CraftingComponent.of(id, fallback);
        }

        private ComponentWrapper set(int tier, Object value) {
            component.add(tier, value);
            return this;
        }

        public ComponentWrapper setItem(int tier, ItemStack stack) {
            return set(tier, stack);
        }

        public ComponentWrapper setTag(int tier, ResourceLocation tag) {
            return set(tier, TagKey.create(Registries.ITEM, tag));
        }

        public ComponentWrapper setMaterialEntry(int tier, MaterialEntry entry) {
            return set(tier, entry);
        }

        public ComponentWrapper setMaterialEntry(int tier, TagPrefix prefix, Material mat) {
            return set(tier, new MaterialEntry(prefix, mat));
        }

        public Object get(int tier) {
            return component.get(tier);
        }
    }
}
