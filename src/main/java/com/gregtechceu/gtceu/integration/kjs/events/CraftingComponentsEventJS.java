package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.rhino.Context;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings({ "unused" })
@NoArgsConstructor
public class CraftingComponentsEventJS implements KubeStartupEvent {

    public ComponentWrapper createItem(String id, ItemStack stack) {
        return ComponentWrapper.of(id, stack);
    }

    public ComponentWrapper createTag(String id, TagKey<Item> tag) {
        return ComponentWrapper.of(id, tag);
    }

    public ComponentWrapper createMaterialEntry(String id, MaterialEntry entry) {
        return ComponentWrapper.of(id, entry);
    }

    // Set singular

    public void setItem(CraftingComponent craftingComponent, int tier, ItemStack item) {
        craftingComponent.add(tier, item);
    }

    public void setTag(CraftingComponent craftingComponent, int tier, TagKey<Item> tag) {
        craftingComponent.add(tier, tag);
    }

    public void setMaterialEntry(CraftingComponent craftingComponent, int tier,
                                 MaterialEntry matEntry) {
        craftingComponent.add(tier, matEntry);
    }

    public void setItems(Context cx, CraftingComponent craftingComponent, Map<Object, ItemStack> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            ItemStack stack = parseItemStack(cx, val.getValue());
            if (stack == null) {
                ConsoleJS.STARTUP.errorf("Invalid ItemStack %s passed to setItems!", val.getValue());
                return;
            }
            craftingComponent.add(tier, stack);
        }
    }

    public void setTags(CraftingComponent craftingComponent, Map<Object, TagKey<Item>> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            TagKey<Item> tagKey = parseTag(val.getValue());
            if (tagKey == null) {
                ConsoleJS.STARTUP.error("Invalid TagKey passed to setTags");
                return;
            }
            craftingComponent.add(tier, tagKey);
        }
    }

    public void setMaterialEntries(CraftingComponent craftingComponent, Map<Object, MaterialEntry> map) {
        for (var val : map.entrySet()) {
            int tier = parseTier(val.getKey());
            if (tier == -1) return;
            MaterialEntry entry = MaterialEntry.of(val.getValue());
            if (entry == null) {
                ConsoleJS.STARTUP.error("Invalid MaterialEntry passed to setMaterialEntries");
                return;
            }
            craftingComponent.add(tier, entry);
        }
    }

    public void setFallbackItem(CraftingComponent craftingComponent, ItemStack stack) {
        craftingComponent.setFallback(new CraftingComponent.CraftingComponentEntry(stack));
    }

    public void setFallbackTag(CraftingComponent craftingComponent, TagKey<Item> tag) {
        craftingComponent.setFallback(new CraftingComponent.CraftingComponentEntry(tag));
    }

    public void setFallbackMaterialEntry(CraftingComponent craftingComponent, MaterialEntry materialEntry) {
        craftingComponent.setFallback(new CraftingComponent.CraftingComponentEntry(materialEntry));
    }

    public void removeTier(CraftingComponent craftingComponent, int tier) {
        craftingComponent.remove(tier);
    }

    public void removeTiers(CraftingComponent craftingComponent, int... tiers) {
        for (int t : tiers) {
            craftingComponent.remove(t);
        }
    }

    private static @Nullable ItemStack parseItemStack(Context cx, Object o) {
        if (o instanceof ItemStack stack) {
            if (stack.isEmpty()) return null;
            return stack;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable TagKey<Item> parseTag(Object o) {
        if (o instanceof TagKey<?> key && key.isFor(Registries.ITEM)) return (TagKey<Item>) key;
        ResourceLocation rl = ID.mc(o);
        if (rl != null) return TagKey.create(Registries.ITEM, rl);
        return null;
    }

    private static Object parseObject(Context cx, Object o) {
        Object obj = parseItemStack(cx, o);
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
                ret = GTUtil.getTierByName(str);
            }
        } else if (o instanceof Number number) {
            int tier = number.intValue();
            if (tier >= 0 && tier < GTValues.TIER_COUNT) ret = tier;
        }

        if (ret == -1) ConsoleJS.STARTUP.errorf("%s is not a valid tier!", o);
        return ret;
    }

    public static class ComponentWrapper extends CraftingComponent {

        private ComponentWrapper() {
            super();
        }

        public static ComponentWrapper of(@NotNull String id, @NotNull ItemStack fallback) {
            if (ALL_COMPONENTS.containsKey(id)) {
                // Throw here because we don't want Kubers to mess with existing components
                throw new IllegalArgumentException("Duplicate crafting component: " + id);
            }
            var ret = new ComponentWrapper();
            ret.setFallback(new CraftingComponentEntry(fallback));
            ALL_COMPONENTS.put(id, ret);
            return ret;
        }

        public static ComponentWrapper of(@NotNull String id, @NotNull MaterialEntry fallback) {
            if (ALL_COMPONENTS.containsKey(id)) {
                // Throw here because we don't want Kubers to mess with existing components
                throw new IllegalArgumentException("Duplicate crafting component: " + id);
            }
            var ret = new ComponentWrapper();
            ret.setFallback(new CraftingComponentEntry(fallback));
            ALL_COMPONENTS.put(id, ret);
            return ret;
        }

        public static ComponentWrapper of(@NotNull String id, @NotNull TagKey<Item> fallback) {
            if (ALL_COMPONENTS.containsKey(id)) {
                // Throw here because we don't want Kubers to mess with existing components
                throw new IllegalArgumentException("Duplicate crafting component: " + id);
            }
            var ret = new ComponentWrapper();
            ret.setFallback(new CraftingComponentEntry(fallback));
            ALL_COMPONENTS.put(id, ret);
            return ret;
        }

        public ComponentWrapper addItem(int tier, ItemStack stack) {
            add(tier, stack);
            return this;
        }

        public ComponentWrapper addTag(int tier, ResourceLocation tag) {
            add(tier, TagKey.create(Registries.ITEM, tag));
            return this;
        }

        public ComponentWrapper addMaterialEntry(int tier, MaterialEntry entry) {
            add(tier, entry);
            return this;
        }

        public ComponentWrapper addMaterialEntry(int tier, TagPrefix prefix, Material mat) {
            add(tier, new MaterialEntry(prefix, mat));
            return this;
        }
    }
}
