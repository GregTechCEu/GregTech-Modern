package com.gregtechceu.gtceu.api.data.chemical;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ChemicalHelper
 */
public class ChemicalHelper {

    public static final Map<ItemLike, ItemMaterialInfo> ITEM_MATERIAL_INFO = new HashMap<>();
    public static final Map<ItemLike, UnificationEntry> ITEM_UNIFICATION_ENTRY = new HashMap<>();
    public static final Map<UnificationEntry, ArrayList<ItemLike>> UNIFICATION_ENTRY_ITEM = new Object2ObjectOpenHashMap<>();

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        ITEM_MATERIAL_INFO.put(item, materialInfo);
    }

    public static void registerUnificationEntry(ItemLike item, UnificationEntry unificationEntry) {
        ITEM_UNIFICATION_ENTRY.put(item, unificationEntry);
    }

    public static void registerUnificationItems(UnificationEntry unificationEntry, ItemLike... items) {
        UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                .addAll(Arrays.stream(items).toList());
        for (ItemLike item : items) {
            registerUnificationEntry(item, unificationEntry);
        }
    }

    public static void registerUnificationItems(TagPrefix tagPrefix, @Nullable Material material, ItemLike... items) {
        registerUnificationItems(new UnificationEntry(tagPrefix, material), items);
    }

    @Nullable
    public static MaterialStack getMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        var entry = ITEM_UNIFICATION_ENTRY.get(itemStack.getItem());
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial == null) {
                entryMaterial = entry.tagPrefix.materialType();
            }
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = ITEM_MATERIAL_INFO.get(itemStack.getItem());
        return info == null ? null : info.getMaterial().copy();
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(ItemLike item) {
        return ITEM_UNIFICATION_ENTRY.get(item);
    }

    public static List<ItemLike> getItems(UnificationEntry unificationEntry) {
        return UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> {
            var items = new ArrayList<ItemLike>();
            for (TagKey<Item> tag : getTags(unificationEntry.tagPrefix, unificationEntry.material)) {
                for (Holder<Item> itemHolder : Registry.ITEM.getTagOrEmpty(tag)) {
                    items.add(itemHolder.value());
                }
            }
            return items;
        });
    }

    public static ItemStack get(UnificationEntry unificationEntry, int size) {
        var list = getItems(unificationEntry);
        if (list.isEmpty()) return ItemStack.EMPTY;
        var stack = list.get(0).asItem().getDefaultInstance();
        stack.setCount(size);
        return stack;
    }

    public static ItemStack get(TagPrefix orePrefix, Material material, int stackSize) {
        return get(new UnificationEntry(orePrefix, material), stackSize);
    }

    public static ItemStack get(TagPrefix orePrefix, Material material) {
        return get(orePrefix, material, 1);
    }

    public static TagKey<Item> getTag(TagPrefix orePrefix, @Nullable Material material) {
        var tags = material == null ? orePrefix.getItemTags() : orePrefix.getSubItemTags(material);
        if (tags.length > 0) {
            return tags[0];
        }
        return TagUtil.createItemTag(FormattingUtil.toLowerCaseUnder(orePrefix.name) + (material == null ? "" : ("/" + material.getName())));
    }

    public static TagKey<Item>[] getTags(TagPrefix orePrefix, @Nullable Material material) {
        var tags = material == null ? orePrefix.getItemTags() : orePrefix.getSubItemTags(material);
        if (tags.length == 0) {
            tags = new TagKey[]{TagUtil.createItemTag(FormattingUtil.toLowerCaseUnder(orePrefix.name) + (material == null ? "" : ("/" + material.getName())))};
        }
        return tags;
    }

    public static ItemStack getDust(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.DUST) || materialAmount <= 0)
            return ItemStack.EMPTY;
        if (materialAmount % GTValues.M == 0 || materialAmount >= GTValues.M * 16)
            return get(TagPrefix.dust, material, (int) (materialAmount / GTValues.M));
        else if ((materialAmount * 4) % GTValues.M == 0 || materialAmount >= GTValues.M * 8)
            return get(TagPrefix.dustSmall, material, (int) ((materialAmount * 4) / GTValues.M));
        else if ((materialAmount * 9) >= GTValues.M)
            return get(TagPrefix.dustTiny, material, (int) ((materialAmount * 9) / GTValues.M));
        return ItemStack.EMPTY;
    }
}
